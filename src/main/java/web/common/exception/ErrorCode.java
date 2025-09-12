package web.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  VALIDATION_FAILED("VALIDATION_FAILED", "error.validation_failed", HttpStatus.BAD_REQUEST),
  ARGUMENT_INVALID("ARGUMENT_INVALID", "error.argument_invalid", HttpStatus.BAD_REQUEST),
  UNAUTHENTICATED("UNAUTHENTICATED", "error.unauthenticated", HttpStatus.UNAUTHORIZED),
  ACCESS_DENIED("ACCESS_DENIED", "error.access_denied", HttpStatus.FORBIDDEN),
  RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "error.resource_not_found", HttpStatus.NOT_FOUND),
  DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "error.duplicate_resource", HttpStatus.CONFLICT),
  CONFLICT("CONFLICT", "error.conflict", HttpStatus.CONFLICT),
  RATE_LIMITED("RATE_LIMITED", "error.rate_limited", HttpStatus.TOO_MANY_REQUESTS),
  INTERNAL("INTERNAL", "error.internal", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String messageKey;
  private final HttpStatus httpStatus;

  ErrorCode(String code, String messageKey, HttpStatus httpStatus) {
    this.code = code;
    this.messageKey = messageKey;
    this.httpStatus = httpStatus;
  }

  public String code() { return code; }
  public String messageKey() { return messageKey; }
  public HttpStatus httpStatus() { return httpStatus; }
}
