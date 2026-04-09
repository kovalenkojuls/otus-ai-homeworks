FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . /app
RUN chmod +x /app/mvnw
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/questionnaire-0.0.1-SNAPSHOT.jar"]
