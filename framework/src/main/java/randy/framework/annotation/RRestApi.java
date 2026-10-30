package randy.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Le retour de la méthode est écrit en JSON (Jackson) au lieu d'être traité comme une vue.
 * Change aussi la façon de refuser l'accès : 401/403 en JSON plutôt qu'une redirection.
 */
@Retention(RetentionPolicy.RUNTIME)
// @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Target({ElementType.METHOD})

public @interface RRestApi {
    
}
