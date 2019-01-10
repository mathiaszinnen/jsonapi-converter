package models;

import annotations.JsonApiId;
import annotations.JsonApiResource;

@JsonApiResource(type = "simple")
public class SimplePojo {

    @JsonApiId
    public final String id;
    public String anotherAttribute = "something";
    public int yetAnother = 42;

    public SimplePojo(String id) {
        this.id = id;
    }
}
