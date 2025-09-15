package web.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(CorsProps.class)
public class CorsConfig {

  @Bean
  public CorsConfiguration corsConfiguration(CorsProps props) {
    CorsConfiguration cfg = new CorsConfiguration();
    if (props.allowedOrigins() != null && !props.allowedOrigins().isEmpty()) {
      cfg.setAllowedOrigins(props.allowedOrigins());
    }
    if (props.allowedOriginPatterns() != null && !props.allowedOriginPatterns().isEmpty()) {
      cfg.setAllowedOriginPatterns(props.allowedOriginPatterns());
    }
    if (props.allowedMethods() != null) cfg.setAllowedMethods(props.allowedMethods());
    if (props.allowedHeaders() != null) cfg.setAllowedHeaders(props.allowedHeaders());
    if (props.exposedHeaders() != null) cfg.setExposedHeaders(props.exposedHeaders());
    if (props.allowCredentials() != null) cfg.setAllowCredentials(props.allowCredentials());
    if (props.maxAge() != null) cfg.setMaxAge(props.maxAge());
    return cfg;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(CorsConfiguration cfg) {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
}
