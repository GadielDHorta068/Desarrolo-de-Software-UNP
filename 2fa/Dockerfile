FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY test-api.sh /app/test-api.sh
RUN chmod +x /app/test-api.sh
HEALTHCHECK --interval=30s --timeout=3s CMD curl -fsS http://localhost:8080/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]