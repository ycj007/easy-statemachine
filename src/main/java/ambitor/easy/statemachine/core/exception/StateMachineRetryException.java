package ambitor.easy.statemachine.core.exception;

/**
 * 可重试的异常
 * Created by Ambitor on 2019/3/19
 */
public class StateMachineRetryException extends StateMachineException {
    public StateMachineRetryException(int code) {
        super(code);
    }

    public StateMachineRetryException(int code, String message) {
        super(code, message);
    }

    public StateMachineRetryException(int code, boolean notPrintStack) {
        super(code, notPrintStack);
    }

    public StateMachineRetryException(int code, Throwable cause) {
        super(code, cause);
    }

    public StateMachineRetryException(int code, Object[] args) {
        super(code, args);
    }

    public StateMachineRetryException(String message) {
        super(message);
    }

    public StateMachineRetryException(String message, Object... params) {
        super(message, params);
    }

    public StateMachineRetryException(Throwable cause, String message, Object... params) {
        super(cause, message, params);
    }

    public StateMachineRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateMachineRetryException(Throwable cause) {
        super(cause);
    }

    protected StateMachineRetryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
