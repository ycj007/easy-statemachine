package ambitor.easy.statemachine.core.state;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.context.Message;

import java.util.Collection;

/**
 * 状态接口
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public interface State<S, E> {

    /**
     * 发送事件的包装类.
     * @param event 事件包装类
     * @return 如果事件被接受返回true 否则false
     */
    boolean sendEvent(Message<E> event);

    /**
     * 获取状态ID
     */
    S getId();

    /**
     * 是否初始化状态
     */
    boolean isInitial();

    /**
     * 是否结束状态
     */
    boolean isEnd();

    /**
     * 是否为挂起状态
     */
    boolean isSuspend();

    /**
     * 进入Actions
     */
    Collection<? extends Action<S, E>> getEntryActions();

    /**
     * 所有退出的actions
     * @return actions
     */
    Collection<? extends Action<S, E>> getExitActions();
}