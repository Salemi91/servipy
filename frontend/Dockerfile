# ---- Stage 1: Build ----
FROM node:20-alpine AS build

WORKDIR /app

# Copiar package files primero (cache de dependencias)
COPY package.json package-lock.json ./
RUN npm ci

# Copiar código fuente y compilar
COPY . .
RUN npm run build -- --configuration=production

# ---- Stage 2: Runtime ----
FROM nginx:alpine

# Remover config default de nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copiar config personalizada
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar archivos compilados de Angular (builder 'application' output)
COPY --from=build /app/dist/servipy-frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
