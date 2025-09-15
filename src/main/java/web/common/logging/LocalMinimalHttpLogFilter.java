package web.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Profile("local")
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // after RequestIdFilter
public class LocalMinimalHttpLogFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(LocalMinimalHttpLogFilter.class);
  private static final int MAX_LOG_BYTES = 4096;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    ContentCachingRequestWrapper req =
        (request instanceof ContentCachingRequestWrapper) ? (ContentCachingRequestWrapper) request
            : new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper res =
        (response instanceof ContentCachingResponseWrapper) ? (ContentCachingResponseWrapper) response
            : new ContentCachingResponseWrapper(response);

    long start = System.currentTimeMillis();
    try {
      chain.doFilter(req, res);
    } finally {
      long took = System.currentTimeMillis() - start;

      String method = req.getMethod();
      String uri = req.getRequestURI();
      String qs = req.getQueryString();
      String url = (qs == null) ? uri : uri + "?" + qs;

      // Extract bearer token (redact contents but show presence + first 8 chars)
      String auth = req.getHeader("Authorization");
      String bearer = null;
      if (auth != null && auth.startsWith("Bearer ")) {
        String token = auth.substring(7);
        if (!token.isBlank()) {
          String head = token.length() > 8 ? token.substring(0, 8) : token;
          bearer = "Bearer " + head + "…";
        } else {
          bearer = "Bearer";
        }
      }

      // Extra signal when JWT is missing or not Bearer
      if (auth == null || auth.isBlank()) {
        log.info("[API] {} {} | no_jwt=true", method, url);
      } else if (!auth.startsWith("Bearer ")) {
        log.info("[API] {} {} | auth=present_non_bearer", method, url);
      }

      String reqBody = body(req.getContentAsByteArray(), req.getCharacterEncoding());
      String resBody = body(res.getContentAsByteArray(), res.getCharacterEncoding());

      if (bearer != null) {
        log.info("[API] {} {} ({}ms) | bearer={} | req={} | res={} {}", method, url, took, bearer, reqBody, resBody, res.getStatus());
      } else {
        log.info("[API] {} {} ({}ms) | req={} | res={} {}", method, url, took, reqBody, resBody, res.getStatus());
      }

      res.copyBodyToResponse();
    }
  }

  private String body(byte[] bytes, String enc) {
    if (bytes == null || bytes.length == 0) return "";
    int len = Math.min(bytes.length, MAX_LOG_BYTES);
    String text = new String(bytes, 0, len, encoding(enc));
    if (bytes.length > MAX_LOG_BYTES) {
      return text + "... [truncated " + (bytes.length - MAX_LOG_BYTES) + " bytes]";
    }
    return text;
  }

  private java.nio.charset.Charset encoding(String enc) {
    try {
      return (enc == null) ? StandardCharsets.UTF_8 : java.nio.charset.Charset.forName(enc);
    } catch (Exception e) {
      return StandardCharsets.UTF_8;
    }
  }
}
