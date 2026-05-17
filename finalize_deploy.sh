#!/bin/bash
cd /home/ubuntu/hireconnect

# Update all Dockerfiles to use the built JAR directly
services=(eureka-server api-gateway auth-service profile-service job-service application-service notification-service payment-service subscription-service interview-service analytics-service)

for s in "${services[@]}"; do
  echo "Updating Dockerfile for $s"
  cat << 'EOF_DOCKER' > "$s/Dockerfile"
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-Xmx256m", \
  "-Xms64m", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
EOF_DOCKER
done

echo "Launching containers..."
sudo docker compose up -d
