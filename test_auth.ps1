$baseUrl = 'http://localhost:8080/api/auth'

Write-Host "1. Testing Registration (CLIENT)..."
$regBody = @{
    name = "Test Client"
    username = "test_client"
    email = "client@test.com"
    password = "password123"
    role = "CLIENT"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/register" -Method Post -ContentType "application/json" -Body $regBody
    Write-Host "SUCCESS: $($response | ConvertTo-Json)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    $stream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    Write-Host $reader.ReadToEnd() -ForegroundColor Yellow
}
