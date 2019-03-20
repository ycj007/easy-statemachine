package ambitor.easy.statemachine.core.interceptor;

import ambitor.easy.statemachine.core.StateMachine;
import ambitor.easy.statemachine.core.context.Message;
import ambitor.easy.statemachine.core.state.State;
import ambitor.easy.statemachine.core.transition.Transition;
import lombok.extern.slf4j.Slf4j;

/**
 * 状态机拦截器
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
@Slf4j
public class AbstractStateMachineInterceptor<S, E> implements StateMachineInterceptor<S, E> {
    @Override
    public Message<E> preEvent(Message<E> message, StateMachine<S, E> stateMachine) {
        log.info("preEvent In StateMachineInterceptor...");
        return message;
    }

    @Override
    public void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
        log.info("preStateChange In StateMachineInterceptor...");
    }

    @Override
    public void afterStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
        log.info("afterStateChange In StateMachineInterceptor...");
    }

    @Override
    public Exception stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
        log.info("stateMachineError In StateMachineInterceptor...");
        return exception;
    }
}
