package ambitor.easy.statemachine.core.state.config;

import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.builder.ConfigurerBuilder;

import java.util.Collection;
import java.util.Set;

/**
 * 状态配置
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public interface StateConfigurer<S, E> extends ConfigurerBuilder<StateConfigurer<S, E>> {

    /**
     * 初始化状态
     * @param initial 初始化状态
     * @return this
     */
    StateConfigurer<S, E> initial(S initial);

    /**
     * 初始化状态，以及对应的action
     * @param initial 初始化状态
     * @param action  对应的action
     * @return this
     */
    StateConfigurer<S, E> initial(S initial, Action<S, E> action);

    /**
     * 初始化状态，以及对应的action
     * @param initial       初始化状态
     * @param initialAction 对应的action
     * @param initialError  出错的action
     * @return this
     */
    StateConfigurer<S, E> initial(S initial, Action<S, E> initialAction, Action<S, E> initialError);

    /**
     * 定义status
     * @param state 状态
     * @return this
     */
    StateConfigurer<S, E> state(S state);

    /**
     * 定义状态，进入以及退出action
     * @param state        the state
     * @param entryActions the state entry actions
     * @param exitActions  the state exit actions
     * @return this
     */
    StateConfigurer<S, E> state(S state, Collection<? extends Action<S, E>> entryActions,
                                Collection<? extends Action<S, E>> exitActions);

    /**
     * 挂起状态，此状态执行完Transition后会挂起状态机(请注意状态机只有恢复后才会继续执行，使用此状态请记得恢复状态机)
     * 使用场景：触发事件扭转状态后，需要回调才能继续往下走流程
     * @param state the state
     */
    StateConfigurer<S, E> suspend(S state);

    /**
     * 挂起状态，此状态执行完Transition后会挂起状态机(请注意状态机只有恢复后才会继续执行，使用此状态请记得恢复状态机)
     * 使用场景：触发事件扭转状态后，需要回调才能继续往下走流程
     * @param state the state
     * @param entry 进入该状态触发的action
     */
    StateConfigurer<S, E> suspendEntry(S state, Action<S, E> entry);

    /**
     * 挂起状态，此状态执行完Transition后会挂起状态机(请注意状态机只有恢复后才会继续执行，使用此状态请记得恢复状态机)
     * 使用场景：触发事件扭转状态后，需要回调才能继续往下走流程
     * @param state the state
     * @param entry 进入该状态触发的action
     * @param error entry 异常就调用
     */
    StateConfigurer<S, E> suspendEntry(S state, Action<S, E> entry, Action<S, E> error);

    /**
     * 挂起状态，此状态执行完Transition后会挂起状态机(请注意状态机只有恢复后才会继续执行，使用此状态请记得恢复状态机)
     * 使用场景：触发事件扭转状态后，需要回调才能继续往下走流程
     * @param state the state
     * @param exit 退出该状态触发的action
     */
    StateConfigurer<S, E> suspendExit(S state, Action<S, E> exit);

    /**
     * 挂起状态，此状态执行完Transition后会挂起状态机(请注意状态机只有恢复后才会继续执行，使用此状态请记得恢复状态机)
     * 使用场景：触发事件扭转状态后，需要回调才能继续往下走流程
     * @param state the state
     * @param exit 退出该状态触发的action
     * @param error exit 异常就调用
     */
    StateConfigurer<S, E> suspendExit(S state, Action<S, E> exit, Action<S, E> error);

    StateConfigurer<S, E> suspend(S state, Collection<? extends Action<S, E>> entryActions,
                                  Collection<? extends Action<S, E>> exitActions);

    /**
     * 定义状态，并且绑定进入前action
     * @param state  the state
     * @param action the state entry action
     * @return this
     */
    StateConfigurer<S, E> stateEntry(S state, Action<S, E> action);

    /**
     * 定义状态，并且绑定进入前action
     * @param state  the state
     * @param action the state entry action
     * @param error  callback when Exception
     * @return this
     */
    StateConfigurer<S, E> stateEntry(S state, Action<S, E> action, Action<S, E> error);

    /**
     * 定义状态，并且绑定退出时action
     * @param state  the state
     * @param action the state exit action
     * @return this
     */
    StateConfigurer<S, E> stateExit(S state, Action<S, E> action);

    /**
     * 定义状态，并且绑定退出时action
     * @param state  the state
     * @param action the state entry action
     * @param error  callback when Exception
     * @return this
     */
    StateConfigurer<S, E> stateExit(S state, Action<S, E> action, Action<S, E> error);

    /**
     * 定义所有状态
     * @param states the states
     * @return this
     */
    StateConfigurer<S, E> states(Set<S> states);

    /**
     * 结束状态，可以在任何状态时候被调用来标记结束
     * @param end the end state
     * @return this
     */
    StateConfigurer<S, E> end(S end);

    /**
     * 创建状态配置
     * @return this
     */
    StateConfigurer<S, E> newStatesConfigurer();
}
