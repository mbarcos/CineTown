-- Elimina todas las sesiones y las compras asociadas.
-- Ejecutar manualmente contra la base de datos deseada.

BEGIN;
SET LOCAL search_path TO cine;

DELETE FROM compra c
USING sesion s
WHERE c.sesion_id = s.id;

DELETE FROM sesion;

-- Debe devolver 0 antes de confirmar la transacción.
SELECT COUNT(*) AS sesiones_restantes FROM sesion;

COMMIT;
