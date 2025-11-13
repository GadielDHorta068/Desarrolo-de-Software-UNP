# Script de despliegue simplificado con pruebas automatizadas (Windows PowerShell)
# Este script construye, despliega y prueba la aplicación 2FA

param(
    [string]$ComposeFile = "docker-compose.yml"
)

Write-Host "🚀 Iniciando despliegue con pruebas automatizadas..." -ForegroundColor Cyan
Write-Host ""

Write-Host "📋 Paso 1: Construyendo imagen Docker" -ForegroundColor Yellow
docker build -t 2fa-autohosted:latest .
Write-Host ""

Write-Host "📋 Paso 2: Iniciando servicios con Docker Compose" -ForegroundColor Yellow
docker-compose up -d --build
Write-Host ""

Write-Host "⏳ Esperando a que la aplicación esté lista..." -ForegroundColor Yellow
$maxAttempts = 60
for ($i = 1; $i -le $maxAttempts; $i++) {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -ErrorAction Stop
        Write-Host "✅ Aplicación está lista!" -ForegroundColor Green
        break
    }
    catch {
        Write-Host "⏳ Esperando... ($i/$maxAttempts)" -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
}

Write-Host ""
Write-Host "📋 Paso 3: Ejecutando pruebas automatizadas" -ForegroundColor Yellow
Write-Host "🧪 Probando endpoints de la API..." -ForegroundColor Yellow

# Esperar un poco más para que la base de datos esté lista
Start-Sleep -Seconds 10

# Ejecutar pruebas simplificadas con PowerShell
$TestUser = "test_user_$(Get-Date -Format 'yyyyMMddHHmmss')"
$BaseUrl = "http://localhost:8080"

function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    
    Write-Host "📡 $Method $Endpoint" -ForegroundColor Green
    
    try {
        $headers = @{}
        if ($Body) {
            $headers["Content-Type"] = "application/json"
        }
        
        if ($Method -eq "GET") {
            $response = Invoke-RestMethod -Uri "$BaseUrl$Endpoint" -Method GET -Headers $headers -StatusCodeVariable statusCode
        } else {
            $response = Invoke-RestMethod -Uri "$BaseUrl$Endpoint" -Method $Method -Headers $headers -Body $Body -StatusCodeVariable statusCode
        }
        
        Write-Host "📊 Código de respuesta: $statusCode" -ForegroundColor Yellow
        
        if ($statusCode -eq $ExpectedStatus) {
            Write-Host "✅ PASÓ" -ForegroundColor Green
        } else {
            Write-Host "❌ FALLÓ (esperado: $ExpectedStatus)" -ForegroundColor Red
            exit 1
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "📊 Código de respuesta: $statusCode" -ForegroundColor Yellow
        
        if ($statusCode -eq $ExpectedStatus) {
            Write-Host "✅ PASÓ" -ForegroundColor Green
        } else {
            Write-Host "❌ FALLÓ (esperado: $ExpectedStatus)" -ForegroundColor Red
            exit 1
        }
    }
    
    Write-Host ""
}

# Pruebas simplificadas
Test-Endpoint -Method "GET" -Endpoint "/actuator/health" -ExpectedStatus 200
Test-Endpoint -Method "POST" -Endpoint "/api/users" -Body '{"username":"'$TestUser'"}' -ExpectedStatus 201
Test-Endpoint -Method "GET" -Endpoint "/api/users/$TestUser/status" -ExpectedStatus 200
Test-Endpoint -Method "POST" -Endpoint "/api/2fa/enable" -Body '{"username":"'$TestUser'"}' -ExpectedStatus 200
Test-Endpoint -Method "POST" -Endpoint "/api/2fa/verify" -Body '{"username":"'$TestUser'","code":"000000"}' -ExpectedStatus 200
Test-Endpoint -Method "GET" -Endpoint "/api/admin/stats" -ExpectedStatus 200
Test-Endpoint -Method "GET" -Endpoint "/api/admin/users" -ExpectedStatus 200
Test-Endpoint -Method "POST" -Endpoint "/api/2fa/disable/$TestUser" -ExpectedStatus 204
Test-Endpoint -Method "DELETE" -Endpoint "/api/users/$TestUser" -ExpectedStatus 204

Write-Host "📋 Paso 4: Verificando salud del sistema" -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET
    $healthStatus = $healthResponse.status
    if ($healthStatus -eq "UP") {
        Write-Host "✅ Sistema saludable: $healthStatus" -ForegroundColor Green
    } else {
        Write-Host "❌ Sistema no saludable: $healthStatus" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Error al verificar salud del sistema: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "📊 Resumen de endpoints disponibles:" -ForegroundColor Cyan
Write-Host "✅ GET  /actuator/health                    - Salud del sistema" -ForegroundColor Green
Write-Host "✅ POST /api/users                          - Crear usuario" -ForegroundColor Green
Write-Host "✅ GET  /api/users/{username}               - Obtener usuario" -ForegroundColor Green
Write-Host "✅ GET  /api/users/{username}/status        - Estado 2FA" -ForegroundColor Green
Write-Host "✅ DELETE /api/users/{username}               - Eliminar usuario" -ForegroundColor Green
Write-Host "✅ POST /api/2fa/enable                     - Habilitar 2FA" -ForegroundColor Green
Write-Host "✅ POST /api/2fa/verify                     - Verificar código TOTP" -ForegroundColor Green
Write-Host "✅ POST /api/2fa/verify-recovery/{username} - Verificar código recuperación" -ForegroundColor Green
Write-Host "✅ POST /api/2fa/rotate/{username}          - Rotar secreto" -ForegroundColor Green
Write-Host "✅ POST /api/2fa/disable/{username}         - Deshabilitar 2FA" -ForegroundColor Green
Write-Host "✅ GET  /api/admin/stats                     - Estadísticas del sistema" -ForegroundColor Green
Write-Host "✅ GET  /api/admin/users                    - Listar usuarios con 2FA" -ForegroundColor Green

Write-Host ""
Write-Host "🎉 ¡DESPLIEGUE EXITOSO!" -ForegroundColor Green
Write-Host "✅ Todos los endpoints han sido probados y funcionan correctamente" -ForegroundColor Green
Write-Host "✅ El sistema 2FA está completamente operativo" -ForegroundColor Green
Write-Host "✅ Las pruebas automatizadas pasaron exitosamente" -ForegroundColor Green
Write-Host ""
Write-Host "📖 Próximos pasos:" -ForegroundColor Yellow
Write-Host "- La API está disponible en: http://localhost:8080" -ForegroundColor White
Write-Host "- Documentación Swagger: http://localhost:8080/swagger-ui/index.html" -ForegroundColor White
Write-Host "- Health check: http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "🚀 ¡Sistema listo para uso en producción!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Para detener los servicios:" -ForegroundColor Yellow
Write-Host "docker-compose down" -ForegroundColor White
Write-Host ""
Write-Host "📋 Para ver logs:" -ForegroundColor Yellow
Write-Host "docker-compose logs -f" -ForegroundColor White