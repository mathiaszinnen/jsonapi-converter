package serializer;

import annotations.JsonApiId;
import annotations.JsonApiResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonApiSerializerTest {

    private static final SimpleModule module = new SimpleModule();
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void setUp(){

        module.addSerializer(new JsonApiSerializer(SimplePojo.class));
        module.addSerializer(new JsonApiSerializer(Collection.class));
        module.addSerializer(new JsonApiSerializer(SelfLinkPojo.class));
        mapper.registerModule(module);
    }

    @JsonApiResource(type = "simple")
    private static class SimplePojo {

        @JsonApiId
        public final String id;
        public String anotherAttribute = "something";
        public int yetAnother = 42;

        private SimplePojo(String id) {
            this.id = id;
        }
    }
    private final SimplePojo simple = new SimplePojo("id");

    @Test
    void testSimplePojoSerialization() {
        JsonNode result = mapper.valueToTree(simple);

        System.out.println(result);
        assertEquals(JsonNodeType.OBJECT, result.getNodeType());
        assertEquals("simple", result.get("data").get("type").textValue());
        assertEquals("id", result.get("data").get("id").textValue());
        assertEquals(2, result.get("data").get("attributes").size());
        assertEquals("something", result.get("data").get("attributes").get("anotherAttribute").textValue());
        assertEquals(42, result.get("data").get("attributes").get("yetAnother").asInt());
    }

    @Test
    void testCollectionSerialization() {
        List<SimplePojo> pojoList = Arrays.asList(
                new SimplePojo("1"), new SimplePojo("2"), new SimplePojo("3"));

        JsonNode result = mapper.valueToTree(pojoList);
        JsonNode data = result.get("data");

        System.out.println(result);
        assertEquals(JsonNodeType.ARRAY, data.getNodeType());
        assertEquals(3, data.size());
        assertEquals("1", data.get(0).get("id").textValue());
        assertEquals("2", data.get(1).get("id").textValue());
        assertEquals("3", data.get(2).get("id").textValue());
        assertEquals("simple", data.get(0).get("type").textValue());
        assertEquals(2, data.get(1).get("attributes").size());
        assertEquals("something", data.get(2).get("attributes").get("anotherAttribute").textValue());
    }

    @JsonApiResource(type = "selflinker", location = "http://www.example.com/repository")
    private static class SelfLinkPojo {
        @JsonApiId
        String id = "id";
    }
    private static final SelfLinkPojo selfLinkPojo = new SelfLinkPojo();

    @Test
    public void testSelfLinkGeneration() {
        JsonNode result = mapper.valueToTree(selfLinkPojo);

        System.out.println(result);
        assertEquals(2, result.size());
        assertEquals("http://www.example.com/repository/id", result.get("links").get("self").textValue());
    }
}
