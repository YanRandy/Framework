package randy.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Associe une méthode de controller à une URL exacte et à un ou plusieurs verbes HTTP.
 * Ex : {@code @RUrlMapping(value = "/livres", method = "GET")}.
 * Sans "method", la route répond en GET et en POST.
 * Deux méthodes avec la même URL + le même verbe = erreur au démarrage.
 */
@Retention(RetentionPolicy.RUNTIME)
// @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Target({ElementType.METHOD})

public @interface RUrlMapping {
    String value();
    String[] method() default {"GET", "POST"};
}