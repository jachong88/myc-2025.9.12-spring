package web.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String requestId,
    T data,
    Map<String, Object> meta,
    ApiError error
) {
  public static <T> ApiResponse<T> success(String requestId, T data, Map<String, Object> meta) {
    return new ApiResponse<>(true, requestId, data, meta, null);
  }
  public static <T> ApiResponse<T> failure(String requestId, ApiError error) {
    return new ApiResponse<>(false, requestId, null, null, error);
  }
}
