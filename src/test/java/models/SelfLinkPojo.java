package models;

import annotations.JsonApiId;
import annotations.JsonApiResource;

@JsonApiResource(type = "selflinker", location = "http://www.example.com/repository")
public class SelfLinkPojo {
    @JsonApiId
    String id = "id";
}