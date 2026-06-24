package randy.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
// @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Target({ElementType.METHOD})

public @interface UrlMapping {
    String value();
    String[] method() default {"GET", "POST"};
}