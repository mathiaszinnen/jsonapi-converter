package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface for marking attributes of a json api resource class as (static) links.
 * When specified, the serialized objects links object contains a ' "name":"target" ' entry.
 * May be applied multiple times per class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonApiLink {
    String target();
    String name() default "";
}
