# Multi-stage build: the first stage BUILDS the JAR, the second stage only RUNS it.
# The final image has no Maven and no source code, so it is much smaller.

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy pom.xml first and download the libraries. Docker caches this layer,
# so it is repeated only when pom.xml changes (not when you change a Java file).
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Now copy the code and build target/classchat.jar (tests are skipped here: run them with ./mvnw test)
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Stage 2: run ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Do not run the application as root
RUN useradd --system --no-create-home classchat
USER classchat

COPY --from=build /build/target/classchat.jar app.jar

# The app listens on the port in the environment variable PORT (default 8080, see application.properties)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
