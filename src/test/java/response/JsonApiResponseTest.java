package response;

import com.fasterxml.jackson.databind.JsonNode;
import models.SimplePojo;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonApiResponseTest {
    //mock uriinfo

    @Test
    public void testGetResponse() {
        SimplePojo simplePojo = new SimplePojo("idValue");

        Response result = JsonApiResponse
                .getResponse(null)
                .data(simplePojo)
                .build();

        JsonNode resultNode = (JsonNode) result.getEntity();
        assertEquals("simple", resultNode.get("data").get("type").textValue());
        assertEquals("idValue", resultNode.get("data").get("id").textValue());
        assertEquals("something", resultNode.get("data").get("attributes").get("anotherAttribute").textValue());
        assertEquals(42, resultNode.get("data").get("attributes").get("yetAnother").asInt());
    }
}
