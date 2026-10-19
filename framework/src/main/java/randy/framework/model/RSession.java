package randy.framework.model;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class RSession {
    private final HttpSession httpSession;
    private final Map<String, Object> cache;

    public RSession(HttpSession httpSession) {
        this.httpSession = httpSession;
        this.cache = new HashMap<>();
    }

    public void set(String key, Object value) {
        cache.put(key, value);                // cache local
        httpSession.setAttribute(key, value); // persistance Tomcat
    }

    public Object get(String key) {
        return cache.containsKey(key)
            ? cache.get(key)                  // cache d'abord
            : httpSession.getAttribute(key);  // sinon Tomcat
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) get(key);
    }

    public void remove(String key) {
        cache.remove(key);
        httpSession.removeAttribute(key);
    }

    public boolean has(String key) {
        return cache.containsKey(key)
            || httpSession.getAttribute(key) != null;
    }

    // accès direct à la Map pour manipulation si besoin
    public Map<String, Object> getCache() {
        return cache;
    }

    // synchro cache → HttpSession (si tu modifies le cache directement)
    public void flush() {
        cache.forEach(httpSession::setAttribute);
    }

    // charger tous les attributs Tomcat dans le cache local
    public void loadAll() {
        Enumeration<String> names = httpSession.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            cache.put(name, httpSession.getAttribute(name));
        }
    }

    public Map<String, Object> getAll() {
        Map<String, Object> merged = new HashMap<>();
        // d'abord les attributs Tomcat
        Enumeration<String> names = httpSession.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            merged.put(name, httpSession.getAttribute(name));
        }
        // le cache local écrase si conflit
        merged.putAll(cache);
        return merged;
    }

    public void clear() {
        cache.clear();
        getAll().keySet().forEach(httpSession::removeAttribute);
    }

    public void invalidate() {
        cache.clear();
        httpSession.invalidate();
    }

    public void regenerateId(HttpServletRequest req) {
        Map<String, Object> attrs = getAll();
        cache.clear();
        httpSession.invalidate();
        HttpSession newSession = req.getSession(true);
        attrs.forEach(newSession::setAttribute);
    }

    public String getId() {
        return httpSession.getId();
    }

    public boolean isNew() {
        return httpSession.isNew();
    }

    public long getCreationTime() {
        return httpSession.getCreationTime();
    }

    public long getLastAccessedTime() {
        return httpSession.getLastAccessedTime();
    }

    public void setMaxInactiveInterval(int seconds) {
        httpSession.setMaxInactiveInterval(seconds);
    }

    public HttpSession raw() {
        return httpSession;
    }
}