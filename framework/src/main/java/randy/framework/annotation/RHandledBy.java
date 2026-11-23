package randy.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import randy.framework.interceptor.RInterceptor;

/**
 * Se pose sur une ANNOTATION (pas sur une méthode) pour dire quel intercepteur
 * agit à chaque requête quand cette annotation est sur une route.
 * order : le plus petit passe en premier (l'auth sera à 0).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface RHandledBy {
    Class<? extends RInterceptor> value();
    int order() default 100;
}
