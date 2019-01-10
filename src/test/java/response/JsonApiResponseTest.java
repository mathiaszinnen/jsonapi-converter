package response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import models.LinkObject;
import models.SimplePojo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonApiResponseTest {
    private static UriInfo uriInfo = mock(UriInfo.class);

    private static final SimplePojo simplePojo = new SimplePojo("idValue");

    @BeforeAll
    public static void setUp() {
        when(uriInfo.getAbsolutePath()).thenReturn(URI.create("http://BASEPATH"));
    }

    @Test
    public void testGetResponse() {
        Response result = JsonApiResponse
                .getResponse(uriInfo)
                .data(simplePojo)
                .build();

        assertEquals(200, result.getStatus());
        assertEquals("application/vnd.api+json", result.getHeaderString("Content-Type"));
        assertTrue(result.hasEntity());
    }

    @Test
    public void testAddWrongEntity() {
        Response result = JsonApiResponse
                .getResponse(uriInfo)
                .data(new Object())
                .build();


    }

    @Test
    public void testAddSimpleData() {
        Response result = JsonApiResponse
                .getResponse(uriInfo)
                .data(simplePojo)
                .build();

        JsonNode resultNode = getEntityNode(result);
        assertEquals("simple", resultNode.get("data").get("type").textValue());
        assertEquals("idValue", resultNode.get("data").get("id").textValue());
        assertEquals("something", resultNode.get("data").get("attributes").get("anotherAttribute").textValue());
        assertEquals(42, resultNode.get("data").get("attributes").get("yetAnother").asInt());
        assertEquals("http://BASEPATH", resultNode.get("links").get("self").textValue());
    }

    @Test
    public void testAddCollection() {
        List<SimplePojo> list = Arrays.asList(new SimplePojo("1"), new SimplePojo("2"));

        Response result = JsonApiResponse
                .getResponse(uriInfo)
                .data(list)
                .addLink("hallo",URI.create("du"))
                .build();

        JsonNode resultNode = getEntityNode(result);
        assertEquals(JsonNodeType.ARRAY, resultNode.get("data").getNodeType());
        assertEquals(2, resultNode.get("data").size());
        assertEquals("1", resultNode.get("data").get(0).get("id").textValue());
        assertEquals("2", resultNode.get("data").get(1).get("id").textValue());
        assertEquals("http://BASEPATH/du", resultNode.get("links").get("hallo").textValue());
        assertEquals("http://BASEPATH", resultNode.get("links").get("self").textValue());
        assertEquals(
                "http://BASEPATH/1",
                resultNode.get("data").get(0).get("links").get("self").textValue());
        assertEquals(
                "http://BASEPATH/2",
                resultNode.get("data").get(1).get("links").get("self").textValue());
    }

    @Test
    public void testAddEmptyCollection() {
        List<SimplePojo> list = new LinkedList<>();

        Response result = JsonApiResponse
                .getResponse(uriInfo)
                .data(list)
                .build();

        assertEquals(0, getEntityNode(result).get("data").size());
    }

    @Test
    public void testAddLinkedCollection() {
        List<LinkObject> list = Arrays.asList(new LinkObject("0"), new LinkObject("1"));

        Response result = JsonApiResponse
                .getResponse(uriInfo)
                .data(list)
                .addLink("top-level-link", URI.create("here"))
                .build();

        JsonNode resultNode = getEntityNode(result);
        System.out.println(resultNode);
        assertEquals(
                "http://BASEPATH/here",
                resultNode.get("links").get("top-level-link").textValue());
        assertEquals("0", resultNode.get("data").get(0).get("id").textValue());
        assertEquals("1", resultNode.get("data").get(1).get("id").textValue());
        assertEquals(
                "http://BASEPATH/otherLocation",
                resultNode.get("data").get(0).get("links").get("other").textValue());
        assertEquals(
                "http://BASEPATH/unnamedLocation",
                resultNode.get("data").get(1).get("links").get("unnamed").textValue());
        assertEquals(
                "http://BASEPATH/0",
                resultNode.get("data").get(0).get("links").get("self").textValue());
    }

    @Test
    public void testAddLinkedData() {
        LinkObject linked = new LinkObject();
        Response result = JsonApiResponse
                .getResponse(uriInfo)
                .data(linked)
                .build();

        JsonNode resultNode = getEntityNode(result);
        System.out.println(resultNode);
        assertEquals("linkObject", resultNode.get("data").get("type").textValue());
        assertEquals("42", resultNode.get("data").get("id").textValue());
        assertEquals(0, resultNode.get("data").get("attributes").size());
        assertEquals(3, resultNode.get("data").get("links").size());
        assertEquals("http://BASEPATH/linkLocation/42", resultNode.get("data").get("links").get("self").textValue());
        assertEquals("http://BASEPATH/unnamedLocation", resultNode.get("data").get("links").get("unnamed").textValue());
        assertEquals("http://BASEPATH/otherLocation", resultNode.get("data").get("links").get("other").textValue());
    }

    @Test
    public void testAddRelativeLink() {
        Response result = JsonApiResponse
                .getResponse(uriInfo)
                .data(simplePojo)
                .addLink("name", URI.create("location"))
                .build();

        JsonNode resultNode = getEntityNode(result);
        System.out.println(resultNode);
        assertEquals(2, resultNode.get("links").size());
        assertEquals("http://BASEPATH/location", resultNode.get("links").get("name").textValue());
    }

    @Test
    public void testAddAbsoluteLink() {
        Response result = JsonApiResponse
                .getResponse(uriInfo)
                .data(simplePojo)
                .addLink("google", URI.create("http://www.google.com"))
                .build();

        JsonNode resultNode = getEntityNode(result);
        System.out.println(resultNode);
        assertEquals(2, resultNode.get("links").size());
        assertEquals("http://www.google.com", resultNode.get("links").get("google").textValue());
    }

    private JsonNode getEntityNode(Response response) {
        return (JsonNode) response.getEntity();
    }
}
