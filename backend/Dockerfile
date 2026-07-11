# --- Étape 1 : build du JAR avec Gradle ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew
# télécharge les dépendances (cache) puis compile
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# --- Étape 2 : image d'exécution légère (JRE seul) ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
