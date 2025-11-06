# DevFlow Project Build Script (PowerShell)
# Usage:
#   .\build.ps1              # Build with current version
#   .\build.ps1 -Version 1.0.0  # Build with specified version

param(
    [string]$Version = ""
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "       DevFlow Project Build" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Set version if provided
if ($Version -ne "") {
    Write-Host "Setting version to: $Version" -ForegroundColor Yellow
    mvn versions:set -DnewVersion=$Version
    mvn versions:commit
    Write-Host ""
}

# Clean old files
Write-Host "Cleaning old files..." -ForegroundColor Yellow
mvn clean
Write-Host ""

# Build project
Write-Host "Building project (skipping tests)..." -ForegroundColor Yellow
mvn package -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "         Build Success!" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Build artifacts:" -ForegroundColor Cyan
    Write-Host "  Backend JAR: " -NoNewline -ForegroundColor Cyan
    Write-Host "backend\target\devflow.jar" -ForegroundColor White
    Write-Host "  Frontend files: " -NoNewline -ForegroundColor Cyan
    Write-Host "frontend\target\dist\" -ForegroundColor White
    Write-Host ""
    
    # Display version information
    Write-Host "Version information:" -ForegroundColor Cyan
    if (Test-Path "backend\target\classes\application.properties") {
        $props = Get-Content "backend\target\classes\application.properties" -Encoding UTF8 | Select-String "application.version|application.build.time"
        $props | ForEach-Object {
            Write-Host "  $_" -ForegroundColor White
        }
    }
    Write-Host ""
    
    # Display JAR file size
    if (Test-Path "backend\target\devflow.jar") {
        $jarSize = (Get-Item "backend\target\devflow.jar").Length / 1MB
        Write-Host "JAR file size: " -NoNewline -ForegroundColor Cyan
        Write-Host ("{0:N2} MB" -f $jarSize) -ForegroundColor White
    }
    Write-Host ""
    
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Verify build artifacts" -ForegroundColor White
    Write-Host "  2. Upload to server" -ForegroundColor White
    Write-Host "  3. Configure environment variables" -ForegroundColor White
    Write-Host "  4. Start application" -ForegroundColor White
    Write-Host ""
    
} else {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host "         Build Failed!" -ForegroundColor Red
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please check error messages and fix issues" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}
