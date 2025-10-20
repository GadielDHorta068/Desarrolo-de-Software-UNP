# Build stage
FROM node:22-alpine AS build

WORKDIR /app

RUN apk add --no-cache python3 make g++

COPY package*.json ./
COPY tailwind.config.js ./
COPY postcss.config.js ./
COPY angular.json ./
COPY tsconfig*.json ./
COPY src ./src
COPY public ./public

RUN npm install --legacy-peer-deps

# Genera artefactos de producción en dist/temp-angular
RUN npm run build

# Run stage (Nginx)
FROM nginx:alpine AS run

# Copiar configuración de Nginx específica de la SPA
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar artefactos construidos al docroot de Nginx
COPY --from=build /app/dist/temp-angular/ /usr/share/nginx/html/

EXPOSE 4200

CMD ["nginx", "-g", "daemon off;"]
