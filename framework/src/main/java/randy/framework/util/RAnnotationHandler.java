package randy.framework.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import randy.framework.annotation.RAuthenticated;
import randy.framework.annotation.RController;
import randy.framework.annotation.RHandledBy;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.auth.RAuthSecurity;
import randy.framework.interceptor.RInterceptor;
import randy.framework.model.RMapping;
import randy.framework.model.RUrlKey;

public class RAnnotationHandler {
    /**
     * Scanne un package et retourne la liste des noms de classes annotées par @Controller et @UrlMapping
     * On met en <Urlkey, Mapping> pour que le code applique UrlKey comme une classe cle pour le hashmap
     * et ces fonctions
     * Les infos @RAuthenticated (méthode, sinon classe) et @RRestApi sont lues ici une seule fois,
     * puis gardées dans le RMapping pour ne pas refaire de réflexion à chaque requête.
     */
    public static void scanPaths(String packageToScan, Map<RUrlKey, RMapping> map) {
        ClassGraph cg = new ClassGraph().enableClassInfo().enableAnnotationInfo();
        Map<Class<? extends RInterceptor>, RInterceptor> instances = new HashMap<>();
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
                            // RAuthenticated auth = RAuthSecurity.resolveAuth(clazz, method);
                            // boolean authRequired = auth != null;
                            // List<String> roles = authRequired ? List.of(auth.roles()) : List.of();
                            // boolean restApi = method.isAnnotationPresent(RRestApi.class);
                            String[] httpMethods = urlMapping.method();

                            Map<Class<? extends Annotation>, Annotation> annots = new HashMap<>();
                            // remplissage des maps avec RMapping une seule fois
                            for (Annotation a : clazz.getAnnotations())  if (isFrameworkAnnotation(a)) annots.put(a.annotationType(), a);
                            for (Annotation a : method.getAnnotations()) if (isFrameworkAnnotation(a)) annots.put(a.annotationType(), a);

                            List<RHandledBy> handlers = new ArrayList<>();
                            for (Annotation a : annots.values()) {
                                RHandledBy h = a.annotationType().getAnnotation(RHandledBy.class);
                                if (h != null) handlers.add(h);
                            }
                            handlers.sort(Comparator.comparingInt(RHandledBy::order));   // le plus petit d'abord

                            List<RInterceptor> interceptors = new ArrayList<>();
                            for (RHandledBy h : handlers) {
                                RInterceptor i = instances.computeIfAbsent(h.value(), RAnnotationHandler::createInterceptor);
                                if (!interceptors.contains(i)) interceptors.add(i);   // pas deux fois le même
                            }

                            for (String httpMethod : httpMethods) {
                                RUrlKey key = new RUrlKey(urlMapping.value(), httpMethod);
                                // generalisation - doublon url
                            if (map.containsKey(key)) {
                                RMapping conflit = map.get(key);
                                throw new IllegalStateException("Erreur critique de routage : L'URL '" + key.getUrl() +
                                    "' est déjà associée à " + conflit.getClassName() + "." + conflit.getMethod() +
                                    "(). Conflit avec " + className + "." + method.getName() + "()");
                            }
                            map.put(key, new RMapping(className, method.getName(), annots, interceptors));
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
    // utile pour dire au scanpaths ou est ce qu'il doit analyser les annotations sincerement
    private static boolean isFrameworkAnnotation(Annotation a) {
        Class<? extends Annotation> type = a.annotationType();
            return type.getPackageName().equals("randy.framework.annotation")
                || type.isAnnotationPresent(RHandledBy.class);
    }

    private static RInterceptor createInterceptor(Class<? extends RInterceptor> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("L'intercepteur " + type.getName()
                    + " doit avoir un constructeur public sans argument.", e);
        }
    }
}
