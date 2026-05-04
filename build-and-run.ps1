# build-and-run.ps1
# This script builds the entire project locally and then starts Docker containers.
# Requirements: Java 21, Maven, Node.js installed on host.

$ErrorActionPreference = "Stop"

# Force use Java 21 from user's path
# $env:JAVA_HOME = "C:\Users\longx\.jdks\ms-21.0.11"
# $env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "   Flood Rescue System - Local Build & Deploy      " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

$rootPath = Get-Location

# 1. Build Java Services
$javaServices = @("gateway", "auth-service", "request-service", "dispatch-service", "resource-service", "notification-service", "report-service")

Write-Host "`n[1/3] Building Java Services..." -ForegroundColor Yellow
foreach ($service in $javaServices) {
    if (Test-Path "$rootPath\$service") {
        Write-Host "--- Building $service ---" -ForegroundColor Gray
        Set-Location "$rootPath\$service"
        
        # Use Maven Wrapper if available, otherwise fallback to global mvn
        if (Test-Path "mvnw.cmd") {
            .\mvnw.cmd clean package -DskipTests -q
        } else {
            mvn clean package -DskipTests -q
        }

        if ($LASTEXITCODE -ne 0) {
            Write-Host "Error: Failed to build $service" -ForegroundColor Red
            Set-Location $rootPath
            exit 1
        }
    } else {
        Write-Host "Warning: Service $service not found at $rootPath\$service" -ForegroundColor Red
    }
}

# 2. Build Frontend
Write-Host "`n[2/3] Building Frontend (Next.js)..." -ForegroundColor Yellow
if (Test-Path "$rootPath\frontend") {
    Set-Location "$rootPath\frontend"
    Write-Host "--- Installing dependencies ---" -ForegroundColor Gray
    npm install --quiet
    Write-Host "--- Running production build ---" -ForegroundColor Gray
    npm run build
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Failed to build Frontend" -ForegroundColor Red
        Set-Location $rootPath
        exit 1
    }
} else {
    Write-Host "Error: Frontend directory not found!" -ForegroundColor Red
    Set-Location $rootPath
    exit 1
}

# 3. Docker Compose
Write-Host "`n[3/3] Starting Backend Containers..." -ForegroundColor Yellow
Set-Location "$rootPath\infrastructure"
docker-compose up -d --build zipkin gateway auth-service request-service dispatch-service resource-service notification-service report-service

Write-Host "`n====================================================" -ForegroundColor Green
Write-Host "   Deployment Completed Successfully!              " -ForegroundColor Green
Write-Host "   Frontend: run separately with npm run dev       " -ForegroundColor Green
Write-Host "   API Gateway: http://localhost:8080              " -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Green

Set-Location $rootPath
