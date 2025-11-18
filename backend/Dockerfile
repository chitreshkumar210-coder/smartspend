# ============================
# BUILD STAGE
# ============================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Maven wrapper and config
COPY mvnw .
COPY pom.xml .
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy project source
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests


# ============================
# RUNTIME STAGE
# ============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy packaged jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose backend port
EXPOSE 8080

# Makes Render read env vars (important)
ENV JAVA_OPTS=""

# Start app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
