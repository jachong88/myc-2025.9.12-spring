package web.common.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
  private final ErrorCode errorCode;
  private final HttpStatus httpStatus;
  private final Object[] args;
  private final Map<String, Object> details;

  public AppException(ErrorCode errorCode, Object... args) {
    this(errorCode, errorCode.httpStatus(), null, null, args);
  }

  public AppException(ErrorCode errorCode, Map<String, Object> details, Object... args) {
    this(errorCode, errorCode.httpStatus(), null, details, args);
  }

  public AppException(ErrorCode errorCode, HttpStatus httpStatus, String message, Map<String, Object> details, Object... args) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = (httpStatus == null ? errorCode.httpStatus() : httpStatus);
    this.details = details;
    this.args = args;
  }

  public ErrorCode getErrorCode() { return errorCode; }
  public HttpStatus getHttpStatus() { return httpStatus; }
  public Object[] getArgs() { return args; }
  public Map<String, Object> getDetails() { return details; }
}
