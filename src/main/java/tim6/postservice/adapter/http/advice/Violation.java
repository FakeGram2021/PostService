package tim6.postservice.adapter.http.advice;

public class Violation {

    private final String fieldName;

    private final String message;

    public Violation(String fieldName, String message) {
        this.fieldName = fieldName;
        this.message = message;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return "Violation{"
                + "fieldName='"
                + this.fieldName
                + '\''
                + ", message='"
                + this.message
                + '\''
                + '}';
    }
}
