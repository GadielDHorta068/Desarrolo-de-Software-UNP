# Etapa BUILD
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

#Cache dependencias
COPY pom.xml .
#Instala dependencias del backend
RUN mvn -q -e -DskipTests dependency:go-offline

#Compilar .JAR
COPY src ./src
RUN mvn -q -e -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

#Aca despues poner el hardware para el contenedor (como los parametros de minecraft)
ENV JAVA_OPTS=""

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE ${APP_PORT:-8080}

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

