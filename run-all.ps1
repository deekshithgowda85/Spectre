$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

if (Test-Path (Join-Path $root ".env")) {
    Get-Content (Join-Path $root ".env") | ForEach-Object {
        if ($_ -match "^\s*([^#=\s]+)\s*=\s*(.*)\s*$") {
            [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
        }
    }
}

$services = @(
    "service-registry",
    "auth-service",
    "incident-service",
    "dashboard-service",
    "ping-service",
    "anomaly-service",
    "scraper-service",
    "api-gateway"
)

foreach ($service in $services) {
    $path = Join-Path $root $service
    Start-Process powershell.exe -WorkingDirectory $path -ArgumentList @(
        "-NoExit",
        "-Command",
        ".\mvnw.cmd spring-boot:run"
    )
    Write-Host "Started $service" -ForegroundColor Green
}

$dashboardPath = Join-Path $root "dashboard"
Start-Process powershell.exe -WorkingDirectory $dashboardPath -ArgumentList @(
    "-NoExit",
    "-Command",
    "npm run dev"
)
Write-Host "Started dashboard" -ForegroundColor Green
Write-Host "Dashboard: http://localhost:3000" -ForegroundColor Cyan
Write-Host "Gateway:   http://localhost:8080" -ForegroundColor Cyan
