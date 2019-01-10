package response;

import com.fasterxml.jackson.databind.JsonNode;
import models.LinkObject;
import models.SimplePojo;
import name.falgout.jeffrey.testing.junit.mockito.MockitoExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JsonApiResponseTest {
    private static UriInfo uriInfo = mock(UriInfo.class);

    private static final SimplePojo simplePojo = new SimplePojo("idValue");

    @BeforeAll
    public static void setUp() {
        when(uriInfo.getAbsolutePath()).thenReturn(URI.create("http://BASEPATH/"));
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
        assertEquals(3, resultNode.get("links").size());
        assertEquals("http://BASEPATH/linkLocation/42", resultNode.get("links").get("self").textValue());
        assertEquals("http://BASEPATH/unnamedLocation", resultNode.get("links").get("unnamed").textValue());
        assertEquals("http://BASEPATH/otherLocation", resultNode.get("links").get("other").textValue());
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
        assertEquals(1, resultNode.get("links").size());
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
        assertEquals(1, resultNode.get("links").size());
        assertEquals("http://www.google.com", resultNode.get("links").get("google").textValue());
    }

    private JsonNode getEntityNode(Response response) {
        return (JsonNode) response.getEntity();
    }
}
