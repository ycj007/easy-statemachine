package ambitor.easy.statemachine.core;


import ambitor.easy.statemachine.core.interceptor.StateMachineInterceptorList;
import ambitor.easy.statemachine.core.state.State;
import ambitor.easy.statemachine.core.transition.Transition;

import java.util.Collection;
import java.util.Map;

/**
 * 默认状态机
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public class DefaultStateMachine<S, E> extends AbstractStateMachine<S, E> {

    public DefaultStateMachine(Map<S, State<S, E>> states, Map<S, Collection<Transition<S, E>>> transitions, State<S, E> initialState, State<S, E> currentState, Exception currentError, StateMachineInterceptorList<S, E> interceptors) {
        super(states, transitions, initialState, currentState, currentError, interceptors);
    }
}
