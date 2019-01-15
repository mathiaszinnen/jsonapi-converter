package serializer;

import annotations.JsonApiId;
import annotations.JsonApiLink;
import annotations.JsonApiRelationship;
import annotations.JsonApiResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import exceptions.JsonApiSerializationException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static util.Assert.assertHasValidJsonApiAnnotations;
import static util.Assert.isGettable;

public class JsonApiSerializer<T> extends StdSerializer<Object> {
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonApiSerializer() {
        this(null);
    }

    public JsonApiSerializer(Class<Object> t) {
        super(t);
    }

    @Override
    public void serialize(Object obj, JsonGenerator gen, SerializerProvider provider) throws IOException {
        assertHasValidJsonApiAnnotations(obj);

        gen.writeStartObject();

        try{
            serializeData(obj, gen);

            serializeErrors(obj, gen);

            serializeIncluded(obj, gen);

        } catch (Exception e) {
            throw new IOException("Serialization failed", e);
        }

        gen.writeEndObject();
    }

    private String getLocation(Class clazz) {
        JsonApiResource annotation = (JsonApiResource) clazz.getDeclaredAnnotation(JsonApiResource.class);
        return (annotation != null)? annotation.location() : "";
    }

    private void serializeData(Object obj, JsonGenerator gen) throws IOException, InvocationTargetException, IllegalAccessException {
        assertHasValidData(obj);

        JsonNode dataNode;
        if(obj instanceof Collection) { //data is array of resource objects
            //add all serialized elements to the data node
            ArrayNode arrayNode = mapper.createArrayNode();
            for(Object resourceObject: (Collection) obj) {
                arrayNode.add(createDataNode(resourceObject));
            }
            dataNode = arrayNode;
        }
        else { //data is single resource object
            //serialize the object
            dataNode = createDataNode(obj);
        }


        gen.writeObjectField("data", dataNode);
    }

    private ObjectNode createDataNode(Object data) throws IllegalAccessException, InvocationTargetException {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", getJsonApiType(data));
        node.put("id", getJsonApiId(data));
        node.set("attributes", getJsonApiAttributes(data));
        serializeLinks(data, node);
        serializeRelationships(data, node);
        return node;
    }

    private void serializeLinks(Object obj, ObjectNode node) throws InvocationTargetException, IllegalAccessException {
        Class clazz = obj.getClass();
        ObjectNode linkNode = mapper.createObjectNode();
        //process JsonApiLink annotations
        for(Field field: clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if(field.isAnnotationPresent(JsonApiLink.class)) {
                String linkName = field.getDeclaredAnnotation(JsonApiLink.class).name();
                String linkTarget = field.getDeclaredAnnotation(JsonApiLink.class).target();
                if(linkName.equals("")) { //default value. no name specified
                    field.setAccessible(true);
                    linkName = field.getName();
                }
                linkNode.set(linkName, mapper.valueToTree(linkTarget));
            }
        }

        //create selflink
        if(!getLocation(clazz).equals("")) {
                linkNode.put("self", getLocation(clazz) + "/" + getJsonApiId(obj));
        }
        if(linkNode.size() > 0) {
            node.set("links", linkNode);
        }
    }

    private void serializeRelationships(Object obj, ObjectNode node) throws IllegalAccessException, InvocationTargetException {

        ObjectNode relationshipsNode = createRelationshipsNode(obj);

        if(relationshipsNode.size() > 0) {
            node.set("relationships", relationshipsNode);
        }
    }

    private ObjectNode createRelationshipsNode(Object obj) throws IllegalAccessException, InvocationTargetException {
        ObjectNode relationshipsNode = mapper.createObjectNode();
        Class clazz = obj.getClass();

        for(Field field: clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if(field.isAnnotationPresent(JsonApiRelationship.class)) {
                String name = field.getDeclaredAnnotation(JsonApiRelationship.class).name();
                if(name.equals("")) {
                    name = field.getName();
                }
                ObjectNode otherNode = mapper.createObjectNode();
                Object other = field.get(obj);
                JsonNode otherDataNode =createRelationshipDataNode(other);
                otherNode.set("data", otherDataNode);
                relationshipsNode.set(name, otherNode);
            }
        }
        return relationshipsNode;
    }

    private JsonNode createRelationshipDataNode(Object obj) throws InvocationTargetException, IllegalAccessException {
        JsonNode relatedDataNode;
        if(obj instanceof Collection) {
            relatedDataNode = mapper.createArrayNode();
            for(Object element: (Collection) obj) {
                ((ArrayNode) relatedDataNode).add(createRelationshipDataNode(element));
            }

        } else {
            assertHasValidJsonApiAnnotations(obj);
            relatedDataNode = mapper.createObjectNode();
            ((ObjectNode) relatedDataNode).set("id", mapper.valueToTree(getJsonApiId(obj)));
            ((ObjectNode) relatedDataNode).set("type", mapper.valueToTree(getJsonApiType(obj)));
        }
        return relatedDataNode;

    }

    /**
     * Get attributes of a jsonAPI resource object
     * (i.e. public fields, @JsonProperty annotated fields, getter methods, @JsonProperty annotated methods).
     * Fields or methods annotated with @JsonApiId are ignored, since they are serialized elsewhere.
     * @param data the resource object
     * @return a JsonNode containing all visible attributes except jsonAPI id
     * @throws IllegalAccessException if the value of a field cannot be determined
     * @throws InvocationTargetException if the invocation of a attribute method fails
     */
    private JsonNode getJsonApiAttributes(Object data) throws IllegalAccessException, InvocationTargetException {
        ObjectNode node = mapper.createObjectNode();
        //serialize fields
        for(Field field: data.getClass().getDeclaredFields()) {
            if(field.isAnnotationPresent(JsonApiId.class) ) {
                continue; //do not serialize id twice.
            } else if(field.isAnnotationPresent(JsonProperty.class)) {
                field.setAccessible(true);
                String fieldName = field.getAnnotation(JsonProperty.class).value();
                node.set(fieldName, mapper.valueToTree(field.get(data)));
            } else if(!Modifier.isPublic(field.getModifiers())) {
                continue; //do not serialize inaccessible fields without JsonProperty-annotation.
            } else {
                node.set(field.getName(), mapper.valueToTree(field.get(data)));
            }
        }
        //serialize getters and @JsonProperty annotated methods
        for(Method method: data.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if(method.isAnnotationPresent(JsonProperty.class)) {
                if(!isGettable(method)) {
                    throw new JsonApiSerializationException("@JsonProperty annotated method needs to have a non void return value" +
                            "and no parameters.");
                }
                String attributeName = method.getDeclaredAnnotation(JsonProperty.class).value();
                if(!node.has(attributeName)) {
                    node.set(attributeName, mapper.valueToTree(method.invoke(data)));
                }
            }
            if(isGetter(method)) {
                if(!node.has(getterAttribute(method))) {
                    node.set(getterAttribute(method), mapper.valueToTree(method.invoke(data)));
                }
            }
        }
        return node;
    }

    private String getterAttribute(Method method) {
        //remove the leading "get" and lowercase first letter to match attribute name conventions
        char[] chars = method.getName().substring(3).toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    private boolean isGetter(Method method) {
        return method.getName().startsWith("get")
                && Modifier.isPublic(method.getModifiers())
                && isGettable(method);
    }

    /**
     * Get the jsonAPI id of a a jsonAPI resource object
     * @param data the resource object
     * @return the value of a @JsonApiId annotated field or method. If there are multiple @JsonApiId annotations present,
     * annotated fields are considered first.
     * @throws IllegalAccessException if the value of the id field cannot be determined
     * @throws InvocationTargetException if the invocation of the id method fails
     * @throws IllegalArgumentException if there is no JsonApiId annotated field or method
     */
    private String getJsonApiId(Object data) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException {
        for(Field field: data.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if(field.isAnnotationPresent(JsonApiId.class)
                    && field.getType() == String.class) {
                return (String) field.get(data);
            }
        }
        for(Method method: data.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if(method.isAnnotationPresent(JsonApiId.class)
                    && method.getReturnType() == String.class
                    && method.getParameterCount() == 0) {
                return (String) method.invoke(data);
            }
        }
        throw new IllegalArgumentException(data.getClass().getCanonicalName() + " contains no @JsonApiId annotation");
    }

    /**
     * Get the jsonAPI type of a jsonAPI resource object
     * @param data the resource object
     * @return the type of the object, as specified in @JsonApiResource annotation
     */
    private String getJsonApiType(Object data) {
        return data
                .getClass()
                .getDeclaredAnnotation(JsonApiResource.class)
                .type();
    }

    private void serializeErrors(Object doc, JsonGenerator gen) {
        //later
    }



    private void serializeIncluded(Object doc, JsonGenerator gen) {
        //later
    }

    private void assertHasValidData(Object data) {
        //later
    }

}
