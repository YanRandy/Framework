package randy.framework.util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import randy.framework.annotation.RAuthenticated;
import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.auth.RAuthSecurity;
import randy.framework.model.RMapping;
import randy.framework.model.RUrlKey;

public class RAnnotationHandler {
    /**
     * Scanne un package et retourne la liste des noms de classes annotées par @Controller et @UrlMapping
     * On met en <Urlkey, Mapping> pour que le code applique UrlKey comme une classe cle pour le hashmap
     * et ces fonctions
     */
    public static void scanPaths(String packageToScan, Map<RUrlKey, RMapping> map) {
        ClassGraph cg = new ClassGraph().enableClassInfo().enableAnnotationInfo();
        
        if (packageToScan != null && !packageToScan.isBlank()) {
            cg.acceptPackages(packageToScan);
        }

        try (ScanResult scanResult = cg.scan()) {
            List<String> controllers = scanResult
                    .getClassesWithAnnotation(RController.class.getName())
                    .getNames();
            for (String className : controllers) {
                try {
                    Class<?> clazz = Class.forName(className);
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(RUrlMapping.class)) {
                            RUrlMapping urlMapping = method.getAnnotation(RUrlMapping.class);
                            RAuthenticated auth = RAuthSecurity.resolveAuth(clazz, null);
                            boolean authRequired = auth != null;
                            List<String> roles = authRequired ? List.of(auth.roles()) : List.of();
                            boolean restApi = method.isAnnotationPresent(RRestApi.class);
                            String[] httpMethods = urlMapping.method();
                            for (String httpMethod : httpMethods) {
                                RUrlKey key = new RUrlKey(urlMapping.value(), httpMethod);
                                // generalisation - doublon url
                            if (map.containsKey(key)) {
                                RMapping conflit = map.get(key);
                                throw new IllegalStateException("Erreur critique de routage : L'URL '" + key.getUrl() + 
                                    "' est déjà associée à " + conflit.getClassName() + "." + conflit.getMethod() + 
                                    "(). Conflit avec " + className + "." + method.getName() + "()");
                            }
                            map.put(key, new RMapping(className, method.getName(), authRequired, roles, restApi));
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    // System.err.println("Class not found: " + className);
                    throw new RuntimeException("Class not found: " + className, e);
                }
            }
        }
    }
}
