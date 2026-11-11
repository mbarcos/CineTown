-- =====================================================================
-- CineTown · Encriptar contraseñas de clientes introducidas manualmente
-- ---------------------------------------------------------------------
-- Spring Security valida las contraseñas con BCrypt (coste 10).
-- Este script aplica el mismo formato a las contraseñas de clientes que
-- todavía no sean hashes BCrypt válidos. Las contraseñas ya cifradas se
-- conservan, por lo que se puede ejecutar más de una vez con seguridad.
--
-- Requiere PostgreSQL con la extensión pgcrypto disponible.
-- =====================================================================

BEGIN;
SET LOCAL search_path TO cine, public;

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;

-- Recuento opcional antes del cambio; no muestra las contraseñas.
SELECT COUNT(*) AS clientes_con_contrasena_sin_bcrypt
FROM usuario
WHERE rol = 'cliente'
  AND password !~ '^\$2[aby]\$[0-9]{2}\$[./A-Za-z0-9]{53}$';

-- crypt() recibe la contraseña actual y genera un salt BCrypt nuevo.
-- La condición evita volver a cifrar hashes BCrypt existentes.
UPDATE usuario
SET password = public.crypt(password, public.gen_salt('bf', 10))
WHERE rol = 'cliente'
  AND password !~ '^\$2[aby]\$[0-9]{2}\$[./A-Za-z0-9]{53}$';

COMMIT;
