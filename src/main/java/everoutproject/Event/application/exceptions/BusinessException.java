package everoutproject.Event.application.exceptions;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public BusinessException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
    
    public BusinessException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message) {
        this("BUSINESS_ERROR", message, HttpStatus.BAD_REQUEST);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("BusinessException{errorCode='%s', status=%s, message='%s'}",
                errorCode, status, getMessage());
    }
}
