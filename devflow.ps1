# DevFlow Quick Start Script
# This script is located at the project root for quick access to deploy scripts

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("build", "start", "stop", "restart", "status", "help")]
    [string]$Action = "help",
    
    [Parameter(Mandatory=$false)]
    [string]$Version = ""
)

$SCRIPTS_DIR = Join-Path $PSScriptRoot "deploy\scripts"

function Show-Help {
    Write-Host @"
DevFlow Quick Start Tool
========================

Usage: .\devflow.ps1 <action> [options]

Actions:
  build           Build the project
  start           Start the application
  stop            Stop the application
  restart         Restart the application
  status          Check application status
  help            Show this help message

Options:
  -Version <ver>  Specify version (only for build)

Examples:
  .\devflow.ps1 build                # Build project
  .\devflow.ps1 build -Version 1.0.0 # Build with specific version
  .\devflow.ps1 start                # Start application
  .\devflow.ps1 status               # Check status
  .\devflow.ps1 stop                 # Stop application
  .\devflow.ps1 restart              # Restart application

For more information, see: deploy\README.md
"@ -ForegroundColor Cyan
}

function Invoke-DeployScript {
    param([string]$ScriptName)
    
    $scriptPath = Join-Path $SCRIPTS_DIR "$ScriptName.ps1"
    
    if (-not (Test-Path $scriptPath)) {
        Write-Host "Error: Script not found: $scriptPath" -ForegroundColor Red
        exit 1
    }
    
    & $scriptPath @args
}

# Main logic
switch ($Action.ToLower()) {
    "build" {
        Write-Host "Building project..." -ForegroundColor Cyan
        if ($Version) {
            & (Join-Path $SCRIPTS_DIR "build.ps1") -Version $Version
        } else {
            & (Join-Path $SCRIPTS_DIR "build.ps1")
        }
    }
    "start" {
        Write-Host "Starting application..." -ForegroundColor Cyan
        Invoke-DeployScript "start-prod"
    }
    "stop" {
        Write-Host "Stopping application..." -ForegroundColor Cyan
        Invoke-DeployScript "stop"
    }
    "restart" {
        Write-Host "Restarting application..." -ForegroundColor Cyan
        Invoke-DeployScript "restart"
    }
    "status" {
        Write-Host "Checking status..." -ForegroundColor Cyan
        Invoke-DeployScript "status"
    }
    "help" {
        Show-Help
    }
    default {
        Write-Host "Unknown action: $Action" -ForegroundColor Red
        Write-Host ""
        Show-Help
        exit 1
    }
}
