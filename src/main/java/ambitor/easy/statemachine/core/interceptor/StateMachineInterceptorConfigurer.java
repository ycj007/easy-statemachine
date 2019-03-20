package ambitor.easy.statemachine.core.interceptor;

/**
 * 状态机拦截器配置
 * @param <S> 状态
 * @param <E> 事件
 */
public interface StateMachineInterceptorConfigurer<S, E> {

    boolean register(StateMachineInterceptor<S, E> interceptor);
}
