package ambitor.easy.statemachine.core.guard;


import ambitor.easy.statemachine.core.context.StateContext;

/**
 * 断言接口
 * Created by Ambitor on 2019/1/21
 * @param <S> 状态
 * @param <E> 事件
 */
public interface Guard<S, E> {

    boolean evaluate(StateContext<S, E> context);

}
