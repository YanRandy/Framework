package randy.framework.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RControllerRegistry {
    private final Map<String, Object> instances = new ConcurrentHashMap<>();
    
    public void register(String className) {
        instances.computeIfAbsent(className, RControllerRegistry::create);
    }

    // la meme instance du controller appeler
    public Object get(String className) {
        return instances.computeIfAbsent(className, RControllerRegistry::create);
    }

    public int size() {
        return instances.size();
    }

    private static Object create(String className) {
        try {
            return Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de créer une instance de " + className, e);
        }
    }
}
