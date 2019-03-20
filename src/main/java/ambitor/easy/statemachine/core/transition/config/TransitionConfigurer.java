package ambitor.easy.statemachine.core.transition.config;


import ambitor.easy.statemachine.core.builder.ConfigurerBuilder;

/**
 * 状态转换定义
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public interface TransitionConfigurer<S, E> extends ConfigurerBuilder<TransitionConfigurer<S, E>> {

    /**
     * 新建一个标准Transition配置
     */
    StandardTransitionConfigurer<S, E> standardTransition();

    /**
     * 新建一个选择Transition配置
     */
    ChoiceTransitionConfigurer<S, E> choiceTransition();


}
