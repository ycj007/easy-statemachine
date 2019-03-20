package ambitor.easy.statemachine.core.action;


import ambitor.easy.statemachine.core.context.DefaultStateContext;

/**
 * 转换操作，在执行某个转换时执行的具体操作
 * Created by Ambitor on 2019-01-21.
 */
public final class Actions {

    private Actions() {
        // 工具类
    }

    public static <S, E> Action<S, E> errorCallingAction(final Action<S, E> action, final Action<S, E> errorAction) {
        return context -> {
            try {
                action.execute(context);
            } catch (Exception exception) {
                try {
                    errorAction.execute(new DefaultStateContext<>(context.getMessage(), context.getTransition(), context.getSource(),
                            context.getTarget(), exception));
                } catch (Exception e) {
                    throw exception;
                }
            }
        };
    }
}
