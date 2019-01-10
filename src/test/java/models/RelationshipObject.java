package models;

import annotations.JsonApiId;
import annotations.JsonApiRelationship;
import annotations.JsonApiResource;

@JsonApiResource(type = "relationship")
public class RelationshipObject {
    @JsonApiId
    String id = "relationship";

    @JsonApiRelationship
    SimplePojo related = new SimplePojo("relatedObject");
}
