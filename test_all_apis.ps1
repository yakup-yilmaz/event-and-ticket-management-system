$ErrorActionPreference = "Stop"

function Test-Endpoint {
    param($Name, $Method, $Uri, $Body, $Token)
    Write-Host ">>> TEST: $Name ($Method $Uri)"
    $headers = @{"Content-Type" = "application/json"}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $params = @{ Uri = "http://localhost:8080$Uri"; Method = $Method; Headers = $headers }
    if ($Body) { $params.Body = ($Body | ConvertTo-Json -Depth 10) }
    
    try {
        $response = Invoke-RestMethod @params
        Write-Host "BASARILI`n" -ForegroundColor Green
        return $response
    } catch {
        Write-Host "HATA!" -ForegroundColor Red
        Write-Host $_.Exception.Message
        return $null
    }
}

# --- 1. AUTH & USER ENDPOINTS ---
$adminLogin = Test-Endpoint -Name "Admin Login" -Method POST -Uri "/api/v1/auth/login" -Body @{ email="admin@ticketsystem.local"; password="ChangeMe123!" }
$adminToken = $adminLogin.accessToken

$uid = (Get-Date).Ticks
$email = "test_$uid@example.com"
$null = Test-Endpoint -Name "Register User" -Method POST -Uri "/api/v1/auth/register" -Body @{ firstName="Test"; lastName="User"; email=$email; password="Password123!" }
$userLogin = Test-Endpoint -Name "Login User" -Method POST -Uri "/api/v1/auth/login" -Body @{ email=$email; password="Password123!" }
$userToken = $userLogin.accessToken
$refreshToken = $userLogin.refreshToken

$refreshed = Test-Endpoint -Name "Refresh Token" -Method POST -Uri "/api/v1/auth/refresh" -Body @{ refreshToken=$refreshToken }
if ($refreshed.accessToken) { $userToken = $refreshed.accessToken }

$currentUser = Test-Endpoint -Name "Get /me" -Method GET -Uri "/api/v1/users/me" -Token $userToken
$null = Test-Endpoint -Name "Put /me" -Method PUT -Uri "/api/v1/users/me" -Body @{ firstName="Updated"; lastName="Name" } -Token $userToken
$null = Test-Endpoint -Name "Get All Users (Admin)" -Method GET -Uri "/api/v1/users" -Token $adminToken
$null = Test-Endpoint -Name "Get User By Id (Admin)" -Method GET -Uri "/api/v1/users/$($currentUser.id)" -Token $adminToken


# --- 2. EVENT ENDPOINTS ---
$eventBody = @{ name="Full Test Event $uid"; description="Desc"; totalSeats=50; basePrice=100.00; eventDate=(Get-Date).AddDays(5).ToString("yyyy-MM-ddTHH:mm:ss"); pricingType="STANDARD" }
$event = Test-Endpoint -Name "POST Event (Admin)" -Method POST -Uri "/api/v1/events" -Body $eventBody -Token $adminToken
$eventId = $event.id

$null = Test-Endpoint -Name "GET All Events" -Method GET -Uri "/api/v1/events"
$null = Test-Endpoint -Name "GET Active Events" -Method GET -Uri "/api/v1/events/active"
$null = Test-Endpoint -Name "GET Event By Status" -Method GET -Uri "/api/v1/events/status/ACTIVE"
$null = Test-Endpoint -Name "GET Event Search" -Method GET -Uri "/api/v1/events/search?keyword=Full"
$null = Test-Endpoint -Name "GET Event By Id" -Method GET -Uri "/api/v1/events/$eventId"

$updateBody = @{ name="Updated Event $uid"; description="Desc 2"; totalSeats=100; basePrice=150.00; eventDate=(Get-Date).AddDays(10).ToString("yyyy-MM-ddTHH:mm:ss"); status="ACTIVE" }
$null = Test-Endpoint -Name "PUT Event (Admin)" -Method PUT -Uri "/api/v1/events/$eventId" -Body $updateBody -Token $adminToken
$null = Test-Endpoint -Name "PATCH Event (Admin)" -Method PATCH -Uri "/api/v1/events/$eventId" -Body @{ basePrice=200.00 } -Token $adminToken


# --- 3. FAVORITE ENDPOINTS ---
$null = Test-Endpoint -Name "POST Favorite" -Method POST -Uri "/api/v1/favorites/$eventId" -Token $userToken
$null = Test-Endpoint -Name "GET My Favorites" -Method GET -Uri "/api/v1/favorites" -Token $userToken
$null = Test-Endpoint -Name "GET Favorite Check" -Method GET -Uri "/api/v1/favorites/$eventId/check" -Token $userToken


# --- 4. TICKET ENDPOINTS ---
$ticket = Test-Endpoint -Name "POST Buy Ticket" -Method POST -Uri "/api/v1/tickets/buy" -Body @{ eventId=$eventId } -Token $userToken
$ticketId = $ticket.id
$null = Test-Endpoint -Name "GET My Tickets" -Method GET -Uri "/api/v1/tickets/my-tickets" -Token $userToken
$null = Test-Endpoint -Name "GET Tickets By UserId (Admin)" -Method GET -Uri "/api/v1/tickets/user/$($currentUser.id)" -Token $adminToken
$null = Test-Endpoint -Name "POST Cancel Ticket" -Method POST -Uri "/api/v1/tickets/$ticketId/cancel" -Token $userToken


# --- 5. NOTIFICATION ENDPOINTS ---
$notifs = Test-Endpoint -Name "GET All Notifications" -Method GET -Uri "/api/v1/notifications" -Token $userToken
$null = Test-Endpoint -Name "GET Unread Notifications" -Method GET -Uri "/api/v1/notifications/unread" -Token $userToken

if ($notifs -and $notifs.Count -gt 0) {
    $notifId = $notifs[0].id
    if ($notifId) {
        $null = Test-Endpoint -Name "POST Read Notification" -Method POST -Uri "/api/v1/notifications/$notifId/read" -Token $userToken
    }
}
$null = Test-Endpoint -Name "POST Read All Notifications" -Method POST -Uri "/api/v1/notifications/read-all" -Token $userToken


# --- 6. CLEANUP ENDPOINTS ---
$null = Test-Endpoint -Name "DELETE Favorite" -Method DELETE -Uri "/api/v1/favorites/$eventId" -Token $userToken
$null = Test-Endpoint -Name "DELETE Event (Admin)" -Method DELETE -Uri "/api/v1/events/$eventId" -Token $adminToken
$null = Test-Endpoint -Name "POST Logout" -Method POST -Uri "/api/v1/auth/logout" -Body @{ refreshToken=$refreshToken } -Token $userToken

Write-Host ">>> TUM 26 ENDPOINT TESTI TAMAMLANDI."
