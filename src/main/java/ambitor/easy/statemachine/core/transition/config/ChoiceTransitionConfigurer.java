package ambitor.easy.statemachine.core.transition.config;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.guard.Guard;

/**
 * 选择转换器配置
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public interface ChoiceTransitionConfigurer<S, E> extends TransitionConfigurer<S, E> {
    /**
     * 源状态
     * @param source 源状态S
     */
    ChoiceTransitionConfigurer<S, E> source(S source);

    /**
     * 触发状态扭转 Source -> Target 的事件
     * @param event 事件E
     */
    ChoiceTransitionConfigurer<S, E> event(E event);

    /**
     * 等同于if
     * @param guard  如果返回true
     * @param target 则状态变更成target
     * @return
     */
    ChoiceTransitionConfigurer<S, E> first(S target, Guard<S, E> guard);

    /**
     * 等同于 else if
     * @param guard  如果返回true
     * @param target 则状态变更成target
     * @return
     */
    ChoiceTransitionConfigurer<S, E> then(S target, Guard<S, E> guard);

    /**
     * 等同于 else
     * @param target 如果first then 都不使用，变更状态target
     */
    ChoiceTransitionConfigurer<S, E> last(S target);


    /**
     * action执行成功，状态扭转 source -> target
     * 如抛出异常则状态不会扭转
     * @param action 事件触发后执行的action
     */
    ChoiceTransitionConfigurer<S, E> action(Action<S, E> action);

    /**
     * action执行成功状态从 source -> target
     * 注意，如果有error函数，当action执行失败时会调用error，且如果error执行成功无异常后
     * 状态也会扭转 source -> target
     */
    ChoiceTransitionConfigurer<S, E> action(Action<S, E> action, Action<S, E> error);
}
