#docker run -d --name keycloak -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v keycloak_data:/opt/keycloak/data quay.io/keycloak/keycloak:latest start-dev

# Complete rebuild and restart script
# Rebuilds Maven project, Docker image, and docker-compose stack

Write-Host "=== EMS Command Center Complete Rebuild ===" -ForegroundColor Cyan

# Step 1: Build Maven project
Write-Host "`n[1/4] Building Maven project..." -ForegroundColor Yellow
mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Maven build completed" -ForegroundColor Green

# Step 2: Stop running containers
Write-Host "`n[2/4] Stopping Docker containers..." -ForegroundColor Yellow
docker-compose down
Write-Host "Containers stopped" -ForegroundColor Green

# Step 3: Remove old image
Write-Host "`n[3/4] Rebuilding Docker image..." -ForegroundColor Yellow
docker image rm ems-backend:dev -f 2>$null
docker build -t ems-backend:dev .
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Docker image built" -ForegroundColor Green

# Step 4: Start full stack
Write-Host "`n[4/4] Starting Docker stack..." -ForegroundColor Yellow
docker-compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker compose failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Docker stack started" -ForegroundColor Green

Write-Host "`n=== Complete! ===" -ForegroundColor Cyan
Write-Host "Backend: http://localhost:8081" -ForegroundColor Cyan
Write-Host "Mongo Express: http://localhost:8082" -ForegroundColor Cyan
