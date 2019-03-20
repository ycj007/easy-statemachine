package ambitor.easy.statemachine.core.interceptor;


import ambitor.easy.statemachine.core.StateMachine;
import ambitor.easy.statemachine.core.context.Message;
import ambitor.easy.statemachine.core.state.State;
import ambitor.easy.statemachine.core.transition.Transition;

/**
 * 状态机拦截器
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public interface StateMachineInterceptor<S, E> {

    Message<E> preEvent(Message<E> message, StateMachine<S, E> stateMachine);

    void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
                        StateMachine<S, E> stateMachine);

    void afterStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
                          StateMachine<S, E> stateMachine);

    Exception stateMachineError(StateMachine<S, E> stateMachine, Exception exception);
}
