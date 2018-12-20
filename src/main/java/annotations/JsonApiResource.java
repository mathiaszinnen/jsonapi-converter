package annotations;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import serializer.JsonApiSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
@JacksonAnnotationsInside
@JsonSerialize(using = JsonApiSerializer.class)
public @interface JsonApiResource {
    String type();
}
