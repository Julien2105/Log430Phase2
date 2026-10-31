# Construction multi-étapes. Contexte de build : la racine du dépôt.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -B -pl catalog-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 candronex
COPY --from=build /workspace/catalog-service/target/catalog-service-2.0.0.jar app.jar
USER candronex
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
