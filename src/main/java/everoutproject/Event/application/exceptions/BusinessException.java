package everoutproject.Event.application.exceptions;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    
    private final HttpStatus status;
    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static BusinessException notFound(String resource, Object id) {
        return new BusinessException(
                resource + " not found with id: " + id,
                HttpStatus.NOT_FOUND,
                "NOT_FOUND"
        );
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public static BusinessException alreadyExists(String resource, String identifier) {
        return new BusinessException(
                resource + " already exists: " + identifier,
                HttpStatus.CONFLICT,
                "ALREADY_EXISTS"
        );
    }

    public static BusinessException invalidInput(String message) {
        return new BusinessException(message, HttpStatus.BAD_REQUEST, "INVALID_INPUT");
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public static BusinessException invalidState(String message) {
        return new BusinessException(message, HttpStatus.CONFLICT, "INVALID_STATE");
    }


}
