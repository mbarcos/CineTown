-- =====================================================================
-- CineTown · Actualizar fechas de sesiones y compras de ejemplo
-- ---------------------------------------------------------------------
-- Conserva sin cambios una de cada diez sesiones (y las compras de esas
-- sesiones) para poder comprobar cómo se muestran las fechas pasadas.
-- Desplaza las demás sesiones y sus compras el mismo número de días,
-- manteniendo sus horas, el día de la semana y la relación temporal entre
-- compra y sesión. La sesión desplazada más antigua queda como mínimo en
-- 2028, con lo que el resto también queda en el futuro.
--
-- El cambio se hace en una transacción. Este archivo solo define el script;
-- ejecútalo manualmente contra la base de datos que quieras actualizar.
-- =====================================================================

BEGIN;
SET LOCAL search_path TO cine;

-- Selecciona sesiones por id: se deja intacta cada décima (10, 20, 30...).
-- El desplazamiento se redondea a semanas completas para conservar el día
-- de la semana y, por tanto, las sesiones con descuento de miércoles.
CREATE TEMPORARY TABLE sesiones_a_mover ON COMMIT DROP AS
WITH sesiones_ordenadas AS (
    SELECT id,
           row_number() OVER (ORDER BY id) AS posicion
    FROM sesion
),
primera_sesion_a_mover AS (
    SELECT MIN(s.fecha_hora_inicio::date) AS primer_dia
    FROM sesion s
    JOIN sesiones_ordenadas o ON o.id = s.id
    WHERE o.posicion % 10 <> 0
)
SELECT s.id,
       GREATEST(
           0,
           ((DATE '2028-01-01' - p.primer_dia + 6) / 7) * 7
       )::integer AS dias
FROM sesion s
JOIN sesiones_ordenadas o ON o.id = s.id
CROSS JOIN primera_sesion_a_mover p
WHERE o.posicion % 10 <> 0;

-- Mueve las compras con su sesión, manteniendo la antelación original.
UPDATE compra c
SET fecha_compra = c.fecha_compra + m.dias * INTERVAL '1 day'
FROM sesiones_a_mover m
WHERE c.sesion_id = m.id;

UPDATE sesion s
SET fecha_hora_inicio = s.fecha_hora_inicio + m.dias * INTERVAL '1 day',
    fecha_hora_fin = s.fecha_hora_fin + m.dias * INTERVAL '1 day'
FROM sesiones_a_mover m
WHERE s.id = m.id;

COMMIT;

-- Comprobaciones posteriores opcionales.
SELECT COUNT(*) AS sesiones_totales,
       COUNT(*) FILTER (WHERE fecha_hora_inicio >= TIMESTAMP '2028-01-01')
           AS sesiones_desde_2028,
       COUNT(*) FILTER (WHERE fecha_hora_inicio < CURRENT_TIMESTAMP)
           AS sesiones_pasadas_conservadas,
       MIN(fecha_hora_inicio) AS primera_sesion,
       MAX(fecha_hora_inicio) AS ultima_sesion
FROM sesion;

SELECT COUNT(*) AS compras_totales,
       MIN(fecha_compra) AS primera_compra,
       MAX(fecha_compra) AS ultima_compra
FROM compra;
