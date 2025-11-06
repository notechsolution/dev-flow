# DevFlow Restart Script (Windows)

$APP_NAME = "DevFlow"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "       Restarting $APP_NAME" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Stop the application
Write-Host "Step 1: Stopping application..." -ForegroundColor Yellow
& .\stop.ps1

if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne $null) {
    Write-Host "Warning: Stop script returned error code $LASTEXITCODE" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Waiting 3 seconds before restart..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
Write-Host ""

# Start the application
Write-Host "Step 2: Starting application..." -ForegroundColor Yellow
& .\start-prod.ps1

if ($LASTEXITCODE -eq 0 -or $LASTEXITCODE -eq $null) {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "       Restart Complete" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host "       Restart Failed" -ForegroundColor Red
    Write-Host "==========================================" -ForegroundColor Red
    exit 1
}
