package ambitor.easy.statemachine.core.exception;

import java.text.MessageFormat;

/**
 * 状态机异常
 * Created by Ambitor on 2019/1/21 
 */
public class StateMachineException extends RuntimeException {
    static final long serialVersionUID = -7034897190745766939L;
    private int code = -1;
    private Object[] args;
    private boolean notPrintStack;

    public StateMachineException(int code) {
        this.code = code;
    }

    public StateMachineException(int code, String message) {
        super(message);
        this.code = code;
    }

    public StateMachineException(int code, boolean notPrintStack) {
        this.notPrintStack = notPrintStack;
        this.code = code;
    }

    public StateMachineException(int code, Throwable cause) {
        super(null, cause);
        this.code = code;
    }

    public StateMachineException(int code, Object[] args) {
        this.code = code;
        this.args = args;
    }

    public StateMachineException(String message) {
        super(message);
    }

    public StateMachineException(String message, Object... params) {
        super(MessageFormat.format(message, params));
    }

    public StateMachineException(Throwable cause, String message, Object... params) {
        super(MessageFormat.format(message, params), cause);
    }

    public StateMachineException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateMachineException(Throwable cause) {
        super(cause);
    }

    protected StateMachineException(String message, Throwable cause,
                                    boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }

    public boolean isNotPrintStack() {
        return notPrintStack;
    }
}
