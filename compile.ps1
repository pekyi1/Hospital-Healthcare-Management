# Compile and Run Script for Hospital Management System

# Clean bin directory
Write-Host "Cleaning bin directory..." -ForegroundColor Yellow
Remove-Item -Path "bin\*" -Recurse -Force -ErrorAction SilentlyContinue
New-Item -Path "bin" -ItemType Directory -Force | Out-Null

# Generate sources list
Write-Host "Generating sources list..." -ForegroundColor Yellow
Get-ChildItem -Path src -Filter *.java -Recurse | Resolve-Path -Relative | Out-File -Encoding ASCII sources.txt

# Compile
Write-Host "Compiling Java files..." -ForegroundColor Yellow
javac -d bin --module-path "C:\Users\FredPekyi\Downloads\javafx-sdk-25.0.1\lib" --add-modules "javafx.controls,javafx.fxml" -cp "lib\postgresql-42.7.8.jar;lib\mongodb-driver-sync-4.11.1.jar;lib\mongodb-driver-core-4.11.1.jar;lib\bson-4.11.1.jar;src" "@sources.txt"

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
    
    # Copy resources
    Write-Host "Copying FXML and CSS files..." -ForegroundColor Yellow
    New-Item -Path "bin\com\hospital\view" -ItemType Directory -Force | Out-Null
    Copy-Item -Path "src\com\hospital\view\*.fxml" -Destination "bin\com\hospital\view\" -Force
    Copy-Item -Path "src\com\hospital\view\*.css" -Destination "bin\com\hospital\view\" -Force
    
    Write-Host "`nBuild complete! You can now run the application with:" -ForegroundColor Green
    Write-Host "`nLaunching application..." -ForegroundColor Cyan
    java -cp "bin;lib\postgresql-42.7.8.jar;lib\mongodb-driver-sync-4.11.1.jar;lib\mongodb-driver-core-4.11.1.jar;lib\bson-4.11.1.jar" --module-path "C:\Users\FredPekyi\Downloads\javafx-sdk-25.0.1\lib" --add-modules "javafx.controls,javafx.fxml" com.hospital.Main
}
else {
    Write-Host "Compilation failed! Check the errors above." -ForegroundColor Red
}
