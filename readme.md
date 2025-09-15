// run with local
mvn -q spring-boot:run -D"spring-boot.run.profiles=local"

// run withou local
./mvnw.cmd -DskipTests spring-boot:run

// to see log
mvn spring-boot:run -Dspring-boot.run.profiles=local

// set google credential in settings, else startup error:
C:\d\code-backup\firebase_token\firebase-service-account.json
