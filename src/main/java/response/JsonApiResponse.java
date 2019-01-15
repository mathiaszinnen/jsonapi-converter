package response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import serializer.JsonApiSerializer;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;

import static util.Assert.assertHasValidJsonApiAnnotations;
import static util.JsonUtils.createNodeIfNotExisting;
import static util.JsonUtils.createRelationshipDataNode;

public class JsonApiResponse {

    private ObjectNode document;
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

    private static class ResponseBuilder implements RequiredEntity, Buildable, WithRelationship {
        private final JsonApiResponse instance;
        private final ObjectMapper mapper = new ObjectMapper();
        private final SimpleModule module = new SimpleModule();

        private ResponseBuilder(JsonApiResponse instance) {
            this.instance = instance;
        }

        @Override
        public Buildable data(Object entity) {
            assertHasValidJsonApiAnnotations(entity);

            module.addSerializer(new JsonApiSerializer(entity.getClass()));
            mapper.registerModule(module);

            instance.document = mapper.valueToTree(entity);
            createSelfLink(instance.document);

            return this;
        }

        @Override
        public Buildable data(Collection<?> entityCollection) {
            assertHasValidJsonApiAnnotations(entityCollection);

            if(!entityCollection.isEmpty()) {
                module.addSerializer(new JsonApiSerializer(entityCollection.toArray()[0].getClass()));
            }
            module.addSerializer(new JsonApiSerializer(Collection.class));
            mapper.registerModule(module);

            instance.document = mapper.valueToTree(entityCollection);
            createSelfLink(instance.document);
            instance.document.get("data").elements().forEachRemaining(
                    el -> createResourceSelfLink( (ObjectNode) el)
            );

            return this;
        }

        private void createResourceSelfLink(ObjectNode el) {
            ObjectNode linkNode;
            createNodeIfNotExisting(el, "links");

            linkNode = (ObjectNode) el.get("links");

            String id = el.get("id").textValue();
            URI ref = instance.uriInfo.getAbsolutePath().resolve("/").resolve(id);
            linkNode.set("self", mapper.valueToTree(ref.toString()));
            el.set("links", linkNode);
        }

        private void createSelfLink(JsonNode document) {
            ObjectNode linkNode = mapper.createObjectNode();
            String selfRef = instance.uriInfo.getAbsolutePath().toString();
            linkNode.set("self", mapper.valueToTree(selfRef));
            ((ObjectNode) document).set("links", linkNode);
        }

        @Override
        public Response build() {
            updateLinks(instance.document.get("data"));

            return Response
                    .status(instance.statusCode)
                    .type(JSONAPI_TYPE)
                    .entity(instance.document)
                    .build();
        }

        /**
         * Updates existing, possibly relative links so they hold the absolute address afterwards.
         */
        private void updateLinks(JsonNode dataNode) {
            if(dataNode.isArray()) {
                for(JsonNode dataElement: dataNode) {
                    updateLinks(dataElement);
                }
            } else {
                if (dataNode.has("links")) {
                    ObjectNode linkNode = (ObjectNode) dataNode.get("links");
                    URI baseUri = instance.uriInfo.getAbsolutePath().resolve("/");

                    linkNode
                            .fieldNames()
                            .forEachRemaining(
                                    fn -> linkNode.set(fn, updateSingleLinkNode(baseUri, linkNode, fn))
                            );
                }
            }
        }

        private JsonNode updateSingleLinkNode(URI baseUri, ObjectNode linkNode, String nodeName) {
            JsonNode refNode = linkNode.get(nodeName);
            if(isAbsolute(refNode.textValue())) {
                return refNode;
            } else {
                URI absUri = baseUri.resolve(refNode.textValue());
                return mapper.valueToTree(absUri);
            }
        }

        private boolean isAbsolute(String linkNode) {
            return URI.create(linkNode).isAbsolute();
        }

        @Override
        public JsonApiResponse.Buildable addLink(String name, URI ref) {

            createNodeIfNotExisting(instance.document, "links");

            URI absRef = instance.uriInfo.getAbsolutePath().resolve("/").resolve(ref);

            ((ObjectNode) instance.document.get("links"))
                    .set(name, mapper.valueToTree(absRef.toString()));

            return this;
        }

        @Override
        public JsonApiResponse.WithRelationship addRelationship(Object entity) {
            String name = entity.getClass().getSimpleName();
            addRelationship(name, entity);
            return this;
        }

        @Override
        public JsonApiResponse.WithRelationship addRelationship(String name, Object entity) {
            assertHasValidJsonApiAnnotations(entity);

            if(dataNode().isArray()) {
                //exception?
                //do it for each datanode element?
            } else {
                ObjectNode relationshipsNode = createNodeIfNotExisting(dataNode(), "relationships");
                ObjectNode currentRelationship = createNodeIfNotExisting(relationshipsNode, name);
                try {
                    JsonNode relationshipDataNode = createRelationshipDataNode(entity);
                    currentRelationship.set("data", relationshipDataNode);

                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException("Only correctly annotated classes can be added as relationships. Please add JsonApiResource and JsonApiId annotations to " + entity.getClass(), e);
                }
            }

            return this;
        }

        @Override
        public WithRelationship include(Object included) {
            return null;
        }

        @Override
        public WithRelationship include(String includedName) {
            return null;
        }

        private JsonNode dataNode() {
            return instance.document.get("data");
        }
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

        /**
         * Add a collection of entities to a response.
         * @param entityCollection
         * @return
         */
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

        /**
         * Adds a relationship to the generated response body. The corresponding entity is represented as
         * a ResourceIdentifier with a selflink on resource level.
         *
         * @param name   the name of the relationship in the generated response body.
         * @param entity the corresponding entity to which the relationship should be added.
         * @return a buildable Responsebuilder on which addIncluded() can be called.
         */
        JsonApiResponse.WithRelationship addRelationship(String name, Object entity);

        /**
         * Adds a relationship without specifying its name. The runtime class of the related entity is being used as relationship name.
         * @param entity the corresponding entity to which the relationship should be added.
         * @return a buildable Responsebuilder on which addIncluded() can be called.
         */
        JsonApiResponse.WithRelationship addRelationship(Object entity);
    }

    /**
     * Interface for a Responsebuilder which has at least one relationship added.
     * It is needed to ensure that included entities can only be added to responses that contain relationships.
     * Extends Buildable since it can only be returned if the Responsebuilder already contains an entity
     */
    public interface WithRelationship extends Buildable {
        /**
         * Adds a included resource to the generated response body.
         *
         * @param included the entity to be included. Entity has to be added as a relationship before.
         * @return a buildable ResponseBuilder on which addIncluded() can be called.
         */
        WithRelationship include(Object included);

        /**
         * Same as above, except the resource to be included is identified by the name of the corresponding relationship.
         * @param includedName identifier for the included relationship
         * @return a buildable ResponseBuilder on which addIncluded() can be called.
         */
        WithRelationship include(String includedName);
    }
}
