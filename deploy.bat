@echo off
setlocal enabledelayedexpansion

:: --- 1. Cargar variables del archivo .env ---
if not exist .env (
    echo [ERROR] No se encontro el archivo .env
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    set "line=%%A"
    if not "!line:~0,1!"=="#" (
        if not "%%A"=="" set "%%A=%%B"
    )
)
echo [OK] Variables de entorno de .env cargadas.

:: --- 2. Levantar los contenedores ---
echo.
echo [DEPLOY] Levantando la pila con Docker Compose...
docker compose up -d --build
if errorlevel 1 (
    echo [ERROR] Docker Compose fallo al levantar los servicios.
    exit /b 1
)

:: --- 3. Esperar a que Flyway cree las tablas ---
echo.
echo [DEPLOY] Esperando a que el backend y Flyway creen la estructura en MySQL...

:wait_loop
timeout /t 3 /nobreak >nul
docker exec servipy-mysql mysql -u%MYSQL_USER% -p%MYSQL_PASSWORD% %MYSQL_DATABASE% -e "DESCRIBE users;" >nul 2>&1
if errorlevel 1 (
    echo     Flyway aun trabajando... reintentando en 3s
    goto wait_loop
)
echo [OK] Tablas creadas por Flyway.

:: --- 4. Cargar seed.sql ---
echo.
echo [DEPLOY] Insertando datos de prueba desde database/seed.sql...
docker exec -i servipy-mysql mysql --default-character-set=utf8mb4 -u%MYSQL_USER% -p%MYSQL_PASSWORD% %MYSQL_DATABASE% < database\seed.sql
if errorlevel 1 (
    echo [WARN] El seed pudo tener errores ^(ej. datos ya existentes^). Revisa la salida anterior.
    goto done
)
echo [OK] Datos de prueba cargados.

:done
echo.
echo [DEPLOY] Despliegue completo. Servicios disponibles:
echo   - Frontend: http://localhost
echo   - Backend:  http://localhost:8080/api/v1/health
echo   - MySQL:    localhost:3306

endlocal
