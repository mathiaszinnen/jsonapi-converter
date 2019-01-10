package response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import serializer.JsonApiSerializer;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;
import java.util.Objects;

public class JsonApiResponse {

    private JsonNode document;
    private final UriInfo uriInfo;
    private final Response.StatusType statusCode;

    public static final String JSONAPI_TYPE = "application/vnd.api+json";

    private JsonApiResponse(UriInfo uriInfo, Response.StatusType statusCode) {
        this.uriInfo = uriInfo;
        this.statusCode = statusCode;
    }

    public static RequiredEntity getResponse(UriInfo uriInfo) {
        return new ResponseBuilder(new JsonApiResponse(uriInfo, Response.Status.OK));
    }

    private static class ResponseBuilder implements RequiredEntity, Buildable{
        private final JsonApiResponse instance;
        private final ObjectMapper mapper = new ObjectMapper();
        private final SimpleModule module = new SimpleModule();


        private ResponseBuilder(JsonApiResponse instance) {
            this.instance = instance;
        }

        @Override
        public Buildable data(Object entity) {
            Objects.requireNonNull(entity);

            module.addSerializer(new JsonApiSerializer(entity.getClass()));
            mapper.registerModule(module);

            instance.document = mapper.valueToTree(entity);

            return this;
        }

        @Override
        public Buildable data(Collection<?> entityCollection) {
            Objects.requireNonNull(entityCollection);

            if(!entityCollection.isEmpty()) {
                module.addSerializer(new JsonApiSerializer(entityCollection.toArray()[0].getClass()));
            }
            module.addSerializer(new JsonApiSerializer(Collection.class));
            mapper.registerModule(module);

            instance.document = mapper.valueToTree(entityCollection);

            return this;
        }

        @Override
        public Response build() {

            return Response
                    .status(instance.statusCode)
                    .type(JSONAPI_TYPE)
                    .entity(instance.document)
                    .build();
        }

        @Override
        public JsonApiResponse.Buildable addLink(String name, URI ref) {
            //todo
            return this;
        }

//        @Override
//        public JsonApiResponse.WithRelationship addRelationship(String name, Object entity, URI location) {
//            return null;
//        }
//
//        @Override
//        public JsonApiResponse.WithRelationship addRelationship(String name, Collection<?> entityCollection, URI location) {
//            return null;
//        }
    }

    /**
     * Interface for a Responsebuilder that needs an entity for further processing
     */
    public interface RequiredEntity {
        /**
         * Add a single entity to a response.
         *
         * @param entity the entity to add.
         * @return A buildable Responsebuilder.
         */
        JsonApiResponse.Buildable data(Object entity);

        JsonApiResponse.Buildable data(Collection<?> entityCollection);
    }

    /**
     * Interface for a Responsebuilder that meets all requirements to build the response.
     */
    public interface Buildable {
        Response build();

        /**
         * Adds a link on document level to the generated response body.
         *
         * @param name the name of the link.
         * @param ref  the reference of the link.
         * @return a buildable Responsebuilder.
         */
        JsonApiResponse.Buildable addLink(String name, URI ref);

//        /**
//         * Adds a relationship to the generated response body. The corresponding entity is represented as
//         * a ResourceIdentifier with a selflink on resource level.
//         *
//         * @param name     the name of the relationship in the generated response body.
//         * @param entity   the corresponding entity to which the relationship should be added.
//         * @param location the endpoint where the entity is located.
//         * @return a buildable Responsebuilder on which addIncluded() can be called.
//         */
//        JsonApiResponse.WithRelationship addRelationship(String name, Object entity, URI location);
//
//        /**
//         * Adds a relationship to the generated response body. The corresponding collection of entities is represented as
//         * a collection of ResourceIdentifiers with selflinks on resource level.
//         *
//         * @param name             the name of the relationship in the generated response body.
//         * @param entityCollection the collection of entities to which the relationship should be added
//         * @param location         the endpoint where the entity is located.
//         * @return a buildable Responsebuilder on which addIncluded() can be called.
//         */
//        JsonApiResponse.WithRelationship addRelationship(String name, Collection<?> entityCollection, URI location);
    }
}