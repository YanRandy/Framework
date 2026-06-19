package randy.framework.util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import randy.framework.annotation.UrlMapping;
import randy.framework.model.Mapping;

    /**
    * This class is intended to provide utility methods for the framework, such as:
    * - Scanning for classes with specific annotations (e.g., @Controller)
    * - Instantiating classes and managing dependencies
    * - Handling configuration and properties
    */
public class Utilitaire {
    /**
     * Scanne un package et retourne la liste des noms de classes annotées par @Controller
     */
    public static Map<String, Mapping> scanPaths(String packageToScan) {
        Map<String, Mapping> mappedUrl = new java.util.HashMap<>();
        ClassGraph cg = new ClassGraph().enableClassInfo().enableAnnotationInfo();
        
        if (packageToScan != null && !packageToScan.isBlank()) {
            cg.acceptPackages(packageToScan);
        }

        try (ScanResult scanResult = cg.scan()) {
            // On récupère toutes les classes qui portent notre annotation spécifique
            List<String> controllers = scanResult
                    .getClassesWithAnnotation("randy.framework.annotation.Controller")
                    .getNames(); // Retourne une List<String> des noms de classes
            for (String className : controllers) {
                try {
                    Class<?> clazz = Class.forName(className);
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(UrlMapping.class)) {
                            UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                            String url = urlMapping.value();
                            mappedUrl.put(url, new Mapping(className, method.getName()));
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Class not found: " + className);
                }
            }
        }
        return mappedUrl;
    }
}
