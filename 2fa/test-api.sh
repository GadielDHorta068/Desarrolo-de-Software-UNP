#!/bin/bash

# Script de pruebas automatizadas para 2FA API
# Este script prueba todos los endpoints del sistema

set -e  # Salir si hay algún error

# Configuración
BASE_URL="${API_URL:-http://localhost:8080}"
TEST_USER="test_user_$(date +%s)"
echo "=== Iniciando pruebas de 2FA API ==="
echo "URL: $BASE_URL"
echo "Usuario de prueba: $TEST_USER"
echo ""

# Función para hacer requests
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local content_type=$4
    
    echo "📡 $method $endpoint"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL$endpoint")
    else
        if [ -z "$content_type" ]; then
            content_type="application/json"
        fi
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$endpoint" \
            -H "Content-Type: $content_type" \
            -d "$data")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    echo "📊 Código de respuesta: $http_code"
    if [ -n "$body" ]; then
        echo "📄 Respuesta: $body"
    fi
    echo ""
    
    echo "$http_code"
}

# Función para verificar código HTTP
check_status() {
    local expected=$1
    local actual=$2
    local test_name=$3
    
    if [ "$expected" = "$actual" ]; then
        echo "✅ $test_name - PASÓ"
    else
        echo "❌ $test_name - FALLÓ (esperado: $expected, obtenido: $actual)"
        exit 1
    fi
}

echo "1️⃣ CREANDO USUARIO DE PRUEBA"
status=$(make_request "POST" "/api/users" '{"username":"'$TEST_USER'"}')
check_status "201" "$status" "Crear usuario"

echo "2️⃣ VERIFICANDO ESTADO INICIAL DEL USUARIO"
status=$(make_request "GET" "/api/users/$TEST_USER/status")
check_status "200" "$status" "Obtener estado inicial"

echo "3️⃣ HABILITANDO 2FA"
status=$(make_request "POST" "/api/2fa/enable" '{"username":"'$TEST_USER'"}')
check_status "200" "$status" "Habilitar 2FA"

# Extraer un código de recuperación para pruebas posteriores
recovery_code=$(echo "$body" | grep -o '"[^"]*-[^"]*-[^"]*-[^"]*"' | head -1 | tr -d '"')
echo "📋 Código de recuperación obtenido: $recovery_code"

echo "4️⃣ VERIFICANDO ESTADO DESPUÉS DE HABILITAR 2FA"
status=$(make_request "GET" "/api/users/$TEST_USER/status")
check_status "200" "$status" "Verificar estado después de habilitar"

echo "5️⃣ INTENTANDO VERIFICAR CÓDIGO TOTP INVÁLIDO"
status=$(make_request "POST" "/api/2fa/verify" '{"username":"'$TEST_USER'","code":"000000"}')
check_status "200" "$status" "Verificar código TOTP inválido"

# Verificar que el código fue rechazado
if echo "$body" | grep -q '"verified":false'; then
    echo "✅ Código inválido correctamente rechazado"
else
    echo "❌ Error: código inválido fue aceptado"
    exit 1
fi

echo "6️⃣ VERIFICANDO CÓDIGO DE RECUPERACIÓN"
status=$(make_request "POST" "/api/2fa/verify-recovery/$TEST_USER" "$recovery_code" "text/plain")
check_status "200" "$status" "Verificar código de recuperación"

echo "7️⃣ INTENTANDO USAR EL MISMO CÓDIGO DE RECUPERACIÓN (DEBE FALLAR)"
status=$(make_request "POST" "/api/2fa/verify-recovery/$TEST_USER" "$recovery_code" "text/plain")
check_status "200" "$status" "Reusar código de recuperación"

# Verificar que el código usado fue rechazado
if echo "$body" | grep -q '"verified":false'; then
    echo "✅ Código usado correctamente rechazado"
else
    echo "❌ Error: código usado fue aceptado"
    exit 1
fi

echo "8️⃣ ROTANDO SECRETO 2FA"
status=$(make_request "POST" "/api/2fa/rotate/$TEST_USER")
check_status "200" "$status" "Rotar secreto 2FA"

echo "9️⃣ OBTENIENDO ESTADÍSTICAS DEL SISTEMA"
status=$(make_request "GET" "/api/admin/stats")
check_status "200" "$status" "Obtener estadísticas del sistema"

echo "🔟 LISTANDO USUARIOS CON 2FA"
status=$(make_request "GET" "/api/admin/users")
check_status "200" "$status" "Listar usuarios con 2FA"

echo "1️⃣1️⃣ DESHABILITANDO 2FA"
status=$(make_request "POST" "/api/2fa/disable/$TEST_USER")
check_status "204" "$status" "Deshabilitar 2FA"

echo "1️⃣2️⃣ VERIFICANDO ESTADO DESPUÉS DE DESHABILITAR"
status=$(make_request "GET" "/api/users/$TEST_USER/status")
check_status "200" "$status" "Verificar estado después de deshabilitar"

echo "1️⃣3️⃣ ELIMINANDO USUARIO"
status=$(make_request "DELETE" "/api/users/$TEST_USER")
check_status "204" "$status" "Eliminar usuario"

echo "1️⃣4️⃣ VERIFICANDO QUE EL USUARIO FUE ELIMINADO"
status=$(make_request "GET" "/api/users/$TEST_USER")
check_status "404" "$status" "Verificar usuario eliminado"

echo ""
echo "🎉 ¡TODAS LAS PRUEBAS PASARON EXITOSAMENTE!"
echo "✅ Todos los endpoints están funcionando correctamente"
echo "✅ El flujo completo de 2FA está operativo"
echo "✅ Los códigos de recuperación funcionan correctamente"
echo "✅ Las estadísticas y administración están disponibles"
echo ""
echo "📊 Resumen de pruebas ejecutadas:"
echo "- ✅ Gestión de usuarios (crear, obtener, eliminar)"
echo "- ✅ Habilitar/deshabilitar 2FA"
echo "- ✅ Verificación de códigos TOTP"
echo "- ✅ Códigos de recuperación (uso único)"
echo "- ✅ Rotación de secretos"
echo "- ✅ Estadísticas del sistema"
echo "- ✅ Listado de usuarios con 2FA"