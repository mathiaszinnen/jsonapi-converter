package serializer;

import annotations.JsonApiId;
import annotations.JsonApiResource;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import document.JsonApiDocument;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;

public class JsonApiSerializer<T extends JsonApiDocument> extends StdSerializer<T> {
    private static final ObjectMapper mapper = new ObjectMapper();


    private JsonApiSerializer() {
        this(null);
    }

    protected JsonApiSerializer(Class<T> t) {
        super(t);
    }

    @Override
    public void serialize(JsonApiDocument doc, JsonGenerator gen, SerializerProvider provider) throws IOException {
        assertIsValidJsonApiDocument(doc);

        gen.writeStartObject();

        try{
            serializeData(doc, gen);

            serializeErrors(doc, gen);

            serializeIncluded(doc, gen);

            serializeRelationships(doc, gen);
        } catch (Exception e) {
            throw new IOException("Serialization failed", e);
        }

        gen.writeEndObject();
    }

    private void serializeData(JsonApiDocument doc, JsonGenerator gen) throws IOException, InvocationTargetException, IllegalAccessException {
        Object data = doc.getData();
        assertIsValidData(data);

        JsonNode dataNode;
        if(data instanceof Collection) { //data is array of resource objects
            dataNode = mapper.createArrayNode();
            //add all data entries to docdataNode array
        }
        else { //data is single resource object
            dataNode = createDataNode(data);
            //add data to datanode
        }

        gen.writeObjectField("data", dataNode);
    }

    private ObjectNode createDataNode(Object data) throws IllegalAccessException, InvocationTargetException {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", getJsonApiType(data));
        node.put("id", getJsonApiId(data));
        node.set("attributes", getJsonApiAttributes(data));
        return node;
    }

    private JsonNode getJsonApiAttributes(Object data) throws IllegalAccessException {
        ObjectNode node = mapper.createObjectNode();
        for(Field field: data.getClass().getDeclaredFields()) {
            if(!field.isAnnotationPresent(JsonApiId.class)) {
                node.set(field.getName(), mapper.valueToTree(field.get(data)));
            }
        }
        return node;
    }

    private String getJsonApiId(Object data) throws IllegalAccessException, InvocationTargetException {
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

    private String getJsonApiType(Object data) {
        return data
                .getClass()
                .getDeclaredAnnotation(JsonApiResource.class)
                .type();
    }

    private void serializeErrors(JsonApiDocument doc, JsonGenerator gen) {
        //later
    }

    private void serializeRelationships(JsonApiDocument doc, JsonGenerator gen) {
        //later
    }

    private void serializeIncluded(JsonApiDocument doc, JsonGenerator gen) {
        //later
    }

    private void assertIsValidData(Object data) {
        //later
    }

    private void assertIsValidJsonApiDocument(JsonApiDocument doc) {
        //later
    }
}
