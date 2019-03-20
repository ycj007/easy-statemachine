package ambitor.easy.statemachine.core.transition;


import ambitor.easy.statemachine.core.action.Action;
import ambitor.easy.statemachine.core.guard.Guard;
import ambitor.easy.statemachine.core.state.State;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * 默认转换器
 * @param <S> 状态
 * @param <E> 事件
 */
@Slf4j
public class StandardTransition<S, E> extends AbstractTransition<S, E> {

    public StandardTransition(State<S, E> source, State<S, E> target, E event, Guard<S, E> guard, Collection<Action<S, E>> actions) {
        super(source, target, event, guard, actions);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
