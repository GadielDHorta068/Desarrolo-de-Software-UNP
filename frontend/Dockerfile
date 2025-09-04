# Etapa de desarrollo optimizada para hot reload
FROM node:22-alpine

# Directorio de trabajo
WORKDIR /app

# Instalar dependencias del sistema
RUN apk add --no-cache python3 make g++

# Copiar archivos de configuración y dependencias
COPY package*.json ./
COPY tailwind.config.js ./
COPY postcss.config.js ./

# Instalar dependencias
RUN npm install --legacy-peer-deps

# Exponer puerto
EXPOSE 4200

# Configurar variables de entorno para evitar problemas de memoria y optimizar hot reload
ENV NODE_OPTIONS="--max-old-space-size=4096"
ENV CHOKIDAR_USEPOLLING=true
ENV CHOKIDAR_INTERVAL=1000
ENV WATCHPACK_POLLING=true
ENV NODE_ENV=development

# Comando por defecto
CMD ["npm", "start"]
