package ambitor.easy.statemachine.service;

import ambitor.easy.statemachine.core.StateMachine;
import ambitor.easy.statemachine.core.annotation.EnableWithStateMachine;
import ambitor.easy.statemachine.core.configurer.StateMachineConfigurer;
import ambitor.easy.statemachine.core.configurer.adapter.StateMachineConfigurerAdapter;
import ambitor.easy.statemachine.core.context.MessageHeaders;
import ambitor.easy.statemachine.core.exception.StateMachineException;
import ambitor.easy.statemachine.core.exception.StateMachineRetryException;
import ambitor.easy.statemachine.core.factory.StateMachineFactory;
import ambitor.easy.statemachine.model.StateMachineTask;
import ambitor.easy.statemachine.model.TaskStatus;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static ambitor.easy.statemachine.model.StateMachineConstant.TASK_HEADER;

/**
 * 状态机service
 */
@Slf4j
@Component
public abstract class AbstractStateMachineTaskService<S extends Enum<S>, E extends Enum<E>> implements ApplicationContextAware, StateMachineTaskService {

    @Autowired
    private StateMachineTaskService stateMachineTaskService;
    private static ApplicationContext context = null;

    /**
     * 通过状态机名称拿到状态机Configurer
     * @param stateMachineName 状态机名称
     * @return 配置
     */
    public StateMachineConfigurer<S, E> getByName(String stateMachineName) {
        if (StringUtils.isEmpty(stateMachineName)) throw new StateMachineException("状态机名称为空");
        Map<String, Object> stateMachineConfigs = context.getBeansWithAnnotation(EnableWithStateMachine.class);
        Collection<Object> values = stateMachineConfigs.values();
        if (!CollectionUtils.isEmpty(values)) {
            for (Object stateMachineConfig : values) {
                StateMachineConfigurerAdapter adapter = (StateMachineConfigurerAdapter) stateMachineConfig;
                if (stateMachineName.equals(adapter.getName())) return adapter;
            }
        }
        throw new StateMachineException("找不到名称{}状态机", stateMachineName);
    }

    /**
     * 通过定时调度启动任务
     * 1、从数据库中将任务查询出来
     * 2、标记任务为运行中
     * 3、将任务放入到MQ中
     * 注意事务处理，忽略超时重试的场景
     */
    public void executeStateMachineTask() {
        List<StateMachineTask> list = getExecuteTask();
        for (StateMachineTask task : list) {
            updateAndSendToMq(task);
        }
    }

    /**
     * 更新状态后推送到mq
     * @param task 任务
     */
    public void updateAndSendToMq(StateMachineTask task) {
        Date now = new Date();
        task.setCurrentTrytimes((1 + task.getCurrentTrytimes()));
        task.setScanStatus(TaskStatus.running.name());
        task.setUpdateTime(now);
        int affect = stateMachineTaskService.updateByPrimaryKeySelective(task);
        if (affect > 0) {
            sendToMq(task);
        } else {
            log.info("修改状态机任务为Running失败，不放入MQ task->{}", task.getTransactionId());
        }
    }

    /**
     * 执行task
     * @param task task
     */
    public void processTask(StateMachineTask task) {
        String transactionId = task.getTransactionId();
        log.info("{} 状态机开始执行:{}", transactionId, JSON.toJSONString(task));
        try {
            boolean locked = lock(transactionId);
            if (!locked) {
                log.info("状态机执行时获取锁失败 transactionId->{}", transactionId);
                throw new StateMachineRetryException("状态机执行时获取锁失败");
            }
            StateMachineConfigurer<S, E> configurer = getByName(task.getMachineType());
            //生成一个状态机
            StateMachine<S, E> stateMachine = StateMachineFactory.build(configurer);
            S s = stateMachine.getState().getId();
            //重置状态机的当前状态
            stateMachine.resetStateMachine(Enum.valueOf(s.getDeclaringClass(), task.getMachineState()));
            MessageHeaders headers = new MessageHeaders();
            headers.addHeader(TASK_HEADER, task);
            boolean accept = stateMachine.start(headers);
            StateMachineTask update = new StateMachineTask();
            update.setId(task.getId());
            log.info("{}状态机执行结束,accept：{},当前状态{},异常{}", transactionId, accept,
                    stateMachine.getState().getId(), stateMachine.getStateMachineError());
            if (!accept) {
                //没有接受的话保存异常信息到StateMachineTask
                update.setResponseData(JSON.toJSONString(stateMachine.getStateMachineError()));
                update.setScanStatus(task.isLastRetry() ? TaskStatus.close.name() : TaskStatus.error.name());
                update.setNextRunTime(LocalDateTime2Date(LocalDateTime.now().plusMinutes(5)));
            } else {
                //如果不是结束状态，并且接受了，则重置currentTryTimes
                update.setCurrentTrytimes(0);
                //设置response
                update.setResponseData(task.getResponseData());
                //设置scanStatus
                update.setScanStatus(stateMachine.getState().isSuspend() ? TaskStatus.suspend.name() : TaskStatus.open.name());
                //如果是结束状态
                if (stateMachine.getState().isEnd()) {
                    update.setScanStatus(TaskStatus.close.name());
                }
            }
            stateMachineTaskService.updateByPrimaryKeySelective(update);
        } catch (Exception e) {
            log.error("{}状态机执行发生异常", transactionId, e);
            StateMachineTask update = new StateMachineTask();
            //没有接受的话保存异常信息到StateMachineTask
            update.setId(task.getId());
            update.setResponseData(JSON.toJSONString(e));
            update.setScanStatus(task.isLastRetry() ? TaskStatus.close.name() : TaskStatus.error.name());
            update.setNextRunTime(LocalDateTime2Date(LocalDateTime.now().plusMinutes(5)));
            stateMachineTaskService.updateByPrimaryKeySelective(update);
        } finally {
            log.info("{} 释放锁", transactionId);
            unLock(transactionId);
        }
    }

    /**
     * 获取容器
     * @param applicationContext 上下文
     * @throws BeansException 异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        context = applicationContext;
    }

    /**
     * 时间转换
     * @param localDateTime 时间
     */
    private static Date LocalDateTime2Date(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * 放入MQ
     */
    public abstract void sendToMq(StateMachineTask task);

    /**
     * 对machineTask加锁
     * @param transactionId 状态机的事务ID
     * @return true 成功 false 失败
     */
    public abstract boolean lock(String transactionId);

    /**
     * 对machineTask解锁
     * @param transactionId 状态机的事务ID
     * @return true 成功 false 失败
     */
    public abstract boolean unLock(String transactionId);

}
