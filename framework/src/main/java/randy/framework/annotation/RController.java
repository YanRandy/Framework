package randy.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque une classe comme controller.
 * Seules les classes portant cette annotation sont scannées au démarrage
 * pour y chercher des méthodes @RUrlMapping.
 * La classe doit avoir un constructeur sans argument (instanciée à chaque requête).
 */
@Retention(RetentionPolicy.RUNTIME)
// @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Target({ElementType.TYPE})

public @interface RController {
    // String[] value() default {};
}