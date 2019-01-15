package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.JsonUtils.createNodeIfNotExisting;

public class JsonUtilsTest {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCreateNodeIfNotExisting() {
        ObjectNode result = mapper.createObjectNode();
        result.set("existing", mapper.valueToTree("val"));

        createNodeIfNotExisting(result, "existing");
        createNodeIfNotExisting(result, "new");
        createNodeIfNotExisting(result, "withValue", "stringVal");
        createNodeIfNotExisting(result, "withValue", "discardThis");


        assertEquals(3, result.size());
        assertEquals("val", result.get("existing").textValue());
        assertEquals("stringVal", result.get("withValue").textValue());
    }
}
