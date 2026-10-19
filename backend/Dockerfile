FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Download the New Relic Java agent
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar /app/newrelic.jar

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-javaagent:/app/newrelic.jar", "-jar", "app.jar"]
