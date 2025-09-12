package web.codegen;

import com.querydsl.sql.codegen.MetaDataExporter;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class QuerydslExporter {

  // Arguments are passed from the pom profile:
  // 0: DB_URL
  // 1: DB_USERNAME
  // 2: DB_PASSWORD
  // 3: OUTPUT_DIR
  public static void main(String[] args) throws Exception {
    String url = args[0];
    String username = args[1];
    String password = args[2];
    String outDir = args[3];

    MetaDataExporter exporter = new MetaDataExporter();
    exporter.setTargetFolder(new File(outDir));
    exporter.setPackageName("web.generated");

    exporter.setSchemaPattern(null);
    exporter.setTableNamePattern("(country|province|users)");
    exporter.setExportPrimaryKeys(true);
    exporter.setExportForeignKeys(true);

    try (Connection conn = DriverManager.getConnection(url, username, password)) {
      exporter.export(conn.getMetaData());
    }
  }
}
