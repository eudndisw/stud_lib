# ---------- Етап 1: збірка ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Етап 2: запуск ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
# НФВ (апаратна частина): застосунок вкладається в 1–2 ГБ ОЗП сервера
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
