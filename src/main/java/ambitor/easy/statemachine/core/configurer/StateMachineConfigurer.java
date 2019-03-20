package ambitor.easy.statemachine.core.configurer;

import ambitor.easy.statemachine.core.interceptor.StateMachineInterceptorConfigurer;
import ambitor.easy.statemachine.core.state.config.StateConfigurer;
import ambitor.easy.statemachine.core.transition.config.TransitionConfigurer;

/**
 * 状态机配置
 * Created by Ambitor on 2019-01-21
 * @param <S> 状态
 * @param <E> 事件
 */
public interface StateMachineConfigurer<S, E> {
    /**
     * 状态机名称
     */
    String getName();
    /**
     * 状态配置
     * @param states 状态
     * @throws Exception
     */
    void configure(StateConfigurer<S, E> states);

    /**
     * 转换配置
     * @param transitions 转换
     * @throws Exception
     */
    void configure(TransitionConfigurer<S, E> transitions);

    /**
     * 配置拦截器
     * @param interceptors 拦截器
     */
    void configure(StateMachineInterceptorConfigurer<S, E> interceptors);
}
