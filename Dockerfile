FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --create-home appuser
COPY --from=build /workspace/target/CineTown-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
