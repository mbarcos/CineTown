# CineTown

## Ejecutar con Docker Compose

Necesitas Docker con el plugin Compose. Prepara la configuración local y edita las credenciales:

```bash
cp .env_example .env
```

El esquema de PostgreSQL y los datos de prueba se crean automáticamente cuando se inicializa un volumen de datos vacío. Compose ejecuta `ScriptsSQL/CrearBDcine - Máximo Barcos.sql`, `ScriptsSQL/Cargar_datos.sql` y `ScriptsSQL/Agregar_carteles_cartelera.sql` en ese orden. Los scripts de inicialización de PostgreSQL solo se ejecutan cuando el volumen de datos está vacío.

Arranca todos los servicios desde la raíz del proyecto:

```bash
docker compose up --build
```

- Frontend Angular SSR: <http://localhost:4000>
- API Spring Boot: <http://localhost:8081>
- PostgreSQL: `localhost:5432`

El backend usa el esquema `cine` y espera que sus tablas ya existan (`ddl-auto=validate`). Para detener los servicios, ejecuta `docker compose down`; los datos siguen guardados en el volumen `postgres_data`. Para borrar también la base persistida y volver a cargar el esquema y los datos de prueba, ejecuta `docker compose down -v`.

## Carteles de la cartelera

Para actualizar una base existente, ejecuta `ScriptsSQL/Agregar_carteles_cartelera.sql` antes de arrancar el backend actualizado. Con Docker Compose, desde la raíz del proyecto:

```bash
docker compose up -d db
docker compose exec -T db sh -c 'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' < ScriptsSQL/Agregar_carteles_cartelera.sql
```

El script guarda las URLs de TMDB de las películas activas identificadas por título y año y se puede volver a ejecutar. Si recargas los datos de ejemplo, ejecútalo después de `Cargar_datos.sql`. La cartelera lee las URLs desde CineTown y el navegador carga los carteles desde el CDN de TMDB.

## Correo de confirmación

Para ejecutar el backend localmente, configura `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` y `MAIL_FROM` en `config.properties` (cargado desde la raíz del proyecto). Si lo ejecutas con Docker Compose, configura esas mismas variables en `.env`. Ambos archivos están ignorados por Git. `MAIL_FROM` debe ser una dirección autorizada para esa cuenta. El backend usa SMTP autenticado con STARTTLS y envía al correo indicado en el formulario un resumen con el código de compra después de confirmar la transacción. Si SMTP falla, la compra permanece confirmada y el fallo se registra en los logs del backend; el código también se muestra en pantalla. No repitas una compra confirmada para intentar reenviar el mensaje.

Si publicas el frontend bajo otro origen, configura `CORS_ALLOWED_ORIGIN`. Si cambias el puerto o dirección pública de la API, configura `API_URL`; esa URL es consumida por el navegador.
