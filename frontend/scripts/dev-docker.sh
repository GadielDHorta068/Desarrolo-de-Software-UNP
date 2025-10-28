#!/bin/sh

# Script de inicialización para desarrollo con Docker
# Instala dependencias si no existen y luego inicia el servidor de desarrollo

echo "🚀 Iniciando servidor de desarrollo..."

# Verificar si node_modules existe y tiene contenido
if [ ! -d "node_modules" ] || [ -z "$(ls -A node_modules 2>/dev/null)" ]; then
    echo "📦 Instalando dependencias..."
    npm install --legacy-peer-deps
    echo "✅ Dependencias instaladas correctamente"
else
    echo "✅ Dependencias ya están instaladas"
fi

# Verificar si las dependencias específicas están instaladas
echo "🔍 Verificando dependencias críticas..."
npm list jspdf jspdf-autotable xlsx file-saver > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "⚠️  Algunas dependencias faltan, reinstalando..."
    npm install --legacy-peer-deps
fi

echo "🎯 Iniciando Angular en modo desarrollo..."
exec npm start