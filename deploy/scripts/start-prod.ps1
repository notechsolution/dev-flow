# DevFlow Production Environment Start Script (Windows)
# Please modify the following configuration according to your environment

# ==================== Configuration ====================

# Application Configuration
$APP_NAME = "DevFlow"
$APP_JAR = "backend\target\devflow.jar"
$APP_PORT = 8099
$SPRING_PROFILE = "prod"

# JVM Configuration
$JAVA_OPTS = "-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# AI Configuration
$env:AI_PROVIDER = "qwen"
$env:DASHSCOPE_API_KEY = "your-dashscope-api-key-here"

# Database Configuration
$env:MONGODB_URI = "mongodb://username:password@10.222.39.208:27017/devflow"

# Mail Configuration
$env:MAIL_HOST = "smtp.163.com"
$env:MAIL_PORT = "465"
$env:MAIL_USERNAME = "your-email@163.com"
$env:MAIL_PASSWORD = "your-email-authorization-code"

# Git Information (Optional)
try {
    $env:GIT_COMMIT_ID = git rev-parse HEAD 2>$null
    if (-not $env:GIT_COMMIT_ID) { $env:GIT_COMMIT_ID = "unknown" }
} catch {
    $env:GIT_COMMIT_ID = "unknown"
}

try {
    $env:GIT_BRANCH = git rev-parse --abbrev-ref HEAD 2>$null
    if (-not $env:GIT_BRANCH) { $env:GIT_BRANCH = "unknown" }
} catch {
    $env:GIT_BRANCH = "unknown"
}

# Log Configuration
$LOG_DIR = "logs"
$LOG_FILE = "$LOG_DIR\devflow.log"
$PID_FILE = "devflow.pid"

# ==================== Script Logic ====================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "       Starting $APP_NAME" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Create log directory
if (-not (Test-Path $LOG_DIR)) {
    New-Item -ItemType Directory -Path $LOG_DIR | Out-Null
}

# Check if JAR file exists
if (-not (Test-Path $APP_JAR)) {
    Write-Host "Error: JAR file not found: $APP_JAR" -ForegroundColor Red
    Write-Host "Please build the project first using: .\build.ps1" -ForegroundColor Yellow
    exit 1
}

# Check if port is already in use
$portInUse = Get-NetTCPConnection -LocalPort $APP_PORT -State Listen -ErrorAction SilentlyContinue
if ($portInUse) {
    Write-Host "Error: Port $APP_PORT is already in use" -ForegroundColor Red
    Write-Host "Please stop the process using this port first" -ForegroundColor Yellow
    Write-Host "Use: Get-Process -Id (Get-NetTCPConnection -LocalPort $APP_PORT).OwningProcess | Stop-Process" -ForegroundColor Yellow
    exit 1
}

Write-Host "JAR file: $APP_JAR" -ForegroundColor White
Write-Host "Port: $APP_PORT" -ForegroundColor White
Write-Host "Profile: $SPRING_PROFILE" -ForegroundColor White
Write-Host "Log file: $LOG_FILE" -ForegroundColor White
Write-Host ""

# Start application in background
$startInfo = New-Object System.Diagnostics.ProcessStartInfo
$startInfo.FileName = "java"
$startInfo.Arguments = "$JAVA_OPTS -jar `"$APP_JAR`" --server.port=$APP_PORT --spring.profiles.active=$SPRING_PROFILE"
$startInfo.UseShellExecute = $false
$startInfo.RedirectStandardOutput = $true
$startInfo.RedirectStandardError = $true
$startInfo.CreateNoWindow = $true
$startInfo.WorkingDirectory = $PWD.Path

$process = New-Object System.Diagnostics.Process
$process.StartInfo = $startInfo

# Setup output redirection
$outputHandler = {
    if ($EventArgs.Data) {
        Add-Content -Path $LOG_FILE -Value $EventArgs.Data
    }
}

Register-ObjectEvent -InputObject $process -EventName OutputDataReceived -Action $outputHandler | Out-Null
Register-ObjectEvent -InputObject $process -EventName ErrorDataReceived -Action $outputHandler | Out-Null

# Start the process
try {
    $process.Start() | Out-Null
    $process.BeginOutputReadLine()
    $process.BeginErrorReadLine()
    
    # Save process ID (no trailing newline)
    $process.Id.ToString() | Out-File -FilePath $PID_FILE -Encoding ASCII -NoNewline
    
    Write-Host "Application started successfully!" -ForegroundColor Green
    Write-Host "Process ID: $($process.Id)" -ForegroundColor Green
    Write-Host ""
    
    # Wait a moment and check if process is still running
    Start-Sleep -Seconds 3
    
    if ($process.HasExited) {
        Write-Host "Warning: Application process has exited!" -ForegroundColor Red
        Write-Host "Exit code: $($process.ExitCode)" -ForegroundColor Red
        Write-Host "Please check log file: $LOG_FILE" -ForegroundColor Yellow
        exit 1
    }
    
    Write-Host "Application is running!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Useful commands:" -ForegroundColor Cyan
    Write-Host "  View logs: Get-Content $LOG_FILE -Wait -Tail 50" -ForegroundColor White
    Write-Host "  Check status: Get-Process -Id $($process.Id)" -ForegroundColor White
    Write-Host "  Stop application: .\stop.ps1" -ForegroundColor White
    Write-Host ""
    
    # Try to get version information after a delay
    Start-Sleep -Seconds 10
    
    try {
        $versionUrl = "http://localhost:$APP_PORT/api/version"
        $version = Invoke-RestMethod -Uri $versionUrl -TimeoutSec 5 -ErrorAction SilentlyContinue
        
        if ($version) {
            Write-Host "Version Information:" -ForegroundColor Cyan
            Write-Host "  Application: $($version.application)" -ForegroundColor White
            Write-Host "  Version: $($version.version)" -ForegroundColor White
            Write-Host "  Build Time: $($version.buildTime)" -ForegroundColor White
            Write-Host ""
        }
    } catch {
        Write-Host "Note: Version API not yet available (application may still be starting)" -ForegroundColor Yellow
        Write-Host ""
    }
    
    Write-Host "Access application at: http://localhost:$APP_PORT" -ForegroundColor Cyan
    Write-Host ""
    
} catch {
    Write-Host "Failed to start application!" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please check:" -ForegroundColor Yellow
    Write-Host "  1. Java is installed and in PATH" -ForegroundColor White
    Write-Host "  2. JAR file exists: $APP_JAR" -ForegroundColor White
    Write-Host "  3. Configuration is correct" -ForegroundColor White
    exit 1
}
