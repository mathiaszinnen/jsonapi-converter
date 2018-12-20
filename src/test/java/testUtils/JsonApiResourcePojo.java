package testUtils;

import annotations.JsonApiId;
import annotations.JsonApiResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonApiResource(type = "test")
public class JsonApiResourcePojo {
    private final String id;
    private final String hidden = "hidden";
    private final int number;
    private final long longnumber;

    private final List<String> list = new ArrayList<>();

    public JsonApiResourcePojo(String id, int number, long longnumber, String... strings) {
        this.id = id;
        this.number = number;
        this.longnumber = longnumber;
        list.addAll(Arrays.asList(strings));
    }

    @JsonApiId
    public String getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public long getLongnumber() {
        return longnumber;
    }

    public List<String> getList() {
        return list;
    }
}
