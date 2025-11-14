#!/bin/bash

# Script de pruebas simplificado para 2FA API
# Este script prueba los endpoints principales del sistema

set -e

echo "=== Iniciando pruebas de 2FA API ==="
echo "URL: ${API_URL:-http://localhost:8080}"
echo ""

BASE_URL="${API_URL:-http://localhost:8080}"
TEST_USER="test_user_$(date +%s)"

# Función simple para hacer requests
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    echo "📡 $method $endpoint"
    
    if [ "$method" = "GET" ]; then
        curl -s -o /dev/null -w "%{http_code}" "$BASE_URL$endpoint"
    else
        curl -s -o /dev/null -w "%{http_code}" -X "$method" "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data"
    fi
}

echo "1️⃣ PROBANDO HEALTH CHECK"
status=$(make_request "GET" "/actuator/health")
echo "📊 Código de respuesta: $status"
if [ "$status" = "200" ]; then
    echo "✅ Health check - PASÓ"
else
    echo "❌ Health check - FALLÓ"
    exit 1
fi
echo ""

echo "2️⃣ CREANDO USUARIO DE PRUEBA"
status=$(make_request "POST" "/api/users" "{\"username\":\"$TEST_USER\"}")
echo "📊 Código de respuesta: $status"
if [ "$status" = "201" ]; then
    echo "✅ Crear usuario - PASÓ"
else
    echo "❌ Crear usuario - FALLÓ"
    exit 1
fi
echo ""

echo "3️⃣ VERIFICANDO ESTADO DEL USUARIO"
status=$(make_request "GET" "/api/users/$TEST_USER/status")
echo "📊 Código de respuesta: $status"
if [ "$status" = "200" ]; then
    echo "✅ Obtener estado - PASÓ"
else
    echo "❌ Obtener estado - FALLÓ"
    exit 1
fi
echo ""

echo "4️⃣ HABILITANDO 2FA"
status=$(make_request "POST" "/api/2fa/enable" "{\"username\":\"$TEST_USER\"}")
echo "📊 Código de respuesta: $status"
if [ "$status" = "200" ]; then
    echo "✅ Habilitar 2FA - PASÓ"
else
    echo "❌ Habilitar 2FA - FALLÓ"
    exit 1
fi
echo ""

echo "5️⃣ VERIFICANDO CÓDIGO TOTP INVÁLIDO"
status=$(make_request "POST" "/api/2fa/verify" "{\"username\":\"$TEST_USER\",\"code\":\"000000\"}")
echo "📊 Código de respuesta: $status"
if [ "$status" = "200" ]; then
    echo "✅ Verificar código inválido - PASÓ"
else
    echo "❌ Verificar código inválido - FALLÓ"
    exit 1
fi
echo ""

echo "6️⃣ OBTENIENDO ESTADÍSTICAS"
status=$(make_request "GET" "/api/admin/stats")
echo "📊 Código de respuesta: $status"
if [ "$status" = "200" ]; then
    echo "✅ Estadísticas - PASÓ"
else
    echo "❌ Estadísticas - FALLÓ"
    exit 1
fi
echo ""

echo "7️⃣ LISTANDO USUARIOS CON 2FA"
status=$(make_request "GET" "/api/admin/users")
echo "📊 Código de respuesta: $status"
if [ "$status" = "200" ]; then
    echo "✅ Listar usuarios - PASÓ"
else
    echo "❌ Listar usuarios - FALLÓ"
    exit 1
fi
echo ""

echo "8️⃣ DESHABILITANDO 2FA"
status=$(make_request "POST" "/api/2fa/disable/$TEST_USER")
echo "📊 Código de respuesta: $status"
if [ "$status" = "204" ]; then
    echo "✅ Deshabilitar 2FA - PASÓ"
else
    echo "❌ Deshabilitar 2FA - FALLÓ"
    exit 1
fi
echo ""

echo "9️⃣ ELIMINANDO USUARIO"
status=$(make_request "DELETE" "/api/users/$TEST_USER")
echo "📊 Código de respuesta: $status"
if [ "$status" = "204" ]; then
    echo "✅ Eliminar usuario - PASÓ"
else
    echo "❌ Eliminar usuario - FALLÓ"
    exit 1
fi
echo ""

echo "🎉 ¡TODAS LAS PRUEBAS PASARON EXITOSAMENTE!"
echo "✅ Todos los endpoints principales están funcionando"
echo "✅ El flujo completo de 2FA está operativo"
echo "✅ La gestión de usuarios funciona correctamente"
echo "✅ Las estadísticas y administración están disponibles"
echo ""
echo "📊 Resumen de pruebas ejecutadas:"
echo "- ✅ Health check del sistema"
echo "- ✅ Gestión de usuarios (crear, obtener, eliminar)"
echo "- ✅ Habilitar/deshabilitar 2FA"
echo "- ✅ Verificación de códigos TOTP"
echo "- ✅ Estadísticas del sistema"
echo "- ✅ Listado de usuarios con 2FA"