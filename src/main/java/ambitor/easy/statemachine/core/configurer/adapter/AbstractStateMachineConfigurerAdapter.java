package ambitor.easy.statemachine.core.configurer.adapter;


import ambitor.easy.statemachine.core.configurer.StateMachineConfigurer;
import ambitor.easy.statemachine.core.interceptor.StateMachineInterceptorConfigurer;
import ambitor.easy.statemachine.core.interceptor.StateMachineInterceptorList;
import ambitor.easy.statemachine.core.state.config.DefaultStateConfigurer;
import ambitor.easy.statemachine.core.state.config.StateConfigurer;
import ambitor.easy.statemachine.core.transition.config.BaseTransitionConfigurer;
import ambitor.easy.statemachine.core.transition.config.TransitionConfigurer;

/**
 * 抽象的状态机配置类
 * Created by Ambitor on 2019-01-21
 * @param <S> 状态
 * @param <E> 事件
 */
public abstract class AbstractStateMachineConfigurerAdapter<S, E> implements StateMachineConfigurer<S, E> {

    protected TransitionConfigurer<S, E> transitionConfigurer;

    protected StateConfigurer<S, E> stateConfigurer;

    protected StateMachineInterceptorConfigurer<S, E> interceptorConfigurer;

    private final Object lock = new Object();

    public final void init() {
        //初始化状态配置
        getStateMachineStateConfigurer();
        //初始化转换器配置
        getStateMachineTransitionConfigurer();
        //拦截器配置
        getStateMachineInterceptorConfigurer();
    }

    @Override
    public void configure(StateConfigurer<S, E> states) {
    }

    @Override
    public void configure(TransitionConfigurer<S, E> transitions) {
    }

    @Override
    public void configure(StateMachineInterceptorConfigurer<S, E> interceptors) {
    }

    protected final void getStateMachineTransitionConfigurer() {
        if (transitionConfigurer == null) {
            synchronized (lock) {
                if (transitionConfigurer == null) {
                    transitionConfigurer = new BaseTransitionConfigurer<>();
                    configure(transitionConfigurer);
                }
            }
        }
    }

    protected final void getStateMachineStateConfigurer() {
        if (stateConfigurer == null) {
            synchronized (lock) {
                if (stateConfigurer == null) {
                    stateConfigurer = new DefaultStateConfigurer<>();
                    configure(stateConfigurer);
                }
            }
        }
    }

    protected final void getStateMachineInterceptorConfigurer() {
        if (interceptorConfigurer == null) {
            synchronized (lock) {
                if (interceptorConfigurer == null) {
                    interceptorConfigurer = new StateMachineInterceptorList<>();
                    configure(interceptorConfigurer);
                }
            }
        }
    }

}
