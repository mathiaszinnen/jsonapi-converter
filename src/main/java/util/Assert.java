package util;

import annotations.JsonApiId;
import annotations.JsonApiResource;
import exceptions.JsonApiSerializationException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

public class Assert {

    public static void assertHasValidJsonApiAnnotations(Collection<?> collection) {
        Objects.requireNonNull(collection);
        collection.forEach(Assert::assertHasValidJsonApiAnnotations);
    }

    public static void assertHasValidJsonApiAnnotations(Object obj) {
        Objects.requireNonNull(obj);
        //collections
        if (obj instanceof Collection) {
            ((Collection) obj).forEach(Assert::assertHasValidJsonApiAnnotations);
            return;
        }
        if (!obj.getClass().isAnnotationPresent(JsonApiResource.class)) {
            throw new JsonApiSerializationException("Class needs to be annotated with JsonApiResource annotation");
        }
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(JsonApiId.class)) {
                return;
            }
        }
        for (Method method : obj.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(JsonApiId.class) && isGettable(method)) {
                return;
            }
        }
        throw new JsonApiSerializationException("At least one field or no-arg non-void method needs to be annotated with JsonApiId annotation");
    }

    public static boolean isGettable(Method method) {
        return method.getParameterCount() == 0
                && !method.getReturnType().equals(Void.TYPE);
    }

}
