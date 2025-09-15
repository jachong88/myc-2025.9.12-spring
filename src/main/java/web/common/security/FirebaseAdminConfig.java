package web.common.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@ConditionalOnProperty(value = "firebase.enabled", havingValue = "true", matchIfMissing = true)
public class FirebaseAdminConfig {

  @Value("${firebase.credentials-path:}")
  private String credentialsPath;

  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      GoogleCredentials credentials;
      if (credentialsPath != null && !credentialsPath.isBlank()) {
        try (FileInputStream fis = new FileInputStream(credentialsPath)) {
          credentials = GoogleCredentials.fromStream(fis);
        }
      } else {
        credentials = GoogleCredentials.getApplicationDefault();
      }

      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(credentials)
          .build();
      return FirebaseApp.initializeApp(options);
    }
    return FirebaseApp.getInstance();
  }

  @Bean
  public FirebaseAuth firebaseAuth(FirebaseApp app) {
    return FirebaseAuth.getInstance(app);
  }
}
