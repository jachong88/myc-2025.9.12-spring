package web.common.request;

import java.io.IOException;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import web.common.util.Ulids;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

  private static final Pattern ULID_PATTERN = Pattern.compile("^[0-7][0-9A-HJKMNP-TV-Z]{25}$");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader("X-Request-Id");
    String requestId = (header != null && ULID_PATTERN.matcher(header).matches())
        ? header
        : Ulids.newUlid();

    MDC.put("requestId", requestId);
    RequestIdHolder.set(requestId);

    response.setHeader("X-Request-Id", requestId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      RequestIdHolder.clear();
      MDC.remove("requestId");
    }
  }
}
