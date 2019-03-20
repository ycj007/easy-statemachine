package ambitor.easy.statemachine.core.state;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.context.Message;

import java.util.Collection;

/**
 * 默认的状态实体类
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public class DefaultState<S, E> implements State<S, E> {

    private S state;
    private boolean initial = false;
    private boolean suspend = false;
    private boolean end = false;
    private Collection<? extends Action<S, E>> entryActions;
    private Collection<? extends Action<S, E>> exitActions;

    public DefaultState(S state, boolean initial, boolean suspend, boolean end,
                        Collection<? extends Action<S, E>> entryActions,
                        Collection<? extends Action<S, E>> exitActions) {
        this.state = state;
        this.initial = initial;
        this.suspend = suspend;
        this.end = end;
        this.entryActions = entryActions;
        this.exitActions = exitActions;
    }


    /**
     * 发送事件的包装类.
     * @param event 事件包装类
     * @return 如果事件被接受返回true 否则false
     */
    @Override
    public boolean sendEvent(Message<E> event) {
        return false;
    }

    /**
     * 获取状态ID
     */
    @Override
    public S getId() {
        return state;
    }

    @Override
    public boolean isInitial() {
        return initial;
    }

    @Override
    public boolean isEnd() {
        return end;
    }

    /**
     * 是否为挂起状态
     */
    @Override
    public boolean isSuspend() {
        return suspend;
    }

    /**
     * 进入Actions
     * @return actions
     */
    @Override
    public Collection<? extends Action<S, E>> getEntryActions() {
        return entryActions;
    }

    /**
     * 所有退出的actions
     * @return actions
     */
    @Override
    public Collection<? extends Action<S, E>> getExitActions() {
        return exitActions;
    }
}
