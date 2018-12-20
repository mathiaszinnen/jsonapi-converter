package document;

public class JsonApiDocument {

    private Object data;
    private Object errors;
    private Object relationships;
    private Object included;

    private JsonApiDocument(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public Object getErrors() {
        return errors;
    }

    public Object getRelationships() {
        return relationships;
    }

    public Object getIncluded() {
        return included;
    }

    public static JsonApiDocument fromData(Object dataObject) {
        return new JsonApiDocument(dataObject);
    }
}
