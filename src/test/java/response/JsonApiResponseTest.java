package response;

import com.fasterxml.jackson.databind.JsonNode;
import models.LinkObject;
import models.SimplePojo;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonApiResponseTest {
    //mock uriinfo
    UriInfo uriInfo = null;

    private static final SimplePojo simplePojo = new SimplePojo("idValue");

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
        assertEquals("linkObject", resultNode.get("data").get("type").textValue());
        assertEquals("42", resultNode.get("data").get("id").textValue());
        assertEquals(0, resultNode.get("data").get("attributes").size());
        assertEquals(3, resultNode.get("links").size());
        assertEquals("linkLocation/42", resultNode.get("links").get("self").textValue());
        assertEquals("unnamedLocation", resultNode.get("links").get("unnamed").textValue());
        assertEquals("otherLocation", resultNode.get("links").get("other").textValue());
    }

    private JsonNode getEntityNode(Response response) {
        return (JsonNode) response.getEntity();
    }
}
