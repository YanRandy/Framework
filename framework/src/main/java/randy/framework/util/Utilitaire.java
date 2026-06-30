package randy.framework.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import randy.framework.annotation.Controller;
import randy.framework.annotation.UrlMapping;
import randy.framework.model.Mapping;
import randy.framework.model.UrlKey;

    /**
    * This class is intended to provide utility methods for the framework, such as:
    * - Scanning for classes with specific annotations (e.g., @Controller)
    * - Instantiating classes and managing dependencies
    * - Handling configuration and properties
    */
public class Utilitaire {
    /**
     * Scanne un package et retourne la liste des noms de classes annotées par @Controller
     * On met en <Urlkey, Mapping> pour que le code applique UrlKey comme une classe cle pour le hashmap et
     * ces fonctions
     */
    public static Map<UrlKey, Mapping> scanPaths(String packageToScan) {
        Map<UrlKey, Mapping> mappedUrl = new HashMap<>();
        ClassGraph cg = new ClassGraph().enableClassInfo().enableAnnotationInfo();
        
        if (packageToScan != null && !packageToScan.isBlank()) {
            cg.acceptPackages(packageToScan);
        }

        try (ScanResult scanResult = cg.scan()) {
            List<String> controllers = scanResult
                    .getClassesWithAnnotation(Controller.class.getName())
                    .getNames();
            for (String className : controllers) {
                try {
                    Class<?> clazz = Class.forName(className);
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(UrlMapping.class)) {
                            UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                            String[] httpMethods = urlMapping.method();
                            for (String httpMethod : httpMethods) {
                                UrlKey key = new UrlKey(urlMapping.value(), httpMethod);
                                // generalisation - doublon url
                            if (mappedUrl.containsKey(key)) {
                                Mapping conflit = mappedUrl.get(key);
                                throw new IllegalStateException("Erreur critique de routage : L'URL '" + key.getUrl() + 
                                    "' est déjà associée à " + conflit.getClassName() + "." + conflit.getMethod() + 
                                    "(). Conflit avec " + className + "." + method.getName() + "()");
                            }
                                mappedUrl.put(key, new Mapping(className, method.getName()));
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    // System.err.println("Class not found: " + className);
                    throw new RuntimeException("Class not found: " + className, e);
                }
            }
        }
        return mappedUrl;
    }
}
