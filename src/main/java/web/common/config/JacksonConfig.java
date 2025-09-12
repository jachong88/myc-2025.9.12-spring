package web.common.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
  // Spring Boot 3 uses sane defaults with JavaTimeModule and ISO-8601.
  // If needed later, we can customize ObjectMapper here.
}
