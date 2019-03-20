package ambitor.easy.statemachine.core.context;


import ambitor.easy.statemachine.core.state.State;
import ambitor.easy.statemachine.core.transition.Transition;

/**
 * 默认状态机上下文
 * Created by Ambitor on 2019-01-21
 * @param <S> 状态
 * @param <E> 事件
 */
public class DefaultStateContext<S, E> implements StateContext<S, E> {

    private final Message<E> message;
    private final Transition<S, E> transition;
    private final State<S, E> source;
    private final State<S, E> target;
    private Exception exception;

    /**
     * 构造函数
     * @param message    the message
     * @param transition the transition
     * @param source     the source
     * @param target    the target
     */
    public DefaultStateContext(Message<E> message, Transition<S, E> transition, State<S, E> source,
                               State<S, E> target) {
        this.message = message;
        this.transition = transition;
        this.source = source;
        this.target = target;
    }

    public DefaultStateContext(Message<E> message, Transition<S, E> transition, State<S, E> source,
                               State<S, E> target, Exception e) {
        this.message = message;
        this.transition = transition;
        this.source = source;
        this.target = target;
        this.exception = e;
    }

    @Override
    public E getEvent() {
        return message != null ? message.getPayload() : null;
    }

    @Override
    public Message<E> getMessage() {
        return message;
    }


    @Override
    public Transition<S, E> getTransition() {
        return transition;
    }

    @Override
    public State<S, E> getSource() {
        return source != null ? source : (transition != null ? transition.getSource() : null);
    }

    @Override
    public State<S, E> getTarget() {
        return target;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "DefaultStateContext [ message=" + message +
                ", transition=" + transition + ", source=" + source + ", target="
                + target + ", exception=" + exception + "]";
    }
}