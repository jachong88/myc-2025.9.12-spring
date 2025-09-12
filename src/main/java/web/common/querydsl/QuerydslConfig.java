package web.common.querydsl;

import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
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
  public com.querydsl.sql.Configuration querydslConfiguration(SQLTemplates templates) {
    com.querydsl.sql.Configuration conf = new com.querydsl.sql.Configuration(templates);
    conf.setUseLiterals(true);
    return conf;
  }

  @Bean
  public SQLQueryFactory sqlQueryFactory(DataSource dataSource, com.querydsl.sql.Configuration configuration) {
    Supplier<Connection> supplier = () -> {
      try {
        return dataSource.getConnection();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    };
    return new SQLQueryFactory(configuration, supplier);
  }
}
