package web.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import web.common.dto.ApiError;
import web.common.dto.ApiResponse;
import web.common.exception.ErrorCode;
import web.common.i18n.ErrorMessageResolver;
import web.common.request.RequestIdHolder;

@Configuration
public class SecurityConfig {

  private final FirebaseAuthFilter firebaseAuthFilter;
  private final ObjectMapper objectMapper;
  private final ErrorMessageResolver messages;

  public SecurityConfig(FirebaseAuthFilter firebaseAuthFilter, ObjectMapper objectMapper, ErrorMessageResolver messages) {
    this.firebaseAuthFilter = firebaseAuthFilter;
    this.objectMapper = objectMapper;
    this.messages = messages;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(h -> h
        .authenticationEntryPoint(authenticationEntryPoint())
        .accessDeniedHandler(accessDeniedHandler())
      )
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
        .requestMatchers("/actuator/**", "/error").permitAll()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
        .anyRequest().authenticated()
      )
      .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
      .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) -> {
      String requestId = RequestIdHolder.getOrCreate();
      String msg = messages.resolve(ErrorCode.UNAUTHENTICATED.messageKey());
      ApiError err = new ApiError(HttpStatus.UNAUTHORIZED.value(), ErrorCode.UNAUTHENTICATED.code(), msg, null);
      ApiResponse<Void> body = ApiResponse.failure(requestId, err);
      writeEnvelope(response, HttpStatus.UNAUTHORIZED, body);
    };
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
      String requestId = RequestIdHolder.getOrCreate();
      String msg = messages.resolve(ErrorCode.ACCESS_DENIED.messageKey());
      ApiError err = new ApiError(HttpStatus.FORBIDDEN.value(), ErrorCode.ACCESS_DENIED.code(), msg, null);
      ApiResponse<Void> body = ApiResponse.failure(requestId, err);
      writeEnvelope(response, HttpStatus.FORBIDDEN, body);
    };
  }

  private void writeEnvelope(javax.servlet.http.HttpServletResponse response, HttpStatus status, ApiResponse<?> body) throws IOException {
    response.setStatus(status.value());
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
