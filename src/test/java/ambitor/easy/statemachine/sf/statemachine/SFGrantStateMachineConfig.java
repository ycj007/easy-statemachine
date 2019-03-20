package ambitor.easy.statemachine.sf.statemachine;


import ambitor.easy.statemachine.core.annotation.EnableWithStateMachine;
import ambitor.easy.statemachine.core.configurer.adapter.StateMachineConfigurerAdapter;
import ambitor.easy.statemachine.core.interceptor.StateMachineInterceptorConfigurer;
import ambitor.easy.statemachine.core.state.config.StateConfigurer;
import ambitor.easy.statemachine.core.transition.config.TransitionConfigurer;
import ambitor.easy.statemachine.interceptor.PersistStateMachineInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ambitor.easy.statemachine.sf.action.SFCreateCardIIAction;
import ambitor.easy.statemachine.sf.action.SFDocumentCreditAction;
import ambitor.easy.statemachine.sf.action.SFFinishAction;
import ambitor.easy.statemachine.sf.action.SFGrantAction;
import ambitor.easy.statemachine.sf.enumerate.SFGrantEvent;
import ambitor.easy.statemachine.sf.enumerate.SFGrantState;
import ambitor.easy.statemachine.sf.guard.GrantGuard;

import java.util.EnumSet;

import static ambitor.easy.statemachine.sf.enumerate.GrantConstant.*;

@Slf4j
@EnableWithStateMachine
public class SFGrantStateMachineConfig extends StateMachineConfigurerAdapter<SFGrantState, SFGrantEvent> {

    @Override
    public void configure(StateConfigurer<SFGrantState, SFGrantEvent> states) {
        states
                .newStatesConfigurer()
                // 定义初始状态
                .initial(SFGrantState.WAIT_CREATE_CARDII)
                // 定义挂起状态，遇到此状态后状态机自动挂起，直到再次手动触发事件(回调中)，状态机继续往下执行
                .suspend(SFGrantState.WAIT_DOCUMENT_CREDIT_CALLBACK)
                // 放款校验状态
                .suspend(SFGrantState.WAIT_GRANT_CHECK)
                // 定义所有状态集合
                .states(EnumSet.allOf(SFGrantState.class))
                //定义结束状态
                .end(SFGrantState.CREATE_CARDII_FAILED)
                .end(SFGrantState.DOCUMENT_CREDIT_FAILED)
                .end(SFGrantState.GRANT_FAILED)
                .end(SFGrantState.GRANT_SUCCESS);
    }

    /**
     * 状态扭转器配置，配置工作流通过 event（触发事件）把状态从
     * source status（源状态) 转到 target status (目标状态)
     * 可根据上一步建档授信结果转换成不同的 target status (目标状态)
     * 具体配置情况如下
     */
    @Override
    public void configure(TransitionConfigurer<SFGrantState, SFGrantEvent> transitions) {
        transitions
                /**  1、等待创建二类户  **/
                //标准转换器，不具备结果判断，事件触发后只能从X状态转为Y状态
                .standardTransition()
                .source(SFGrantState.WAIT_CREATE_CARDII)
                .target(SFGrantState.WAIT_DOCUMENT_CREDIT)
                .event(SFGrantEvent.CREATE_CARDII)
                .action(sfCreateCardIIAction, sfCreateCardIIAction.errorAction(s -> log.info("创建二类户异常")))
                .and()
                /**  2、等待建档授信步骤  **/
                //具备选择结果的转换器，可根据当前事件执行结果扭转到不同状态
                .choiceTransition()
                //原状态为等待建档授信
                .source(SFGrantState.WAIT_DOCUMENT_CREDIT)
                //first相当于if，如果建档授信状态返回DOCUMENT_CREDIT_SUCCESS则转换成WAIT_GRANT等待放款
                .first(SFGrantState.WAIT_GRANT, GrantGuard.condition(DOCUMENT_CREDIT_STATUS, DOCUMENT_CREDIT_SUCCESS))
                //then相当于elseif，如果建档授信状态返回WAIT_DOCUMENT_CREDIT_CALLBACK则转换成等待建档授信回调
                .then(SFGrantState.WAIT_DOCUMENT_CREDIT_CALLBACK, GrantGuard.condition(DOCUMENT_CREDIT_STATUS, WAIT_DOCUMENT_CREDIT_CALLBACK))
                //last相当于else，如果都不是则返回建档授信失败
                .last(SFGrantState.DOCUMENT_CREDIT_FAILED)
                //触发事件
                .event(SFGrantEvent.DOCUMENT_CREDIT)
                //事件执行的action
                .action(sfDocumentCreditAction, sfDocumentCreditAction.errorAction())
                .and()
                /**  3、等待建档授信回调步骤  **/
                .choiceTransition()
                .source(SFGrantState.WAIT_DOCUMENT_CREDIT_CALLBACK)
                .first(SFGrantState.WAIT_GRANT, GrantGuard.condition(DOCUMENT_CREDIT_STATUS, DOCUMENT_CREDIT_SUCCESS))
                .last(SFGrantState.DOCUMENT_CREDIT_FAILED)
                .event(SFGrantEvent.DOCUMENT_CREDIT_CALLBACK)
                .and()
                /**  4、等待放款流程 **/
                .choiceTransition()
                .source(SFGrantState.WAIT_GRANT)
                .first(SFGrantState.GRANT_TASK_SAVE, GrantGuard.condition(GRANT_STATUS, GRANT_SUCCESS))
                .last(SFGrantState.WAIT_GRANT_CHECK)
                .event(SFGrantEvent.GRANTED)
                .action(sfGrantAction)
                .and()
                /** 5、放款检查流程，如果上一步操作超时 **/
                .choiceTransition()
                .source(SFGrantState.WAIT_GRANT_CHECK)
                .first(SFGrantState.GRANT_TASK_SAVE, GrantGuard.condition(GRANT_STATUS, GRANT_SUCCESS))
                .last(SFGrantState.GRANT_FAILED)
                .event(SFGrantEvent.GRANT_CHECKED)
                .and()
                /** 6、最后完成的流程 **/
                .standardTransition()
                .source(SFGrantState.GRANT_TASK_SAVE).target(SFGrantState.GRANT_SUCCESS)
                .event(SFGrantEvent.FINISHED)
                .action(sfFinishAction);
    }

    /**
     * 注册拦截器
     */
    @Override
    public void configure(StateMachineInterceptorConfigurer<SFGrantState, SFGrantEvent> interceptors) {
        //状态改变持久化到数据库拦截器
        interceptors.register(persistStateMachineInterceptor);
    }

    /**
     * 状态机名称
     */
    @Override
    public String getName() {
        return "SF";
    }

    @Autowired
    private PersistStateMachineInterceptor<SFGrantState, SFGrantEvent> persistStateMachineInterceptor;
    @Autowired
    private SFCreateCardIIAction sfCreateCardIIAction;
    @Autowired
    private SFDocumentCreditAction sfDocumentCreditAction;
    @Autowired
    private SFGrantAction sfGrantAction;
    @Autowired
    private SFFinishAction sfFinishAction;
}
