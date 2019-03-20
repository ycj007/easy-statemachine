package ambitor.easy.statemachine.core;

import ambitor.easy.statemachine.core.context.Message;
import ambitor.easy.statemachine.core.context.MessageHeaders;
import ambitor.easy.statemachine.core.interceptor.StateMachineInterceptorList;
import ambitor.easy.statemachine.core.state.State;
import ambitor.easy.statemachine.core.transition.Transition;

import java.util.Collection;
import java.util.Map;

/**
 * 状态机接口
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public interface StateMachine<S, E> {
    /**
     * 初始化状态
     */
    State<S, E> getInitialState();

    /**
     * 触发事件
     * @param event Message<E>
     * @return 状态机是否接受事件
     */
    boolean sendEvent(Message<E> event);

    /**
     * 触发事件
     * @param event E
     * @return 状态机是否接受事件
     */
    boolean sendEvent(E event);

    /**
     * 开始执行状态机，并自动事件驱动所有状态扭转，直到有事件不被接受或事件中发生异常
     */
    boolean start();

    /**
     * 开始执行状态机，并自动事件驱动所有状态扭转，直到有事件不被接受或事件中发生异常
     * @param headers 传入参数，可在触发事件中使用
     */
    boolean start(MessageHeaders headers);

    /**
     * 获取状态机当前状态
     * @return S
     */
    State<S, E> getState();

    /**
     * 获取当前事件
     */
    Message<E> getEvent();

    /**
     * 重置状态机当前状态
     * @param newState S
     */
    void resetStateMachine(S newState);

    /**
     * 获取状态机所有状态集合
     */
    Collection<State<S, E>> getStates();

    /**
     * 获取状态机所有转换器
     */
    Map<S, Collection<Transition<S, E>>> getTransitions();

    /**
     * 当前转换器
     */
    Transition<S, E> transition();

    /**
     * 状态机是否完成，如果有异常不接受事件或扭转到end状态 都是完成
     */
    boolean isComplete();

    /**
     * 获取状态机所有拦截器
     * @return
     */
    StateMachineInterceptorList<S, E> interceptors();

    /**
     * 设置状态机异常
     */
    void setStateMachineError(Exception exception);

    /**
     * 状态机是否有异常
     */
    Exception getStateMachineError();

}
