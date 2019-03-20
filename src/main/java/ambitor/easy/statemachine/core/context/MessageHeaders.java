package ambitor.easy.statemachine.core.context;

import java.util.HashMap;
import java.util.Map;

public class MessageHeaders {

    public MessageHeaders() {
        this.headers = new HashMap<>();
    }

    private Map<String, Object> headers;

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public Object getHeader(String key) {
        return headers.get(key);
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, Object value) {
        headers.put(key, value);
    }
}
