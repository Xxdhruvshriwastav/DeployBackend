@echo off
set JAVA_HOME=
echo Starting HireConnect Services with memory limits (Max 256MB each)...

:: Set memory limits for ALL Java processes to prevent out-of-memory errors
set JAVA_TOOL_OPTIONS=-Xmx256m -Xms128m

echo Starting Eureka Server...
cd eureka-server
start "Eureka Server" cmd /k "mvnw.cmd spring-boot:run"
cd ..
ping 127.0.0.1 -n 16 > nul

echo Starting API Gateway...
cd api-gateway
start "API Gateway" cmd /k "mvnw.cmd spring-boot:run"
cd ..
ping 127.0.0.1 -n 11 > nul

echo Starting remaining microservices...

cd auth-service
start "Auth Service" cmd /k "mvnw.cmd spring-boot:run"
cd ..

cd application-service
start "Application Service" cmd /k "mvnw.cmd spring-boot:run"
cd ..

cd interview-service
start "Interview Service" cmd /k "mvnw.cmd spring-boot:run"
cd ..

cd job-service
start "Job Service" cmd /k "mvnw.cmd spring-boot:run"
cd ..

cd notification-service
start "Notification Service" cmd /k "mvnw.cmd spring-boot:run"
cd ..

cd payment-service
start "Payment Service" cmd /k "mvnw.cmd spring-boot:run"
cd ..

cd profile-service
start "Profile Service" cmd /k "mvnw.cmd spring-boot:run"
cd ..

cd subscription-service
start "Subscription Service" cmd /k "mvnw.cmd spring-boot:run"
cd ..

cd analytics-service
start "Analytics Service" cmd /k "mvnw.cmd spring-boot:run"
cd ..

echo All services have been launched in separate windows.
echo Done
