# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
RUN apk update && apk upgrade && \
    apk add --no-cache git && \
    rm -rf /var/cache/apk/*
COPY . /app
RUN ./mvnw package
CMD ["java", "-jar", "target/spring-0.0.1-SNAPSHOT.jar"]
EXPOSE 8585
