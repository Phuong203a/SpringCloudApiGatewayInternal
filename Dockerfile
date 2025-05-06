# --- Stage 1: Build the jar file ---
FROM maven:3.9-eclipse-temurin-21 as builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- Stage 2: Run the app with a lighter JRE ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Set timezone to GMT+7
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Bangkok /etc/localtime && \
    echo "Asia/Bangkok" > /etc/timezone

ENV TZ=Asia/Bangkok

COPY --from=builder /app/target/*.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
