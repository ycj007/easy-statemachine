package ambitor.easy.statemachine.core.action;


import ambitor.easy.statemachine.core.context.MessageHeaders;
import ambitor.easy.statemachine.core.context.StateContext;
import ambitor.easy.statemachine.core.exception.StateMachineException;
import ambitor.easy.statemachine.core.exception.StateMachineRetryException;
import ambitor.easy.statemachine.model.StateMachineTask;

import java.util.function.Consumer;

import static ambitor.easy.statemachine.model.StateMachineConstant.TASK_HEADER;

/**
 * 转换操作，在执行某个转换时执行的具体操作
 * Created by Ambitor on 2019-01-21.
 * @param <S> 状态
 * @param <E> 事件
 */
public interface Action<S, E> {

    /**
     * 执行动作，状态机不保证幂等，请在execute方法内自己实现业务幂等
     * 如果action成功返回则代表事件触发成功，否则请抛出异常
     * @param context 上下文
     */
    void execute(StateContext<S, E> context);

    /**
     * 从上下文中拿到StateMachineTask对象
     * @param context 上下文
     * @return task
     */
    default StateMachineTask getStateMachineTask(StateContext<S, E> context) {
        MessageHeaders headers = context.getMessage().getHeaders();
        return (StateMachineTask) headers.getHeader(TASK_HEADER);
    }

    /**
     * 添加信息到上下文中，会被序列化到数据库中的Response_data
     * @param context 上下文
     * @param key     key
     * @param value   value
     */
    default void addHeader(StateContext<S, E> context, String key, String value) {
        MessageHeaders headers = context.getMessage().getHeaders();
        headers.addHeader(key, value);
    }

    default Action<S, E> errorAction(Consumer<StateContext<S, E>> consumer) {
        return (s) -> {
            StateMachineTask task = getStateMachineTask(s);
            //如果是最后一次重试
            Exception e = s.getException();
            //非RetryException不重试
            if (e != null && e instanceof StateMachineRetryException) {
                if (task.isLastRetry()) {
                    consumer.accept(s);
                } else {
                    throw new StateMachineException(s.getException());
                }
            }
        };
    }
}