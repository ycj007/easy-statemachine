package ambitor.easy.statemachine.core.transition;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.context.StateContext;
import ambitor.easy.statemachine.core.guard.Guard;
import ambitor.easy.statemachine.core.state.State;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * 转换器抽象类
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
@Slf4j
public abstract class AbstractTransition<S, E> implements Transition<S, E> {

    private final State<S, E> source;
    private final State<S, E> target;
    private final Guard<S, E> guard;
    private final E event;
    protected final Collection<Action<S, E>> actions;


    /**
     * Instantiates a new abstract transition.
     * @param source  the source
     * @param event   the event
     * @param actions ths actions
     */
    public AbstractTransition(State<S, E> source, State<S, E> target, E event, Guard<S, E> guard, Collection<Action<S, E>> actions) {
        this.source = source;
        this.target = target;
        this.event = event;
        this.guard = guard;
        this.actions = actions;
    }

    @Override
    public State<S, E> getSource() {
        return source;
    }

    @Override
    public State<S, E> getTarget() {
        return target;
    }

    @Override
    public boolean transit(StateContext<S, E> context) {
        executeTransitionActions(context);
        return context.getException() == null;
    }

    @Override
    public final void executeTransitionActions(StateContext<S, E> context) {
        if (actions == null) {
            return;
        }
        for (Action<S, E> action : actions) {
            try {
                action.execute(context);
            } catch (Exception e) {
                context.setException(e);
                log.error("Action执行结束，发生异常 Source-->{} ,Target-->{} ,Event->{}",
                        context.getSource().getId(), context.getTarget().getId(), context.getEvent(), e);
                return;
            }
        }
    }

    @Override
    public Guard<S, E> guard() {
        return guard;
    }

    @Override
    public E getEvent() {
        return event;
    }

    @Override
    public Collection<Action<S, E>> getActions() {
        return actions;
    }

    @Override
    public int hashCode() {
        return getSource().hashCode() + getTarget().hashCode() + getEvent().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractTransition)) {
            return false;
        }
        AbstractTransition o = (AbstractTransition) obj;
        return this.getSource().equals(o.getSource()) &&
                this.getTarget().equals(o.getTarget()) &&
                this.getEvent().equals(o.getEvent());
    }

    @Override
    public String toString() {
        return "AbstractTransition [source=" + getSource() + ", target=" + target + "]";
    }

}