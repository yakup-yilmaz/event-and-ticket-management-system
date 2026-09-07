$ErrorActionPreference = "Stop"

function Test-Endpoint {
    param($Name, $Method, $Uri, $Body, $Token)
    
    Write-Host "`n>>> TEST: $Name"
    
    $headers = @{"Content-Type" = "application/json"}
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    
    $params = @{
        Uri = "http://localhost:8080$Uri"
        Method = $Method
        Headers = $headers
    }
    
    if ($Body) { $params.Body = ($Body | ConvertTo-Json -Depth 10) }
    
    try {
        $response = Invoke-RestMethod @params
        Write-Host "BASARILI" -ForegroundColor Green
        return $response
    } catch {
        Write-Host "HATA!" -ForegroundColor Red
        Write-Host $_.Exception.Message
        if ($_.ErrorDetails -ne $null) {
            Write-Host $_.ErrorDetails.Message
        }
        return $null
    }
}

# 1. Admin Login
$adminLogin = Test-Endpoint -Name "Admin Login" -Method POST -Uri "/api/v1/auth/login" -Body @{ email="admin@ticketsystem.local"; password="ChangeMe123!" }
$adminToken = $adminLogin.accessToken

# 2. User Register
$uniqueId = (Get-Date).Ticks
$userEmail = "testuser_$uniqueId@example.com"
$null = Test-Endpoint -Name "User Kaydi" -Method POST -Uri "/api/v1/auth/register" -Body @{ firstName="Test"; lastName="User"; email=$userEmail; password="Password123!" }

# 3. User Login
$userLogin = Test-Endpoint -Name "User Login" -Method POST -Uri "/api/v1/auth/login" -Body @{ email=$userEmail; password="Password123!" }
$userToken = $userLogin.accessToken

# 4. Create Event (Admin)
$eventBody = @{
    name = "Test Konseri $uniqueId"
    description = "Test aciklamasi"
    totalSeats = 100
    basePrice = 250.00
    eventDate = (Get-Date).AddDays(10).ToString("yyyy-MM-ddTHH:mm:ss")
    pricingType = "OCCUPANCY_BASED"
}
$event = Test-Endpoint -Name "Etkinlik Olustur (Admin)" -Method POST -Uri "/api/v1/events" -Body $eventBody -Token $adminToken
$eventId = $event.id

if ($eventId) {
    # 5. Favoriye Ekle (User)
    $null = Test-Endpoint -Name "Favori Ekle" -Method POST -Uri "/api/v1/favorites/$eventId" -Token $userToken

    # 6. Bilet Al (User)
    $ticket = Test-Endpoint -Name "Bilet Al" -Method POST -Uri "/api/v1/tickets/buy" -Body @{ eventId=$eventId } -Token $userToken
    $ticketId = $ticket.id

    if ($ticketId) {
        # 7. Bilet Iptal (User)
        $null = Test-Endpoint -Name "Bilet Iptal" -Method POST -Uri "/api/v1/tickets/$ticketId/cancel" -Token $userToken
    }

    # 8. Bildirimleri Oku
    $null = Test-Endpoint -Name "Bildirimleri Listele" -Method GET -Uri "/api/v1/notifications" -Token $userToken
}

# 9. Logout
$null = Test-Endpoint -Name "Cikis Yap" -Method POST -Uri "/api/v1/auth/logout" -Body @{ refreshToken=$userLogin.refreshToken } -Token $userToken

Write-Host "`n>>> TUM TESTLER TAMAMLANDI."
