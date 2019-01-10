package models;

import annotations.JsonApiId;
import annotations.JsonApiResource;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonApiResource(type = "GetterObject")
public class GetterObject {
    @JsonApiId
    private String id = "idValue";

    private String stringAttr = "stringVal";

    public int doubleAttr = 11;

    @JsonProperty(value = "name")
    private String methodName() {
        return stringAttr;
    }

    public int getNumber() {
        return 12;
    }

    private int getAnotherNumber() {
        return 13;
    }

    public String getStringAttr() {
        return stringAttr;
    }

    public int getDoubleAttr() {
        return 13;
    }
}