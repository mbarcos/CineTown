-- Completa las sesiones de Coco (pelicula_id = 5) del 14/11/2026.
-- Inserta compras activas anónimas en grupos de hasta 10 entradas,
-- respetando el aforo y el límite de entradas por compra.
-- Si ya están completas, no añade compras. Ejecutar en PostgreSQL.

BEGIN;
SET LOCAL search_path TO cine;

WITH sesiones AS (
    SELECT s.id AS sesion_id,
           s.fecha_hora_inicio,
           s.precio_base,
           sa.capacidad,
           GREATEST(sa.capacidad - COALESCE(SUM(c.num_entradas), 0), 0)::integer
               AS entradas_pendientes
    FROM sesion s
    JOIN sala sa ON sa.id = s.sala_id
    LEFT JOIN compra c ON c.sesion_id = s.id AND c.estado = TRUE
    WHERE s.pelicula_id = 5
      AND s.fecha_hora_inicio >= TIMESTAMP '2026-11-14 00:00:00'
      AND s.fecha_hora_inicio <  TIMESTAMP '2026-11-15 00:00:00'
    GROUP BY s.id, s.fecha_hora_inicio, s.precio_base, sa.capacidad
), compras AS (
    SELECT s.sesion_id,
           s.fecha_hora_inicio,
           s.precio_base,
           LEAST(10, s.entradas_pendientes - (g.n - 1) * 10)::integer AS entradas
    FROM sesiones s
    CROSS JOIN LATERAL generate_series(
        1, (s.entradas_pendientes + 9) / 10
    ) AS g(n)
    WHERE s.entradas_pendientes > 0
)
INSERT INTO compra (sesion_id, usuario_id, email, num_entradas, cod_compra,
                    fecha_compra, precio_total, estado, parking_usado)
SELECT c.sesion_id,
       NULL,
       'aforo-coco-' || c.sesion_id || '-' || g.n || '@cinetown.test',
       c.entradas,
       'CT' || lpad(nextval('compra_id_seq')::text, 6, '0'),
       c.fecha_hora_inicio - INTERVAL '1 day',
       c.precio_base * c.entradas,
       TRUE,
       FALSE
FROM compras c
CROSS JOIN LATERAL generate_series(
    1, (SELECT count(*) FROM compras c2 WHERE c2.sesion_id = c.sesion_id)
) AS g(n);

COMMIT;

-- Verifica el aforo de las sesiones afectadas.
SELECT s.id AS sesion_id,
       p.titulo AS pelicula,
       sa.numero AS sala,
       s.fecha_hora_inicio,
       sa.capacidad,
       COALESCE(SUM(c.num_entradas) FILTER (WHERE c.estado = TRUE), 0)
           AS entradas_vendidas
FROM sesion s
JOIN pelicula p ON p.id = s.pelicula_id
JOIN sala sa ON sa.id = s.sala_id
LEFT JOIN compra c ON c.sesion_id = s.id
WHERE s.pelicula_id = 5
  AND s.fecha_hora_inicio >= TIMESTAMP '2026-11-14 00:00:00'
  AND s.fecha_hora_inicio <  TIMESTAMP '2026-11-15 00:00:00'
GROUP BY s.id, p.titulo, sa.numero, s.fecha_hora_inicio, sa.capacidad
ORDER BY s.fecha_hora_inicio;
