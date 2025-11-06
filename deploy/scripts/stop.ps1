# DevFlow Stop Script (Windows)

$APP_NAME = "DevFlow"
$PID_FILE = "devflow.pid"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "       Stopping $APP_NAME" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Check if PID file exists
if (-not (Test-Path $PID_FILE)) {
    Write-Host "Warning: PID file not found: $PID_FILE" -ForegroundColor Yellow
    Write-Host "Trying to find process by name..." -ForegroundColor Yellow
    Write-Host ""
    
    # Try to find process by JAR name
    $processes = Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object {
        $_.CommandLine -like "*devflow.jar*"
    }
    
    if (-not $processes) {
        Write-Host "No running $APP_NAME process found" -ForegroundColor Yellow
        exit 0
    }
    
    $APP_PID = $processes[0].Id
} else {
    # Read PID from file
    try {
        $pidContent = Get-Content $PID_FILE -Raw -ErrorAction Stop
        $APP_PID = [int]($pidContent.Trim())
    } catch {
        Write-Host "Error reading PID file: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "Found process: $APP_PID" -ForegroundColor White

# Check if process exists
try {
    $process = Get-Process -Id $APP_PID -ErrorAction Stop
} catch {
    Write-Host "Process $APP_PID does not exist" -ForegroundColor Yellow
    
    # Clean up PID file
    if (Test-Path $PID_FILE) {
        Remove-Item $PID_FILE -Force
    }
    exit 0
}

Write-Host "Stopping process..." -ForegroundColor Yellow

# Try graceful shutdown
try {
    $process.CloseMainWindow() | Out-Null
    
    # Wait for process to exit (max 30 seconds)
    $waitCount = 0
    $maxWait = 30
    
    while (-not $process.HasExited -and $waitCount -lt $maxWait) {
        Write-Host "." -NoNewline -ForegroundColor Yellow
        Start-Sleep -Seconds 1
        $waitCount++
        
        # Refresh process state
        try {
            $process = Get-Process -Id $APP_PID -ErrorAction Stop
        } catch {
            # Process has exited
            break
        }
    }
    
    Write-Host ""
    
    # Check if process has exited
    try {
        $checkProcess = Get-Process -Id $APP_PID -ErrorAction Stop
        
        # Process still running, force kill
        Write-Host "Process did not respond to graceful shutdown" -ForegroundColor Yellow
        Write-Host "Forcing termination..." -ForegroundColor Yellow
        
        Stop-Process -Id $APP_PID -Force
        Start-Sleep -Seconds 2
        
        # Verify process is stopped
        try {
            $verifyProcess = Get-Process -Id $APP_PID -ErrorAction Stop
            Write-Host "Failed to stop process $APP_PID" -ForegroundColor Red
            exit 1
        } catch {
            Write-Host ""
            Write-Host "Application stopped (forced)" -ForegroundColor Green
        }
        
    } catch {
        Write-Host ""
        Write-Host "Application stopped successfully" -ForegroundColor Green
    }
    
} catch {
    Write-Host ""
    Write-Host "Error stopping process: $_" -ForegroundColor Red
    exit 1
}

# Clean up PID file
if (Test-Path $PID_FILE) {
    Remove-Item $PID_FILE -Force
    Write-Host "PID file cleaned up" -ForegroundColor White
}

Write-Host ""
