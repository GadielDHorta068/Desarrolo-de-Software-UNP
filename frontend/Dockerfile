# Etapa de desarrollo
FROM node:18-alpine

# Directorio de trabajo
WORKDIR /app

# Instalar dependencias del sistema
RUN apk add --no-cache python3 make g++

# Copiar package.json y package-lock.json
COPY package*.json ./

# Instalar dependencias con npm install en lugar de npm ci
RUN npm install

# Copiar código fuente
COPY . .

# Exponer puerto
EXPOSE 4200

# Configurar variables de entorno para evitar problemas de memoria
ENV NODE_OPTIONS="--max-old-space-size=4096"

# Comando por defecto
CMD ["npm", "start"]
