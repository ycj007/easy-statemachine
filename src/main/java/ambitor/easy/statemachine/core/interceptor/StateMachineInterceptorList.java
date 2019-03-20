package ambitor.easy.statemachine.core.interceptor;


import ambitor.easy.statemachine.core.StateMachine;
import ambitor.easy.statemachine.core.context.Message;
import ambitor.easy.statemachine.core.state.State;
import ambitor.easy.statemachine.core.transition.Transition;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 状态机
 * @param <S> 状态
 * @param <E> 事件
 */
public class StateMachineInterceptorList<S, E> implements StateMachineInterceptorConfigurer<S, E> {

    private final List<StateMachineInterceptor<S, E>> interceptors = new CopyOnWriteArrayList<StateMachineInterceptor<S, E>>();

    public boolean set(List<StateMachineInterceptor<S, E>> interceptors) {
        synchronized (this.interceptors) {
            this.interceptors.clear();
            return this.interceptors.addAll(interceptors);
        }
    }

    public boolean add(StateMachineInterceptor<S, E> interceptor) {
        return interceptors.add(interceptor);
    }

    public boolean remove(StateMachineInterceptor<S, E> interceptor) {
        return interceptors.remove(interceptor);
    }

    public Message<E> preEvent(Message<E> message, StateMachine<S, E> stateMachine) {
        for (StateMachineInterceptor<S, E> interceptor : interceptors) {
            if ((message = interceptor.preEvent(message, stateMachine)) == null) {
                break;
            }
        }
        return message;
    }

    /**
     * Pre state change.
     * @param state        the state
     * @param message      the message
     * @param transition   the transition
     * @param stateMachine the state machine
     */
    public void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
                               StateMachine<S, E> stateMachine) {
        for (StateMachineInterceptor<S, E> interceptor : interceptors) {
            interceptor.preStateChange(state, message, transition, stateMachine);
        }
    }

    /**
     * after state change.
     * @param state        the state
     * @param message      the message
     * @param transition   the transition
     * @param stateMachine the state machine
     */
    public void afterStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
                                 StateMachine<S, E> stateMachine) {
        for (StateMachineInterceptor<S, E> interceptor : interceptors) {
            interceptor.afterStateChange(state, message, transition, stateMachine);
        }
    }

    /**
     * State machine error.
     * @param stateMachine the state machine
     * @param exception    the exception
     * @return the exception
     */
    public void stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
        for (StateMachineInterceptor<S, E> interceptor : interceptors) {
            if ((exception = interceptor.stateMachineError(stateMachine, exception)) == null) {
                break;
            }
        }
    }

    @Override
    public boolean register(StateMachineInterceptor<S, E> interceptor) {
        return add(interceptor);
    }
}
