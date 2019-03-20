package ambitor.easy.statemachine.core.state.config;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.action.Actions;
import ambitor.easy.statemachine.core.exception.StateMachineException;
import ambitor.easy.statemachine.core.state.DefaultState;
import ambitor.easy.statemachine.core.state.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 状态机状态配置类
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public class DefaultStateConfigurer<S, E> implements StateConfigurer<S, E> {

    S initialState;
    final Collection<S> ends = new ArrayList<>();
    final Map<S, State<S, E>> stateMaps = new TreeMap<>();

    /**
     * 创建状态配置
     * @return this
     */
    @Override
    public StateConfigurer<S, E> newStatesConfigurer() {
        return this;
    }

    /**
     * 初始化状态
     * @param initialState 初始化状态
     * @return this
     */
    @Override
    public StateConfigurer<S, E> initial(S initialState) {
        return initial(initialState, null, null);
    }

    /**
     * 初始化状态，以及对应的action
     * @param initial 初始化状态
     * @param action  对应的action
     * @return this
     */
    @Override
    public StateConfigurer<S, E> initial(S initial, Action<S, E> action) {
        return initial(initial, action, null);
    }

    /**
     * 初始化状态，以及对应的action
     * @param initial       初始化状态
     * @param initialAction 对应的action
     * @return this
     */
    @Override
    public StateConfigurer<S, E> initial(S initial, Action<S, E> initialAction, Action<S, E> initialError) {
        Collection<Action<S, E>> exitActions = null;
        if (initialAction != null) {
            exitActions = new ArrayList<>(1);
            exitActions.add(initialError != null ? Actions.errorCallingAction(initialAction, initialError) : initialAction);
        }
        addState(initial, true, false, false, null, exitActions);
        this.initialState = initial;
        return this;
    }

    /**
     * 定义status
     * @param state 状态
     * @return this
     */
    @Override
    public StateConfigurer<S, E> state(S state) {
        addState(state, false, false, false, null, null);
        return this;
    }

    /**
     * 定义状态，进入以及退出action
     * @param state        the state
     * @param entryActions the state entry actions
     * @param exitActions  the state exit actions
     * @return this
     */
    @Override
    public StateConfigurer<S, E> state(S state, Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions) {
        addState(state, false, false, false, entryActions, exitActions);
        return this;
    }

    /**
     * 定义状态，并且绑定进入前action
     * @param state the state
     * @param entry the state entry action
     * @return this
     */
    @Override
    public StateConfigurer<S, E> stateEntry(S state, Action<S, E> entry) {
        stateEntry(state, entry, null);
        return this;
    }

    /**
     * 定义状态，并且绑定进入前action
     * @param state the state
     * @param entry the state entry action
     * @param error callback when Exception
     * @return this
     */
    @Override
    public StateConfigurer<S, E> stateEntry(S state, Action<S, E> entry, Action<S, E> error) {
        Collection<Action<S, E>> entryActions = null;
        if (entry != null) {
            entryActions = new ArrayList<>(1);
            entryActions.add(error != null ? Actions.errorCallingAction(entry, error) : entry);
        }
        addState(state, false, false, false, entryActions, null);
        return this;
    }

    /**
     * 定义状态，并且绑定退出时action
     * @param state the state
     * @param exit  the state exit action
     * @return this
     */
    @Override
    public StateConfigurer<S, E> stateExit(S state, Action<S, E> exit) {
        stateExit(state, exit, null);
        return this;
    }

    /**
     * 定义状态，并且绑定退出时action
     * @param state the state
     * @param exit  the state exit action
     * @param error callback when Exception
     * @return this
     */
    @Override
    public StateConfigurer<S, E> stateExit(S state, Action<S, E> exit, Action<S, E> error) {
        Collection<Action<S, E>> exitActions = null;
        if (exit != null) {
            exitActions = new ArrayList<>(1);
            exitActions.add(error != null ? Actions.errorCallingAction(exit, error) : exit);
        }
        return state(state, null, exitActions);
    }

    /**
     * 挂起状态，此状态执行完Transition后会挂起状态机(请注意状态机只有恢复后才会继续执行，使用此状态请记得恢复状态机)
     * 使用场景：触发事件扭转状态后，需要回调才能继续往下走流程
     * @param state the state
     */
    @Override
    public StateConfigurer<S, E> suspend(S state) {
        addState(state, false, true, false, null, null);
        return this;
    }

    /**
     * 挂起状态，此状态执行完Transition后会挂起状态机(请注意状态机只有恢复后才会继续执行，使用此状态请记得恢复状态机)
     * 使用场景：触发事件扭转状态后，需要回调才能继续往下走流程
     * @param state the state
     * @param entry 进入该状态触发的action
     */
    @Override
    public StateConfigurer<S, E> suspendEntry(S state, Action<S, E> entry) {
        suspendEntry(state, entry, null);
        return this;
    }

    /**
     * 挂起状态，此状态执行完Transition后会挂起状态机(请注意状态机只有恢复后才会继续执行，使用此状态请记得恢复状态机)
     * 使用场景：触发事件扭转状态后，需要回调才能继续往下走流程
     * @param state the state
     * @param entry 进入该状态触发的action
     * @param error entry 异常就调用
     */
    @Override
    public StateConfigurer<S, E> suspendEntry(S state, Action<S, E> entry, Action<S, E> error) {
        Collection<Action<S, E>> entryActions = null;
        if (entry != null) {
            entryActions = new ArrayList<>(1);
            entryActions.add(error != null ? Actions.errorCallingAction(entry, error) : entry);
        }
        addState(state, false, true, false, entryActions, null);
        return this;
    }

    /**
     * 挂起状态，此状态执行完Transition后会挂起状态机(请注意状态机只有恢复后才会继续执行，使用此状态请记得恢复状态机)
     * 使用场景：触发事件扭转状态后，需要回调才能继续往下走流程
     * @param state the state
     * @param exit  退出该状态触发的action
     */
    @Override
    public StateConfigurer<S, E> suspendExit(S state, Action<S, E> exit) {
        suspendExit(state, exit, null);
        return this;
    }

    /**
     * 挂起状态，此状态执行完Transition后会挂起状态机(请注意状态机只有恢复后才会继续执行，使用此状态请记得恢复状态机)
     * 使用场景：触发事件扭转状态后，需要回调才能继续往下走流程
     * @param state the state
     * @param exit  退出该状态触发的action
     * @param error exit 异常就调用
     */
    @Override
    public StateConfigurer<S, E> suspendExit(S state, Action<S, E> exit, Action<S, E> error) {
        Collection<Action<S, E>> exitActions = null;
        if (exit != null) {
            exitActions = new ArrayList<>(1);
            exitActions.add(error != null ? Actions.errorCallingAction(exit, error) : exit);
        }
        return state(state, null, exitActions);
    }

    @Override
    public StateConfigurer<S, E> suspend(S state, Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions) {
        addState(state, false, true, false, entryActions, exitActions);
        return null;
    }

    /**
     * 定义所有状态
     * @param states the states
     * @return this
     */
    @Override
    public StateConfigurer<S, E> states(Set<S> states) {
        for (S state : states) {
            State<S, E> exist = stateMaps.get(state);
            if (exist == null) {
                state(state);
            }
        }
        return this;
    }

    /**
     * 拼接方法
     * @return 需要拼接的类
     */
    @Override
    public StateConfigurer<S, E> and() {
        throw new StateMachineException("暂时不支持");
    }

    /**
     * 结束状态，可以在任何状态时候被调用来标记结束
     * @param end the end state
     * @return this
     */
    @Override
    public StateConfigurer<S, E> end(S end) {
        ends.add(end);
        addState(end, false, false, true, null, null);
        return this;
    }

    private void addState(S state, boolean initial, boolean suspend, boolean end,
                          Collection<? extends Action<S, E>> entryActions,
                          Collection<? extends Action<S, E>> exitActions) {
        State<S, E> stateData = new DefaultState<>(state, initial, suspend, end, entryActions, exitActions);
        stateMaps.put(state, stateData);
    }

    public Collection<S> getEnds() {
        return ends;
    }

    public Map<S, State<S, E>> getStateMaps() {
        return stateMaps;
    }

    public S getInitialState() {
        return initialState;
    }
}
