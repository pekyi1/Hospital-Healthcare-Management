# Compile and Run Script for Hospital Management System

# Clean bin directory
Write-Host "Cleaning bin directory..." -ForegroundColor Yellow
Remove-Item -Path "bin\*" -Recurse -Force -ErrorAction SilentlyContinue
New-Item -Path "bin" -ItemType Directory -Force | Out-Null

# Generate sources list
Write-Host "Generating sources list..." -ForegroundColor Yellow
Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName } > sources.txt

# Compile
Write-Host "Compiling Java files..." -ForegroundColor Yellow
javac -d bin --module-path "lib\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml -cp "lib\postgresql-42.7.8.jar;src" "@sources.txt"

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
    
    # Copy resources
    Write-Host "Copying FXML and CSS files..." -ForegroundColor Yellow
    Copy-Item -Path "src\com\hospital\view\*.fxml" -Destination "bin\com\hospital\view\" -Force
    Copy-Item -Path "src\com\hospital\view\*.css" -Destination "bin\com\hospital\view\" -Force
    
    Write-Host "`nBuild complete! You can now run the application with:" -ForegroundColor Green
    Write-Host "java -cp `"bin;lib\postgresql-42.7.8.jar`" --module-path `"lib\javafx-sdk-25.0.1\lib`" --add-modules javafx.controls,javafx.fxml com.hospital.Main" -ForegroundColor Cyan
} else {
    Write-Host "Compilation failed! Check the errors above." -ForegroundColor Red
}
