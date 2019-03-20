package ambitor.easy.statemachine.core.transition.config;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.exception.StateMachineException;

/**
 * 默认标准转换配置
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public class DefaultStandardTransitionConfigurer<S, E> extends BaseTransitionConfigurer<S, E>
        implements StandardTransitionConfigurer<S, E> {

    private S target;

    /**
     * 外部事件才会导致状态扭转
     * @param target
     */
    @Override
    public DefaultStandardTransitionConfigurer<S, E> target(S target) {
        setTarget(target);
        return this;
    }


    @Override
    public DefaultStandardTransitionConfigurer<S, E> source(S source) {
        setSource(source);
        return this;
    }

    @Override
    public DefaultStandardTransitionConfigurer<S, E> event(E event) {
        setEvent(event);
        return this;
    }

    @Override
    public DefaultStandardTransitionConfigurer<S, E> action(Action<S, E> action) {
        if (action == null) {
            throw new StateMachineException("action can not be null");
        }
        addAction(action);
        return this;
    }

    @Override
    public DefaultStandardTransitionConfigurer<S, E> action(Action<S, E> action, Action<S, E> error) {
        if (action == null) {
            throw new StateMachineException("action can not be null");
        }
        addAction(action, error);
        return this;
    }

    public S getTarget() {
        return target;
    }

    public void setTarget(S target) {
        this.target = target;
    }

}
