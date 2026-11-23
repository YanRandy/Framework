package randy.framework.model;

import java.util.List;
import java.lang.annotation.Annotation;
import java.util.Map;

import randy.framework.annotation.RAuthenticated;
import randy.framework.annotation.RRestApi;
import randy.framework.interceptor.RInterceptor;
/**
 * Ce qu'il faut savoir pour exécuter une route : classe + nom de méthode,
 * plus les infos lues au démarrage (auth requise, rôles, JSON ou non).
 * Valeur de la table des routes {@code Map<RUrlKey, RMapping>}.
 */
public class RMapping {
    private String className;
    private String method;

    // private boolean authRequired;
    // private List<String> roles = List.of();
    // private boolean restApi;

    private final Map<Class<? extends Annotation>, Annotation> annotations;
    private final List<RInterceptor> interceptors;

    public RMapping(String className, String method, Map<Class<? extends Annotation>, Annotation> annotations, List<RInterceptor> interceptors) {
        this.className = className;
        this.method = method;
        this.annotations = Map.copyOf(annotations);
        this.interceptors = List.copyOf(interceptors);
    }

    // public RMapping(String className, String method,
    //                 boolean authRequired, List<String> roles, boolean restApi,
    //                 Map<Class<? extends Annotation>, Annotation> annotations) {
    //     this(className, method, annotations);
    //     this.authRequired = authRequired;
    //     this.roles = roles;
    //     this.restApi = restApi;
    // }

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
        return has(RAuthenticated.class);
    }

    public List<String> getRoles() {
        RAuthenticated auth = get(RAuthenticated.class);
        return auth == null ? List.of() : List.of(auth.roles());
    }

    public boolean isRestApi() {
        return has(RRestApi.class);
    }

    public <A extends Annotation> A get(Class<A> type) {
        return type.cast(annotations.get(type));
    }

    public boolean has(Class <? extends Annotation> type) {
        return annotations.containsKey(type);
    }

    public List<RInterceptor> getInterceptors() {
        return interceptors;
    }
}
