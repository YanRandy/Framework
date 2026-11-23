package randy.framework.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class RApiResponse {

    private final int status;
    private final Map<String, Object> body = new LinkedHashMap<>();

    private RApiResponse(int status, boolean success) {
        this.status = status;
        body.put("success", success);
    }

    public static RApiResponse ok() {
        return new RApiResponse(200, true);
    }

    public static RApiResponse ok(String key, Object data) {
        return ok().with(key, data);
    }

    public static RApiResponse error(String message) {
        return error(400, message);
    }

    public static RApiResponse error(int status, String message) {
        return new RApiResponse(status, false).with("message", message);
    }

    public RApiResponse with(String key, Object value) {
        body.put(key, value);
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, Object> getBody() {
        return body;
    }
}
