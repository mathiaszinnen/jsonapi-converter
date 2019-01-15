package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonNode createNodeIfNotExisting(JsonNode rootNode, String nodeName) {
        assertIsObjectNode(rootNode);

        return createNodeIfNotExisting(rootNode, nodeName, mapper.createObjectNode());
    }

    public static JsonNode createNodeIfNotExisting(JsonNode rootNode, String nodeName, Object nodeVal) {
        assertIsObjectNode(rootNode);
        if(!rootNode.has(nodeName)) {
            ((ObjectNode) rootNode).set(nodeName, mapper.valueToTree(nodeVal));
        }
        return rootNode;
    }


    private static void assertIsObjectNode(JsonNode node) {
        if(!node.isObject()) {
            throw new AssertionError("JsonNode needs to be ObjectNode");
        }
    }
}
