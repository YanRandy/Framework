package randy.framework.model;

import java.util.List;

public class RMapping {
    private String className;
    private String method;

    private boolean authRequired;
    private List<String> roles = List.of();
    private boolean restApi;

    public RMapping(String className, String method) {
        this.className = className;
        this.method = method;
    }

    public RMapping(String className, String method,
                   boolean authRequired, List<String> roles, boolean restApi) {
        this(className, method);
        this.authRequired = authRequired;
        this.roles = roles;
        this.restApi = restApi;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isAuthRequired() {
        return authRequired;
    }

    public List<String> getRoles() {
        return roles;
    }

    public boolean isRestApi() {
        return restApi;
    }
}