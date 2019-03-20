package ambitor.easy.statemachine.core.transition.config;


import ambitor.easy.statemachine.core.action.Action;

/**
 * 标准转换器
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public interface StandardTransitionConfigurer<S, E> extends TransitionConfigurer<S, E> {
    /**
     * 源状态
     * @param source 源状态S
     */
    StandardTransitionConfigurer<S, E> source(S source);

    /**
     * 目标状态
     * @param target 目标状态S
     */
    StandardTransitionConfigurer<S, E> target(S target);

    /**
     * 触发状态扭转 Source -> Target 的事件
     * @param event 事件E
     */
    StandardTransitionConfigurer<S, E> event(E event);

    /**
     * action执行成功，状态扭转 source -> target
     * 如抛出异常则状态不会扭转
     * @param action 事件触发后执行的action
     */
    StandardTransitionConfigurer<S, E> action(Action<S, E> action);

    /**
     * action执行成功状态从 source -> target
     * 注意，如果有error函数，当action执行失败时会调用error，且如果error执行成功无异常后
     * 状态也会扭转 source -> target
     */
    StandardTransitionConfigurer<S, E> action(Action<S, E> action, Action<S, E> error);
}
