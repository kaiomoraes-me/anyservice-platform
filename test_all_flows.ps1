$baseUrl = 'http://localhost:8080/api/auth'

Write-Host "--- 1. WIPING DATABASE ---" -ForegroundColor Cyan
docker exec db_user psql -U postgres -d anyservice_user -t -c "TRUNCATE TABLE users CASCADE;"

Write-Host "
--- 2. REGISTER CLIENT ---" -ForegroundColor Cyan
$regBody = @{ name="Test Client"; username="test_client"; email="client@test.com"; password="password123"; role="CLIENT" } | ConvertTo-Json
try {
    $r = Invoke-RestMethod -Uri "$baseUrl/register" -Method Post -ContentType "application/json" -Body $regBody
    Write-Host "SUCCESS: $($r.message)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: $($_.Exception.Response.StatusCode) - $((New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd())" -ForegroundColor Red
    exit 1
}

Write-Host "
--- 3. FETCH VERIFICATION CODE ---" -ForegroundColor Cyan
$codeRaw = (docker exec db_user psql -U postgres -d anyservice_user -t -c "SELECT verification_code FROM users WHERE email='client@test.com';")
$code = $codeRaw -replace '\s',''
Write-Host "Verification Code: $code" -ForegroundColor Green

Write-Host "
--- 4. VERIFY ACCOUNT ---" -ForegroundColor Cyan
$verifyBody = @{ email="client@test.com"; code=$code } | ConvertTo-Json
try {
    $r = Invoke-RestMethod -Uri "$baseUrl/verify" -Method Post -ContentType "application/json" -Body $verifyBody
    Write-Host "SUCCESS: $($r.message)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: $($_.Exception.Response.StatusCode) - $((New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd())" -ForegroundColor Red
    exit 1
}

Write-Host "
--- 5. AUTHENTICATE ---" -ForegroundColor Cyan
$loginBody = @{ email="client@test.com"; password="password123" } | ConvertTo-Json
try {
    $r = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method Post -ContentType "application/json" -Body $loginBody
    Write-Host "SUCCESS: Token length $($r.token.Length)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: $($_.Exception.Response.StatusCode) - $((New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd())" -ForegroundColor Red
    exit 1
}

Write-Host "
--- 6. FORGOT PASSWORD ---" -ForegroundColor Cyan
$forgotBody = @{ email="client@test.com" } | ConvertTo-Json
try {
    $r = Invoke-RestMethod -Uri "$baseUrl/forgot-password" -Method Post -ContentType "application/json" -Body $forgotBody
    Write-Host "SUCCESS: $($r.message)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: $($_.Exception.Response.StatusCode) - $((New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd())" -ForegroundColor Red
    exit 1
}

Write-Host "
--- 7. FETCH RESET CODE ---" -ForegroundColor Cyan
$resetCodeRaw = (docker exec db_user psql -U postgres -d anyservice_user -t -c "SELECT reset_password_code FROM users WHERE email='client@test.com';")
$resetCode = $resetCodeRaw -replace '\s',''
Write-Host "Reset Code: $resetCode" -ForegroundColor Green

Write-Host "
--- 8. RESET PASSWORD ---" -ForegroundColor Cyan
$resetBody = @{ email="client@test.com"; code=$resetCode; newPassword="newPassword456" } | ConvertTo-Json
try {
    $r = Invoke-RestMethod -Uri "$baseUrl/reset-password" -Method Post -ContentType "application/json" -Body $resetBody
    Write-Host "SUCCESS: $($r.message)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: $($_.Exception.Response.StatusCode) - $((New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd())" -ForegroundColor Red
    exit 1
}

Write-Host "
--- 9. AUTHENTICATE WITH NEW PASSWORD ---" -ForegroundColor Cyan
$loginBodyNew = @{ email="client@test.com"; password="newPassword456" } | ConvertTo-Json
try {
    $r = Invoke-RestMethod -Uri "$baseUrl/authenticate" -Method Post -ContentType "application/json" -Body $loginBodyNew
    Write-Host "SUCCESS: Token length $($r.token.Length)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: $($_.Exception.Response.StatusCode) - $((New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd())" -ForegroundColor Red
    exit 1
}

