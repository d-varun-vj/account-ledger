FROM eclipse-temurin:25-jre-alpine
VOLUME /tmp
COPY build/libs/accountledger-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]