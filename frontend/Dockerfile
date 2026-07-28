# Stage 1: Build de Angular
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production

# Stage 2: Servir con Nginx (Esta es la imagen final que va a Docker Hub)
FROM nginx:alpine
COPY --from=builder /app/dist/*/browser /usr/share/nginx/html

# Se copian las reglas del Proxy Inverso y el Routing de Angular dentro de la imagen
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]