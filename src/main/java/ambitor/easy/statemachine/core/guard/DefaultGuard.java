package ambitor.easy.statemachine.core.guard;

import ambitor.easy.statemachine.core.context.StateContext;

/**
 * 默认的校验
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public class DefaultGuard<S, E> implements Guard<S, E> {
    @Override
    public boolean evaluate(StateContext<S, E> context) {
        return true;
    }
}
