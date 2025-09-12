package web.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    int status,
    String code,
    String message,
    Map<String, Object> details
) {}
