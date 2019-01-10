package models;

import annotations.JsonApiId;
import annotations.JsonApiRelationship;
import annotations.JsonApiResource;

@JsonApiResource(type = "relationshipType")
public class RelationshipObject {
    @JsonApiId
    String id = "relationshipId";

    @JsonApiRelationship
    SimplePojo related = new SimplePojo("relatedObject");

    @JsonApiRelationship(name = "named")
    LinkObject namedRelated = new LinkObject();
}
