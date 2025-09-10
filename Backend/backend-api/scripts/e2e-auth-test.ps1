param(
  [string]$BaseUrl = 'http://localhost:3000/maian/auth',
  [string]$Password = 'SecurePass123',
  [string]$DeviceName = 'E2E_TOOL',
  [string]$UserAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) E2E'
)

$ErrorActionPreference = 'Stop'

function Out-Flag($v) { if ($null -ne $v -and $v.ToString().Length -gt 0) { 'true' } else { 'false' } }

function Section($title) { Write-Host ("`n=== $title ===") -ForegroundColor Cyan }

$ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$email = "e2e+$ts@test.local"

# 1) REGISTER
Section 'REGISTER'
$regBody = @{ email = $email; password = $Password } | ConvertTo-Json -Depth 5
$regResp = Invoke-RestMethod -Method Post -Uri ("$BaseUrl/register") -ContentType 'application/json' -Body $regBody -TimeoutSec 30
Write-Host ("  Registered id={0} email={1}" -f $regResp.id, $regResp.email)

# 2) LOGIN (API)
Section 'LOGIN (API)'
$loginBody = @{ email = $email; password = $Password; deviceName = $DeviceName; userAgent = $UserAgent } | ConvertTo-Json -Depth 5
$loginResp = $null
try { $loginResp = Invoke-RestMethod -Method Post -Uri ("$BaseUrl/login") -ContentType 'application/json' -Body $loginBody -TimeoutSec 30 } catch { $loginResp = $_.Exception.Response | ConvertTo-Json -Depth 10 }
$access = if ($loginResp.PSObject.Properties.Name -contains 'accessToken') { $loginResp.accessToken } else { $null }
$refresh = if ($loginResp.PSObject.Properties.Name -contains 'refreshToken') { $loginResp.refreshToken } else { $null }
Write-Host ("  accessToken? {0}  refreshToken? {1}" -f (Out-Flag $access), (Out-Flag $refresh))

# 3) REFRESH (API)
Section 'REFRESH (API)'
$refResp = $null
if ($refresh) {
  $refBody = @{ refreshToken = $refresh } | ConvertTo-Json -Depth 5
  $refResp = Invoke-RestMethod -Method Post -Uri ("$BaseUrl/refresh-token") -ContentType 'application/json' -Body $refBody -TimeoutSec 30
  $access2 = $refResp.accessToken
  $refresh2 = $refResp.refreshToken
  Write-Host ("  rotated access? {0}  refresh? {1}" -f (Out-Flag $access2), (Out-Flag $refresh2))
} else {
  Write-Host '  Skipped: no refresh from login (likely blocked by status)' -ForegroundColor Yellow
}

# 4) LOGIN-WEB (Cookie+CSRF)
Section 'LOGIN-WEB (Cookie+CSRF)'
$loginWebResp = Invoke-RestMethod -Method Post -Uri ("$BaseUrl/login-web") -ContentType 'application/json' -Body $loginBody -TimeoutSec 30 -SessionVariable sess
$csrf = $loginWebResp.refreshToken
$accessWeb = $loginWebResp.accessToken
$cookieUrl = "$BaseUrl/refresh-token-web"
$cRefresh = $sess.Cookies.GetCookies($cookieUrl) | Where-Object { $_.Name -eq 'refresh_token' }
Write-Host ("  access? {0}  csrf? {1}  cookie(refresh_token)? {2}" -f (Out-Flag $accessWeb), (Out-Flag $csrf), (Out-Flag $cRefresh))

# 5) REFRESH-WEB (Cookie+CSRF rotation)
Section 'REFRESH-WEB (Cookie+CSRF)'
if ($csrf -and $cRefresh) {
  $refWebBody = @{ refreshToken = $csrf } | ConvertTo-Json -Depth 5
  $refWebResp = Invoke-RestMethod -Method Post -Uri ("$BaseUrl/refresh-token-web") -ContentType 'application/json' -Body $refWebBody -TimeoutSec 30 -WebSession $sess
  $access3 = $refWebResp.accessToken
  $csrf2 = $refWebResp.refreshToken
  $cRefresh2 = $sess.Cookies.GetCookies($cookieUrl) | Where-Object { $_.Name -eq 'refresh_token' }
  Write-Host ("  access? {0}  csrf(new)? {1}  cookie present? {2}" -f (Out-Flag $access3), (Out-Flag $csrf2), (Out-Flag $cRefresh2))
} else {
  Write-Host '  Skipped: missing CSRF or cookie from login-web' -ForegroundColor Yellow
}

# 6) LOGOUT (Bearer)
Section 'LOGOUT (Bearer)'
if ($access) {
  $headers = @{ Authorization = "Bearer $access" }
  $logoutResp = Invoke-RestMethod -Method Delete -Uri ("$BaseUrl/logout") -Headers $headers -TimeoutSec 30 -ErrorAction SilentlyContinue
  $msg = if ($logoutResp) { $logoutResp.message } else { 'no response' }
  Write-Host ("  logout message: {0}" -f $msg)
} else {
  Write-Host '  Skipped: no access token from API login' -ForegroundColor Yellow
}

Write-Host "`nSUMMARY" -ForegroundColor Green
Write-Host ("  Email: {0}" -f $email)
Write-Host ("  API Login access? {0}, refresh? {1}" -f (Out-Flag $access), (Out-Flag $refresh))
Write-Host ("  Web Login access? {0}, CSRF? {1}, Cookie? {2}" -f (Out-Flag $accessWeb), (Out-Flag $csrf), (Out-Flag $cRefresh))
