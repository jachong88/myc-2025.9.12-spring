package web.common.exception;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import web.common.dto.ApiError;
import web.common.dto.ApiResponse;
import web.common.i18n.ErrorMessageResolver;
import web.common.request.RequestIdHolder;

@RestControllerAdvice
public class ApiExceptionHandler {

  private final ErrorMessageResolver messages;

  public ApiExceptionHandler(ErrorMessageResolver messages) {
    this.messages = messages;
  }

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex, Locale locale) {
String requestId = RequestIdHolder.getOrCreate();
    ErrorCode code = ex.getErrorCode();
    HttpStatus status = ex.getHttpStatus();
    String message = messages.resolve(code.messageKey(), ex.getArgs());
    ApiError error = new ApiError(status.value(), code.code(), message, ex.getDetails());
    return new ResponseEntity<>(ApiResponse.failure(requestId, error), status);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, Locale locale) {
String requestId = RequestIdHolder.getOrCreate();
    ErrorCode code = ErrorCode.VALIDATION_FAILED;
    HttpStatus status = code.httpStatus();
    Map<String, Object> details = new HashMap<>();
    Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(FieldError::getField, fe -> fe.getDefaultMessage() == null ? "" : fe.getDefaultMessage(), (a, b) -> a));
    details.put("fields", fieldErrors);
    String message = messages.resolve(code.messageKey());
    ApiError error = new ApiError(status.value(), code.code(), message, details);
    return new ResponseEntity<>(ApiResponse.failure(requestId, error), status);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex, Locale locale) {
String requestId = RequestIdHolder.getOrCreate();
    ErrorCode code = ErrorCode.ARGUMENT_INVALID;
    HttpStatus status = code.httpStatus();
    Map<String, Object> details = new HashMap<>();
    Map<String, String> violations = ex.getConstraintViolations().stream()
        .collect(Collectors.toMap(
            v -> v.getPropertyPath().toString(),
            ConstraintViolation::getMessage,
            (a, b) -> a
        ));
    details.put("violations", violations);
    String message = messages.resolve(code.messageKey());
    ApiError error = new ApiError(status.value(), code.code(), message, details);
    return new ResponseEntity<>(ApiResponse.failure(requestId, error), status);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex, Locale locale) {
String requestId = RequestIdHolder.getOrCreate();
    ErrorCode code = ErrorCode.INTERNAL;
    HttpStatus status = code.httpStatus();
    String message = messages.resolve(code.messageKey());
    ApiError error = new ApiError(status.value(), code.code(), message, null);
    return new ResponseEntity<>(ApiResponse.failure(requestId, error), status);
  }
}
