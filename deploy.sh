#!/bin/bash

# --- 1. Cargar las variables del archivo .env ---
if [ -f .env ]; then
  # 'set -a' exporta automáticamente todas las variables definidas en .env
  set -a
  source .env
  set +a
  echo ">>> Variables de entorno de .env cargadas correctamente."
else
  echo ">>> ERROR: No se encontró el archivo .env"
  exit 1
fi

# --- 2. Desplegar los contenedores ---
echo ">>> Levantando la pila con Docker Compose..."
docker compose up -d

# --- 3. Esperar a que Flyway en el Backend termine de crear las tablas ---
echo ">>> Esperando a que el backend y Flyway creen la estructura en MySQL..."
until docker exec servipy-mysql mysql -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" "${MYSQL_DATABASE}" -e "DESCRIBE users;" > /dev/null 2>&1; do
    echo "    Flyway aún trabajando... reintentando en 3s"
    sleep 3
done

# --- 4. Cargar el seed.sql de prueba/desarrollo ---
echo ">>> Insertando datos de prueba desde database/seed.sql..."
docker exec -i servipy-mysql mysql -u"${MYSQL_USER}" -p"${MYSQL_PASSWORD}" "${MYSQL_DATABASE}" < database/seed.sql

echo ">>> ¡Despliegue y datos iniciales cargados con éxito!"