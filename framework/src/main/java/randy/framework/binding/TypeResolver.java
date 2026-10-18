package randy.framework.binding;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import randy.framework.model.MapFile;

public class TypeResolver {
    public enum Kind {
        PRIMITIVE, OBJECT, LIST, APPLICATION_CONTEXT, ARRAY, MAP, FILE, UNKNOWN
    }

    public static Kind resolve(Parameter param) {
        Class<?> type = param.getType();

        if (ApplicationContext.class.isAssignableFrom(type))
            return Kind.APPLICATION_CONTEXT;
        if (isPrimitive(type))
            return Kind.PRIMITIVE;
        if (type.isArray())
            return Kind.ARRAY;
        if (List.class.isAssignableFrom(type))
            return Kind.LIST;
        if (type == MapFile.class)
            return Kind.FILE;
        if (Map.class.isAssignableFrom(type))
            return Kind.MAP;
        return Kind.OBJECT;
    }

    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || isWrapper(type);
    }

    public static boolean isWrapper(Class<?> type) {
        return type == Integer.class
                || type == Long.class
                || type == Float.class
                || type == Double.class
                || type == Boolean.class
                || type == Byte.class
                || type == Character.class
                || type == Short.class;
    }
}
