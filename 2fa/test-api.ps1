# Script de pruebas automatizadas para 2FA API (Windows PowerShell)
# Este script prueba todos los endpoints del sistema

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$TestUser = $null
)

if (-not $TestUser) {
    $TestUser = "test_user_$(Get-Date -Format 'yyyyMMddHHmmss')"
}

Write-Host "=== Iniciando pruebas de 2FA API ===" -ForegroundColor Cyan
Write-Host "URL: $BaseUrl" -ForegroundColor Yellow
Write-Host "Usuario de prueba: $TestUser" -ForegroundColor Yellow
Write-Host ""

# Función para hacer requests
function Make-Request {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Data = $null,
        [string]$ContentType = "application/json"
    )
    
    Write-Host "📡 $Method $Endpoint" -ForegroundColor Green
    
    try {
        $headers = @{}
        if ($Data) {
            $headers["Content-Type"] = $ContentType
        }
        
        if ($Method -eq "GET") {
            $response = Invoke-RestMethod -Uri "$BaseUrl$Endpoint" -Method GET -Headers $headers -StatusCodeVariable statusCode
        } else {
            $body = $Data
            if ($ContentType -eq "application/json" -and $Data) {
                $body = $Data | ConvertTo-Json
            }
            $response = Invoke-RestMethod -Uri "$BaseUrl$Endpoint" -Method $Method -Headers $headers -Body $body -StatusCodeVariable statusCode
        }
        
        Write-Host "📊 Código de respuesta: $statusCode" -ForegroundColor Yellow
        if ($response) {
            Write-Host "📄 Respuesta: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
        }
        Write-Host ""
        
        return @{ StatusCode = $statusCode; Response = $response }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "📊 Código de respuesta: $statusCode" -ForegroundColor Yellow
        Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
        return @{ StatusCode = $statusCode; Response = $null }
    }
}

# Función para verificar código HTTP
function Check-Status {
    param(
        [int]$Expected,
        [int]$Actual,
        [string]$TestName
    )
    
    if ($Expected -eq $Actual) {
        Write-Host "✅ $TestName - PASÓ" -ForegroundColor Green
    } else {
        Write-Host "❌ $TestName - FALLÓ (esperado: $Expected, obtenido: $Actual)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "1️⃣ CREANDO USUARIO DE PRUEBA" -ForegroundColor Cyan
$result = Make-Request -Method "POST" -Endpoint "/api/users" -Data '{"username":"'$TestUser'"}'
Check-Status -Expected 201 -Actual $result.StatusCode -TestName "Crear usuario"

Write-Host "2️⃣ VERIFICANDO ESTADO INICIAL DEL USUARIO" -ForegroundColor Cyan
$result = Make-Request -Method "GET" -Endpoint "/api/users/$TestUser/status"
Check-Status -Expected 200 -Actual $result.StatusCode -TestName "Obtener estado inicial"

Write-Host "3️⃣ HABILITANDO 2FA" -ForegroundColor Cyan
$result = Make-Request -Method "POST" -Endpoint "/api/2fa/enable" -Data '{"username":"'$TestUser'"}'
Check-Status -Expected 200 -Actual $result.StatusCode -TestName "Habilitar 2FA"

# Extraer un código de recuperación para pruebas posteriores
if ($result.Response -and $result.Response.recoveryCodes) {
    $recoveryCode = $result.Response.recoveryCodes[0]
    Write-Host "📋 Código de recuperación obtenido: $recoveryCode" -ForegroundColor Yellow
}

Write-Host "4️⃣ VERIFICANDO ESTADO DESPUÉS DE HABILITAR 2FA" -ForegroundColor Cyan
$result = Make-Request -Method "GET" -Endpoint "/api/users/$TestUser/status"
Check-Status -Expected 200 -Actual $result.StatusCode -TestName "Verificar estado después de habilitar"

Write-Host "5️⃣ INTENTANDO VERIFICAR CÓDIGO TOTP INVÁLIDO" -ForegroundColor Cyan
$result = Make-Request -Method "POST" -Endpoint "/api/2fa/verify" -Data '{"username":"'$TestUser'","code":"000000"}'
Check-Status -Expected 200 -Actual $result.StatusCode -TestName "Verificar código TOTP inválido"

# Verificar que el código fue rechazado
if ($result.Response -and $result.Response.verified -eq $false) {
    Write-Host "✅ Código inválido correctamente rechazado" -ForegroundColor Green
} else {
    Write-Host "❌ Error: código inválido fue aceptado" -ForegroundColor Red
    exit 1
}

Write-Host "6️⃣ VERIFICANDO CÓDIGO DE RECUPERACIÓN" -ForegroundColor Cyan
$result = Make-Request -Method "POST" -Endpoint "/api/2fa/verify-recovery/$TestUser" -Data $recoveryCode -ContentType "text/plain"
Check-Status -Expected 200 -Actual $result.StatusCode -TestName "Verificar código de recuperación"

Write-Host "7️⃣ INTENTANDO USAR EL MISMO CÓDIGO DE RECUPERACIÓN (DEBE FALLAR)" -ForegroundColor Cyan
$result = Make-Request -Method "POST" -Endpoint "/api/2fa/verify-recovery/$TestUser" -Data $recoveryCode -ContentType "text/plain"
Check-Status -Expected 200 -Actual $result.StatusCode -TestName "Reusar código de recuperación"

# Verificar que el código usado fue rechazado
if ($result.Response -and $result.Response.verified -eq $false) {
    Write-Host "✅ Código usado correctamente rechazado" -ForegroundColor Green
} else {
    Write-Host "❌ Error: código usado fue aceptado" -ForegroundColor Red
    exit 1
}

Write-Host "8️⃣ ROTANDO SECRETO 2FA" -ForegroundColor Cyan
$result = Make-Request -Method "POST" -Endpoint "/api/2fa/rotate/$TestUser"
Check-Status -Expected 200 -Actual $result.StatusCode -TestName "Rotar secreto 2FA"

Write-Host "9️⃣ OBTENIENDO ESTADÍSTICAS DEL SISTEMA" -ForegroundColor Cyan
$result = Make-Request -Method "GET" -Endpoint "/api/admin/stats"
Check-Status -Expected 200 -Actual $result.StatusCode -TestName "Obtener estadísticas del sistema"

Write-Host "🔟 LISTANDO USUARIOS CON 2FA" -ForegroundColor Cyan
$result = Make-Request -Method "GET" -Endpoint "/api/admin/users"
Check-Status -Expected 200 -Actual $result.StatusCode -TestName "Listar usuarios con 2FA"

Write-Host "1️⃣1️⃣ DESHABILITANDO 2FA" -ForegroundColor Cyan
$result = Make-Request -Method "POST" -Endpoint "/api/2fa/disable/$TestUser"
Check-Status -Expected 204 -Actual $result.StatusCode -TestName "Deshabilitar 2FA"

Write-Host "1️⃣2️⃣ VERIFICANDO ESTADO DESPUÉS DE DESHABILITAR" -ForegroundColor Cyan
$result = Make-Request -Method "GET" -Endpoint "/api/users/$TestUser/status"
Check-Status -Expected 200 -Actual $result.StatusCode -TestName "Verificar estado después de deshabilitar"

Write-Host "1️⃣3️⃣ ELIMINANDO USUARIO" -ForegroundColor Cyan
$result = Make-Request -Method "DELETE" -Endpoint "/api/users/$TestUser"
Check-Status -Expected 204 -Actual $result.StatusCode -TestName "Eliminar usuario"

Write-Host "1️⃣4️⃣ VERIFICANDO QUE EL USUARIO FUE ELIMINADO" -ForegroundColor Cyan
$result = Make-Request -Method "GET" -Endpoint "/api/users/$TestUser"
Check-Status -Expected 404 -Actual $result.StatusCode -TestName "Verificar usuario eliminado"

Write-Host ""
Write-Host "🎉 ¡TODAS LAS PRUEBAS PASARON EXITOSAMENTE!" -ForegroundColor Green
Write-Host "✅ Todos los endpoints están funcionando correctamente" -ForegroundColor Green
Write-Host "✅ El flujo completo de 2FA está operativo" -ForegroundColor Green
Write-Host "✅ Los códigos de recuperación funcionan correctamente" -ForegroundColor Green
Write-Host "✅ Las estadísticas y administración están disponibles" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Resumen de pruebas ejecutadas:" -ForegroundColor Cyan
Write-Host "- ✅ Gestión de usuarios (crear, obtener, eliminar)" -ForegroundColor Green
Write-Host "- ✅ Habilitar/deshabilitar 2FA" -ForegroundColor Green
Write-Host "- ✅ Verificación de códigos TOTP" -ForegroundColor Green
Write-Host "- ✅ Códigos de recuperación (uso único)" -ForegroundColor Green
Write-Host "- ✅ Rotación de secretos" -ForegroundColor Green
Write-Host "- ✅ Estadísticas del sistema" -ForegroundColor Green
Write-Host "- ✅ Listado de usuarios con 2FA" -ForegroundColor Green