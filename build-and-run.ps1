# build-and-run.ps1
# This script builds the entire project locally and then starts Docker containers.
# Requirements: Java 21, Maven, Node.js installed on host.

$ErrorActionPreference = "Stop"

# Function to check requirements
function Check-Requirement($cmd, $name) {
    if (-not (Get-Command $cmd -ErrorAction SilentlyContinue)) {
        Write-Host "Error: $name is not installed or not in PATH." -ForegroundColor Red
        return $false
    }
    return $true
}

# 0. Check for necessary tools
$ok = $true
$ok = $ok -and (Check-Requirement "java" "Java")
$ok = $ok -and (Check-Requirement "npm" "Node.js/NPM")
$ok = $ok -and (Check-Requirement "docker" "Docker")

if (-not $ok) {
    Write-Host "`nPlease install the missing requirements and try again." -ForegroundColor Yellow
    exit 1
}

# Ensure standard PowerShell path is present (safety for some Windows environments)
if ($env:Path -notlike "*WindowsPowerShell*") {
    $env:Path = "C:\Windows\System32\WindowsPowerShell\v1.0;$env:Path"
}

# Use JAVA_HOME if set, otherwise assume java is in PATH
if ($env:JAVA_HOME) {
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

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
Write-Host "`n[3/3] Starting Docker Containers..." -ForegroundColor Yellow
Set-Location "$rootPath\infrastructure"
docker-compose up -d --build

Write-Host "`n====================================================" -ForegroundColor Green
Write-Host "   Deployment Completed Successfully!              " -ForegroundColor Green
Write-Host "   Frontend: http://localhost:3000                 " -ForegroundColor Green
Write-Host "   API Gateway: http://localhost:8080              " -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Green

Set-Location $rootPath
