FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src/ src/
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build --chown=1001:0 /workspace/target/auth-service-*.jar /app/app.jar

ENV HOME=/tmp
EXPOSE 8085
USER 1001:0

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
