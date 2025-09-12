package web.common.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
  private CurrentUser() {}

  public static Optional<String> email() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) return Optional.empty();
    Object principal = auth.getPrincipal();
    if (principal == null) return Optional.empty();
    return Optional.of(principal.toString());
  }
}
