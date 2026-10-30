package randy.framework.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sprint 10 / 10-bis : protège une route (sur une méthode) ou toutes les routes d'un controller (sur la classe).
 *  - {@code @RAuthenticated}                     → auth simple : l'utilisateur doit être connecté
 *  - {@code @RAuthenticated(roles = {"ADMIN"})}  → auth complexe : connecté ET au moins un des rôles
 * Sur la méthode, elle remplace celle de la classe. La vérification est faite par RAuthSecurity.
 */
@Retention(RetentionPolicy.RUNTIME)
// @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})

public @interface RAuthenticated {
    /** Rôles acceptés (un seul suffit). Vide = être connecté suffit. */
    String[] roles() default {};
}