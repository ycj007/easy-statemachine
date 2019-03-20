package ambitor.easy.statemachine.core.transition.config;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.action.Actions;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 转换器基类
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public class BaseTransitionConfigurer<S, E> implements TransitionConfigurer<S, E> {
    private S source;
    private E event;
    //如果true则为下一个configurer，复制给next
    private boolean and = true;
    private TransitionConfigurer<S, E> next;
    private final Collection<Action<S, E>> actions = new ArrayList<>();

    public TransitionConfigurer<S, E> getNext() {
        return next;
    }

    public void setNext(TransitionConfigurer<S, E> next) {
        this.next = next;
    }

    public void setAnd(boolean and) {
        this.and = and;
    }

    public boolean isAnd() {
        return and;
    }

    @Override
    public StandardTransitionConfigurer<S, E> standardTransition() {
        //如果有用过and拼接，则设置成下一个config
        StandardTransitionConfigurer<S, E> transitionConfigurer = new DefaultStandardTransitionConfigurer<>();
        if (isAnd()) {
            setNext(transitionConfigurer);
            setAnd(false);
        }
        return transitionConfigurer;
    }

    public ChoiceTransitionConfigurer<S, E> choiceTransition() {
        //如果有用过and拼接，则设置成下一个config
        ChoiceTransitionConfigurer<S, E> transitionConfigurer = new DefaultChoiceTransitionConfigurer<>();
        if (isAnd()) {
            setNext(transitionConfigurer);
            setAnd(false);
        }
        return transitionConfigurer;
    }

    /**
     * 拼接方法
     * @return 需要拼接的类
     */
    @Override
    public TransitionConfigurer<S, E> and() {
        and = true;
        return this;
    }

    public S getSource() {
        return source;
    }

    public E getEvent() {
        return event;
    }

    public void setSource(S source) {
        this.source = source;
    }

    public void setEvent(E event) {
        this.event = event;
    }

    public Collection<Action<S, E>> getActions() {
        return actions;
    }

    protected void addAction(Action<S, E> action, Action<S, E> error) {
        actions.add(error != null ? Actions.errorCallingAction(action, error) : action);
    }

    protected void addAction(Action<S, E> action) {
        addAction(action, null);
    }

}
