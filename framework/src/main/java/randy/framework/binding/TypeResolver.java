package randy.framework.binding;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

public class TypeResolver {
    public enum Kind 
    {
        PRIMITIVE, OBJECT, LIST, APPLICATION_CONTEXT, ARRAY, MAP, UNKNOWN
    }

    public static Kind resolve(Parameter param) {
        Class<?> type = param.getType();

        if (ApplicationContext.class.isAssignableFrom(type)) return Kind.APPLICATION_CONTEXT;
        if (isPrimitive(type))                               return Kind.PRIMITIVE;
        if (type.isArray())                                  return Kind.ARRAY;
        if (List.class.isAssignableFrom(type))               return Kind.LIST;
        if (Map.class.isAssignableFrom(type))                return Kind.MAP;
        return Kind.OBJECT;
    }

    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive()
            || type == String.class
            || type == Integer.class
            || type == Long.class
            || type == Float.class
            || type == Double.class
            || type == Boolean.class;
    }
}
