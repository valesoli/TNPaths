package ar.edu.itba.webapp.dto;

public class Response {

    private boolean success;
    private String message;
    private Object data;
    private String translationTime;
    private String executionTime;

    public Response(boolean success, String message, Object data, String translationTime, String executionTime) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.translationTime = translationTime;
        this.executionTime = executionTime;
    }

    public Response(boolean success, String message, String translationTime, String executionTime) {
        this.success = success;
        this.message = message;
        this.translationTime = translationTime;
        this.executionTime = executionTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public String getTranslationTime() {
        return translationTime;
    }

    public String getExecutionTime() {
        return executionTime;
    }
}
