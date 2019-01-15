package models;

import annotations.JsonApiId;
import annotations.JsonApiRelationship;
import annotations.JsonApiResource;

@JsonApiResource(type = "relationshipType")
public class RelationshipObject {

    public RelationshipObject() { }

    public RelationshipObject(Object dangerous) {
        this.dangerous = dangerous;
    }

    @JsonApiId
    String id = "relationshipId";

    @JsonApiRelationship
    SimplePojo related = new SimplePojo("relatedObject");

    @JsonApiRelationship
    Object dangerous = new SimplePojo("alright");

    @JsonApiRelationship(name = "named")
    LinkObject namedRelated = new LinkObject();

    @JsonApiRelationship(location = "ftp://download.me/")
    SimplePojo located = new SimplePojo("located");
}
