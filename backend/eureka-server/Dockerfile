FROM openjdk:21-jre-slim

WORKDIR /app

COPY target/eureka-server-1.0.0.jar app.jar

EXPOSE 8762

ENTRYPOINT ["java", "-jar", "app.jar"]