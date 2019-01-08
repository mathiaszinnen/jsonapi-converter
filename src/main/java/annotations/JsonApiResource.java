package annotations;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import serializer.JsonApiSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for marking a java object as jsonAPI resource, resulting in the object being serialized confirming to the jsonAPI specification.
 * Type needs to be specified, since it is a mandatory field of a jsonAPI resource.
 * When location is specified, the jsonAPI representation of the object will contain a links object with a selflink entry.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@JacksonAnnotationsInside
@JsonSerialize(using = JsonApiSerializer.class)
public @interface JsonApiResource {
    String type();
    String location() default "";
}
