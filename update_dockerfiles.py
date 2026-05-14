import os

template = """# ─── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", \\
  "-XX:+UseG1GC", \\
  "-Xmx256m", \\
  "-Xms64m", \\
  "-XX:MaxRAMPercentage=75.0", \\
  "-Djava.security.egd=file:/dev/./urandom", \\
  "-jar", "app.jar"]
"""

services = ['analytics-service', 'api-gateway', 'application-service', 'auth-service', 
           'eureka-server', 'interview-service', 'job-service', 'notification-service', 
           'payment-service', 'profile-service', 'subscription-service']

for service in services:
    path = os.path.join(service, 'Dockerfile')
    if os.path.exists(path):
        with open(path, 'w', encoding='utf-8') as f:
            f.write(template)
        print(f'Updated {path}')
