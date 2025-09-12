package web.common.querydsl;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.ConnectionProvider;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class QuerydslConfig {

  @Bean
  @Primary
  public SQLTemplates sqlTemplates() {
    return PostgreSQLTemplates.builder()
        .printSchema()
        .build();
  }

  @Bean
  public Configuration querydslConfiguration(SQLTemplates templates) {
    Configuration conf = new Configuration(templates);
    conf.setUseLiterals(true);
    return conf;
  }

  @Bean
  public SQLQueryFactory sqlQueryFactory(DataSource dataSource, Configuration configuration) {
    ConnectionProvider provider = new ConnectionProvider() {
      @Override
      public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
      }
    };
    return new SQLQueryFactory(configuration, provider);
  }
}
