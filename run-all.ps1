param(
    [switch]$SkipFrontend,
    [switch]$SkipOptional
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$envPath = Join-Path $root ".env"

if (-not (Test-Path $envPath)) {
    throw "Missing .env. Copy .env.example to .env and set Neon and JWT values first."
}

Get-Content $envPath | ForEach-Object {
    if ($_ -match '^([^#][^=]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], "Process")
    }
}

function Start-ServiceWindow([string]$name, [string]$directory, [string]$command) {
    $path = Join-Path $root $directory
    Start-Process powershell.exe -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location '$path'; Write-Host 'Starting $name'; $command"
    ) | Out-Null
}

Start-ServiceWindow "service-registry" "service-registry" ".\mvnw.cmd spring-boot:run"
Start-ServiceWindow "auth-service" "auth-service" ".\mvnw.cmd spring-boot:run"
Start-ServiceWindow "incident-service" "incident-service" ".\mvnw.cmd spring-boot:run"
Start-ServiceWindow "dashboard-service" "dashboard-service" ".\mvnw.cmd spring-boot:run"
Start-ServiceWindow "api-gateway" "api-gateway" ".\mvnw.cmd spring-boot:run"
Start-ServiceWindow "scraper-service" "scraper-service" ".\mvnw.cmd spring-boot:run"
Start-ServiceWindow "ping-service" "ping-service" ".\mvnw.cmd spring-boot:run"

if (-not $SkipOptional) {
    Start-ServiceWindow "anomaly-service" "anomaly-service" ".\mvnw.cmd spring-boot:run"
}

if (-not $SkipFrontend) {
    Start-ServiceWindow "dashboard" "dashboard" "npm run dev"
}

Write-Host "Spectre services are launching in separate PowerShell windows."
Write-Host "Dashboard: http://localhost:3000"
Write-Host "Gateway:   http://localhost:8080"
