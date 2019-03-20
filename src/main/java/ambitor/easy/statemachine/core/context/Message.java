package ambitor.easy.statemachine.core.context;

public interface Message<T> {

    T getPayload();

    MessageHeaders getHeaders();
}