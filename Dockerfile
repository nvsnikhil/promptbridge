# Use a lightweight OpenJDK 21 image
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/promptbridge-0.0.1-SNAPSHOT.jar"]
