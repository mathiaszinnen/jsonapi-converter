package util;

import annotations.JsonApiId;
import annotations.JsonApiResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import static util.Assert.assertHasValidJsonApiAnnotations;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates an empty objectnode on root node if no node by that name existed before.
     * @param rootNode the node on which the new node shall be created
     * @param nodeName the name of the node that shall be created
     * @return the newly created node
     */
    public static ObjectNode createNodeIfNotExisting(JsonNode rootNode, String nodeName) {
        assertIsObjectNode(rootNode);

        return (ObjectNode) createNodeIfNotExisting(rootNode, nodeName, mapper.createObjectNode());
    }

    /**
     * Creates a node on root node if no node by that name existed before.
     * @param rootNode the node on which the new node shall be created
     * @param nodeName the name of the node that shall be created
     * @param nodeVal the value of the newly created node
     * @return the newly created node
     */
    public static JsonNode createNodeIfNotExisting(JsonNode rootNode, String nodeName, Object nodeVal) {
        assertIsObjectNode(rootNode);
        if(!rootNode.has(nodeName)) {
            ((ObjectNode) rootNode).set(nodeName, mapper.valueToTree(nodeVal));
        }
        return rootNode.get(nodeName);
    }

    public static JsonNode createRelationshipDataNode(Object obj) throws InvocationTargetException, IllegalAccessException {
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
     * Get the jsonAPI id of a a jsonAPI resource object
     * @param data the resource object
     * @return the value of a @JsonApiId annotated field or method. If there are multiple @JsonApiId annotations present,
     * annotated fields are considered first.
     * @throws IllegalAccessException if the value of the id field cannot be determined
     * @throws InvocationTargetException if the invocation of the id method fails
     * @throws IllegalArgumentException if there is no JsonApiId annotated field or method
     */
    public static String getJsonApiId(Object data) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException {
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
    public static String getJsonApiType(Object data) {
        return data
                .getClass()
                .getDeclaredAnnotation(JsonApiResource.class)
                .type();
    }


    private static void assertIsObjectNode(JsonNode node) {
        if(!node.isObject()) {
            throw new AssertionError("JsonNode needs to be ObjectNode");
        }
    }
}
