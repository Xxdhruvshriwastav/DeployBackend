# Stop all running Java processes first
# Force JDK 21
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Using Java from: $env:JAVA_HOME"
try {
    taskkill /f /im java.exe
} catch {
    Write-Host "No Java processes found or error stopping."
}

# Set Java version to JDK 21
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Set Maven memory limits to prevent out-of-memory errors
$env:MAVEN_OPTS = "-Xmx256m -Xms128m"

$services = @(
    "eureka-server",
    "api-gateway",
    "auth-service",
    "profile-service",
    "job-service",
    "application-service",
    "subscription-service",
    "payment-service",
    "notification-service",
    "interview-service",
    "analytics-service"
)

foreach ($service in $services) {
    Write-Host "Starting $service..."
    $servicePath = Join-Path (Get-Location) $service
    
    Start-Process cmd -ArgumentList "/c mvnw.cmd spring-boot:run" -WorkingDirectory $servicePath -WindowStyle Minimized
    
    if ($service -eq "eureka-server") {
        Write-Host "Waiting 20 seconds for Eureka Server to stabilize..."
        Start-Sleep -Seconds 20
    } elseif ($service -eq "api-gateway") {
        Write-Host "Waiting 10 seconds for API Gateway..."
        Start-Sleep -Seconds 10
    } else {
        Start-Sleep -Seconds 2
    }
}

Write-Host "All services started."
