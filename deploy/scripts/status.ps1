# DevFlow Status Check Script (Windows)

$APP_NAME = "DevFlow"
$PID_FILE = "devflow.pid"
$APP_PORT = 8099

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "       $APP_NAME Status" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Check if PID file exists
if (Test-Path $PID_FILE) {
    try {
        $pidContent = Get-Content $PID_FILE -Raw -ErrorAction Stop
        $APP_PID = [int]($pidContent.Trim())
        
        Write-Host "PID File: Found ($APP_PID)" -ForegroundColor Green
        
        # Check if process is running
        try {
            $process = Get-Process -Id $APP_PID -ErrorAction Stop
            
            Write-Host "Process Status: Running" -ForegroundColor Green
            Write-Host ""
            Write-Host "Process Details:" -ForegroundColor Cyan
            Write-Host "  Process ID: $($process.Id)" -ForegroundColor White
            Write-Host "  Process Name: $($process.ProcessName)" -ForegroundColor White
            Write-Host "  Start Time: $($process.StartTime)" -ForegroundColor White
            
            # Calculate uptime
            $uptime = (Get-Date) - $process.StartTime
            Write-Host "  Uptime: $($uptime.Days)d $($uptime.Hours)h $($uptime.Minutes)m $($uptime.Seconds)s" -ForegroundColor White
            
            # Memory usage
            $memoryMB = [math]::Round($process.WorkingSet64 / 1MB, 2)
            Write-Host "  Memory Usage: $memoryMB MB" -ForegroundColor White
            
            # CPU time
            Write-Host "  CPU Time: $($process.TotalProcessorTime.ToString('hh\:mm\:ss'))" -ForegroundColor White
            
        } catch {
            Write-Host "Process Status: Not Running (PID $APP_PID not found)" -ForegroundColor Red
            Write-Host "Cleaning up stale PID file..." -ForegroundColor Yellow
            Remove-Item $PID_FILE -Force
        }
        
    } catch {
        Write-Host "PID File: Error reading file" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Yellow
    }
} else {
    Write-Host "PID File: Not found" -ForegroundColor Yellow
    Write-Host "Process Status: Unknown" -ForegroundColor Yellow
}

Write-Host ""

# Check port status
Write-Host "Port Check:" -ForegroundColor Cyan
$portConnection = Get-NetTCPConnection -LocalPort $APP_PORT -State Listen -ErrorAction SilentlyContinue

if ($portConnection) {
    Write-Host "  Port ${APP_PORT}: In Use (Listening)" -ForegroundColor Green
    
    # Try to get process using the port
    $portProcess = Get-Process -Id $portConnection.OwningProcess -ErrorAction SilentlyContinue
    if ($portProcess) {
        Write-Host "  Process: $($portProcess.ProcessName) (PID: $($portProcess.Id))" -ForegroundColor White
    }
} else {
    Write-Host "  Port ${APP_PORT}: Available (Not in use)" -ForegroundColor Yellow
}

Write-Host ""

# Try to access health endpoint
Write-Host "Health Check:" -ForegroundColor Cyan
try {
    $healthUrl = "http://localhost:$APP_PORT/api/health"
    $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5 -ErrorAction Stop
    
    Write-Host "  Health API: Responding" -ForegroundColor Green
    Write-Host "  Status: $($health.status)" -ForegroundColor White
    
} catch {
    Write-Host "  Health API: Not responding" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""

# Try to get version information
Write-Host "Version Information:" -ForegroundColor Cyan
try {
    $versionUrl = "http://localhost:$APP_PORT/api/version"
    $version = Invoke-RestMethod -Uri $versionUrl -TimeoutSec 5 -ErrorAction Stop
    
    Write-Host "  Application: $($version.application)" -ForegroundColor White
    Write-Host "  Version: $($version.version)" -ForegroundColor White
    Write-Host "  Build Time: $($version.buildTime)" -ForegroundColor White
    if ($version.gitCommitId -and $version.gitCommitId -ne "unknown") {
        Write-Host "  Git Commit: $($version.gitCommitId)" -ForegroundColor White
    }
    if ($version.gitBranch -and $version.gitBranch -ne "unknown") {
        Write-Host "  Git Branch: $($version.gitBranch)" -ForegroundColor White
    }
    
} catch {
    Write-Host "  Version API: Not available" -ForegroundColor Yellow
}

Write-Host ""

# Summary
Write-Host "==========================================" -ForegroundColor Cyan

if ($portConnection -and (Test-Path $PID_FILE)) {
    Write-Host "Overall Status: RUNNING" -ForegroundColor Green
    Write-Host ""
    Write-Host "Access application at: http://localhost:${APP_PORT}" -ForegroundColor Cyan
} elseif ($portConnection) {
    Write-Host "Overall Status: RUNNING (No PID file)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Application is running but may not have been started by start-prod.ps1" -ForegroundColor Yellow
} else {
    Write-Host "Overall Status: STOPPED" -ForegroundColor Red
    Write-Host ""
    Write-Host "Start application with: .\start-prod.ps1" -ForegroundColor Yellow
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
