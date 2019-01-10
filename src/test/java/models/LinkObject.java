package models;

import annotations.JsonApiId;
import annotations.JsonApiLink;
import annotations.JsonApiResource;

@JsonApiResource(type = "linkObject", location = "linkLocation")
public class LinkObject {
    @JsonApiId
    private String id = "42";

    @JsonApiLink(name = "other", target = "otherLocation")
    private LinkObject other;

    @JsonApiLink(target = "unnamedLocation")
    private LinkObject unnamed;
}