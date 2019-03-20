package ambitor.easy.statemachine.core.configurer.adapter;


import ambitor.easy.statemachine.core.interceptor.StateMachineInterceptorConfigurer;
import ambitor.easy.statemachine.core.state.config.StateConfigurer;
import ambitor.easy.statemachine.core.transition.config.TransitionConfigurer;

/**
 * 状态机配置适配器，外界实现状态机需要继承此适配器
 * Created by Ambitor on 2019-01-21
 * @param <S> 状态
 * @param <E> 事件
 */
public abstract class StateMachineConfigurerAdapter<S, E> extends AbstractStateMachineConfigurerAdapter<S, E> {

    public TransitionConfigurer<S, E> getTransitionConfigurer() {
        return transitionConfigurer;
    }

    public StateConfigurer<S, E> getStateConfigurer() {
        return stateConfigurer;
    }

    public StateMachineInterceptorConfigurer<S, E> getInterceptorConfigurer() {
        return interceptorConfigurer;
    }
}
