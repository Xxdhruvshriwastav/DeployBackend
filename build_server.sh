#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
cd /home/ubuntu/hireconnect
services=(eureka-server api-gateway auth-service profile-service job-service application-service notification-service payment-service subscription-service interview-service analytics-service)
for s in "${services[@]}"; do
  echo "Building $s on server..."
  cd "$s"
  mvn clean package -DskipTests -B
  cd ..
done
echo "All builds finished on server!"
