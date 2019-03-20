package ambitor.easy.statemachine.core.context;


import ambitor.easy.statemachine.core.state.State;
import ambitor.easy.statemachine.core.transition.Transition;

/**
 * 状态上下文
 * Created by Ambitor on 2019-01-21
 * @param <S> 状态
 * @param <E> 事件
 */
public interface StateContext<S, E> {

    Message<E> getMessage();

    E getEvent();

    State<S, E> getSource();

    State<S, E> getTarget();

    Exception getException();

    void setException(Exception exception);

    Transition<S, E> getTransition();

}
