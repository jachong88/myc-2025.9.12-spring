package web.common.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import web.common.request.RequestIdHolder;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

  private final FirebaseAuth firebaseAuth;

  public FirebaseAuthFilter(FirebaseAuth firebaseAuth) {
    this.firebaseAuth = firebaseAuth;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      try {
        FirebaseToken decoded = firebaseAuth.verifyIdToken(token, true);
        String email = decoded.getEmail();
        if (email == null || email.isBlank()) {
          email = decoded.getUid();
        }
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (FirebaseAuthException e) {
        // Invalid token; leave unauthenticated and proceed.
      }
    }

    try {
      chain.doFilter(request, response);
    } finally {
      String requestId = RequestIdHolder.get();
      if (requestId != null) {
        MDC.put("requestId", requestId);
      }
    }
  }
}
