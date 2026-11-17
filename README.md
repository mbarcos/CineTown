# CineTown

## Ejecutar con Docker Compose

Necesitas Docker con el plugin Compose. Prepara la configuración local y edita las credenciales:

```bash
cp .env_example .env_example
```

El esquema de PostgreSQL y los datos de prueba se crean automáticamente cuando se inicializa un volumen de datos vacío. Compose ejecuta `ScriptsSQL/CrearBDcine - Máximo Barcos.sql` primero y `ScriptsSQL/Cargar_datos.sql` después. Los scripts de inicialización de PostgreSQL solo se ejecutan cuando el volumen de datos está vacío.

Arranca todos los servicios desde la raíz del proyecto:

```bash
docker compose up --build
```

- Frontend Angular SSR: <http://localhost:4000>
- API Spring Boot: <http://localhost:8081>
- PostgreSQL: `localhost:5432`

El backend usa el esquema `cine` y espera que sus tablas ya existan (`ddl-auto=validate`). Para detener los servicios, ejecuta `docker compose down`; los datos siguen guardados en el volumen `postgres_data`. Para borrar también la base persistida y volver a cargar el esquema y los datos de prueba, ejecuta `docker compose down -v`.

Si publicas el frontend bajo otro origen, configura `CORS_ALLOWED_ORIGIN`. Si cambias el puerto o dirección pública de la API, configura `API_URL`; esa URL es consumida por el navegador.
