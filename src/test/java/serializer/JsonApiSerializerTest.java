package serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonApiSerializerTest {

    private static final SimpleModule module = new SimpleModule();
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void setUp() {

        module.addSerializer(new JsonApiSerializer(SimplePojo.class));
        module.addSerializer(new JsonApiSerializer(Collection.class));
        module.addSerializer(new JsonApiSerializer(SelfLinkPojo.class));
        module.addSerializer(new JsonApiSerializer(RelationshipObject.class));
        mapper.registerModule(module);
    }

    @Test
    public void testSimplePojoSerialization() {
        SimplePojo simple = new SimplePojo("id");
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
    public void testCollectionSerialization() {
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

    @Test
    public void testEmptyCollectionSerialization() {
        List<SimplePojo> emptyList = Collections.emptyList();

        JsonNode result = mapper.valueToTree(emptyList);

        System.out.println(result);
        JsonNode data = result.get("data");
        assertEquals(JsonNodeType.ARRAY, data.getNodeType());
        assertEquals(0, data.size());
    }

    @Test
    public void testSelfLinkGeneration() {
        SelfLinkPojo selfLinkPojo = new SelfLinkPojo();
        JsonNode result = mapper.valueToTree(selfLinkPojo);

        System.out.println(result);
        assertEquals(1, result.size());
        assertEquals("http://www.example.com/repository/id", result.get("data").get("links").get("self").textValue());
    }

    @Test
    public void testMethodSerialization() {
        GetterObject getterObject = new GetterObject();
        JsonNode result = mapper.valueToTree(getterObject);

        System.out.println(result);
        JsonNode dataNode = result.get("data");
        assertEquals("idValue", dataNode.get("id").textValue());
        assertEquals(4, dataNode.get("attributes").size());
        assertEquals(12, dataNode.get("attributes").get("number").asInt());
        assertEquals("stringVal", dataNode.get("attributes").get("name").textValue());
        assertEquals("stringVal", dataNode.get("attributes").get("stringAttr").textValue());
        assertEquals(11, dataNode.get("attributes").get("doubleAttr").asInt());
    }

    @Test
    public void testLinkSerialization() {
        LinkObject linkObject = new LinkObject();
        JsonNode result = mapper.valueToTree(linkObject);

        System.out.println(result);
        assertTrue(result.has("data"));
        assertTrue(result.get("data").has("links"));
        assertTrue(result.get("data").get("links").isObject());
        assertEquals(3, result.get("data").get("links").size());
        assertEquals("linkLocation/42", result.get("data").get("links").get("self").textValue());
        assertEquals("otherLocation", result.get("data").get("links").get("other").textValue());
        assertEquals("unnamedLocation", result.get("data").get("links").get("unnamed").textValue());
    }

    @Test
    public void testRelationshipSerialization() {
        RelationshipObject relObject = new RelationshipObject();
        JsonNode result = mapper.valueToTree(relObject);

        System.out.println(result);
        assertTrue(result.get("data").has("relationships"));
        assertEquals(JsonNodeType.OBJECT, result.get("data").getNodeType());
        assertTrue(result.get("data").get("relationships").has("related"));
        assertEquals(
                "relatedObject",
                result.get("data").get("relationships").get("related").get("id").textValue());
        assertEquals(
                "simple",
                result.get("data").get("relationships").get("related").get("type").textValue());
        assertEquals(
                2,
                result.get("data").get("relationships").get("related").size());
        assertEquals(
                "linkObject",
                result.get("data").get("relationships").get("named").get("type").textValue());
        assertEquals(
                "42",
                result.get("data").get("relationships").get("named").get("id").textValue());
    }
}
