package serializer;

import annotations.JsonApiId;
import annotations.JsonApiResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import document.JsonApiDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonApiSerializerTest {

    private static final SimpleModule module = new SimpleModule();
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void setUp(){
        module.addSerializer(new JsonApiSerializer<>(JsonApiDocument.class));
        mapper.registerModule(module);
    }

    @JsonApiResource(type = "simple")
    private static class SimplePojo {

        @JsonApiId
        public final String id = "id";
        public String anotherAttribute = "something";
        public int yetAnother = 42;
    }
    private final SimplePojo simple = new SimplePojo();

    @Test
    void testSimplePojoSerialization() {
        JsonApiDocument doc = JsonApiDocument.fromData(simple);
        JsonNode result = mapper.valueToTree(doc);

        assertEquals(JsonNodeType.OBJECT, result.getNodeType());
        assertEquals("simple", result.get("data").get("type").textValue());
        assertEquals("id", result.get("data").get("id").textValue());
        assertEquals(2, result.get("data").get("attributes").size());
        assertEquals("something", result.get("data").get("attributes").get("anotherAttribute").textValue());
        assertEquals(42, result.get("data").get("attributes").get("yetAnother").asInt());
    }

}
