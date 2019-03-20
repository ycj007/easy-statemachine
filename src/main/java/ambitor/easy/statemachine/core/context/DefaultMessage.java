package ambitor.easy.statemachine.core.context;

public class DefaultMessage<T> implements Message<T> {
    private T payload;
    private MessageHeaders headers;

    public DefaultMessage(T payload, MessageHeaders headers) {
        this.payload = payload;
        this.headers = headers;
    }

    @Override
    public T getPayload() {
        return payload;
    }

    @Override
    public MessageHeaders getHeaders() {
        return headers;
    }
}
