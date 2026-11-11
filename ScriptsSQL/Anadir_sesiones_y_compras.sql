-- =====================================================================
-- CineTown · Añadir sesiones y algunas compras de ejemplo
-- ---------------------------------------------------------------------
-- Añade siete sesiones futuras por película (incluidas las películas
-- inactivas), distribuidas entre las salas existentes. Añade una compra
-- de un cliente por película, para la primera de sus nuevas sesiones.
--
-- Es idempotente: evita repetir sesiones y compras si se vuelve a ejecutar.
-- Las compras solo se crean si hay al menos un usuario con rol cliente.
-- =====================================================================
ROLLBACK;
BEGIN;
SET LOCAL search_path TO cine;

CREATE TEMPORARY TABLE nuevas_sesiones ON COMMIT DROP AS
WITH peliculas AS (
    SELECT id, duracion,
           row_number() OVER (ORDER BY id) - 1 AS posicion
    FROM pelicula
),
salas AS (
    SELECT id,
           row_number() OVER (ORDER BY numero) - 1 AS posicion,
           count(*) OVER () AS total
    FROM sala
),
turnos AS (
    SELECT turno,
           CASE (turno - 1) % 3
               WHEN 0 THEN TIME '16:00'
               WHEN 1 THEN TIME '19:00'
               ELSE TIME '22:00'
           END AS hora,
           CASE (turno - 1) % 3
               WHEN 0 THEN NUMERIC '6.50'
               WHEN 1 THEN NUMERIC '8.50'
               ELSE NUMERIC '9.50'
           END AS precio
    FROM generate_series(1, 7) AS serie(turno)
),
candidatas AS (
    SELECT p.id AS pelicula_id,
           sa.id AS sala_id,
           DATE '2030-01-01' + p.posicion::integer * 49
               + (t.turno - 1) * 7 + t.hora
               AS inicio,
           p.duracion,
           t.precio + CASE
               WHEN EXTRACT(ISODOW FROM DATE '2030-01-01'
                    + p.posicion::integer * 49 + (t.turno - 1) * 7) >= 5
               THEN 1.50 ELSE 0
           END AS precio_base,
           p.posicion,
           t.turno
    FROM peliculas p
    CROSS JOIN turnos t
    JOIN salas sa
      ON sa.posicion = (p.posicion * 7 + t.turno - 1) % sa.total
)
SELECT c.pelicula_id,
       c.sala_id,
       c.inicio AS fecha_hora_inicio,
       c.inicio + c.duracion * INTERVAL '1 minute' AS fecha_hora_fin,
       c.precio_base,
       c.posicion,
       c.turno
FROM candidatas c
WHERE NOT EXISTS (
    SELECT 1
    FROM sesion s
    WHERE s.pelicula_id = c.pelicula_id
      AND s.sala_id = c.sala_id
      AND s.fecha_hora_inicio = c.inicio
);

INSERT INTO sesion (pelicula_id, sala_id, fecha_hora_inicio,
                    fecha_hora_fin, precio_base)
SELECT pelicula_id, sala_id, fecha_hora_inicio, fecha_hora_fin, precio_base
FROM nuevas_sesiones;

-- Una compra por película, en su primera sesión nueva, si hay clientes.
WITH clientes AS (
    SELECT array_agg(id ORDER BY id) AS ids
    FROM usuario
    WHERE rol = 'cliente'
),
sesiones_para_compra AS (
    SELECT s.id AS sesion_id,
           s.pelicula_id,
           s.fecha_hora_inicio,
           s.precio_base,
           row_number() OVER (
               PARTITION BY s.pelicula_id ORDER BY s.fecha_hora_inicio
           ) AS numero_sesion
    FROM sesion s
    JOIN nuevas_sesiones n
      ON n.pelicula_id = s.pelicula_id
     AND n.fecha_hora_inicio = s.fecha_hora_inicio
)
INSERT INTO compra (sesion_id, usuario_id, email, num_entradas, cod_compra,
                    fecha_compra, precio_total, estado, parking_usado)
SELECT s.sesion_id,
       c.ids[1 + ((s.pelicula_id - 1) % cardinality(c.ids))::integer],
       'cliente' || c.ids[1 + ((s.pelicula_id - 1)
           % cardinality(c.ids))::integer] || '@cinetown.test',
       2,
       'CT' || lpad(nextval('compra_id_seq')::text, 6, '0'),
       s.fecha_hora_inicio - INTERVAL '2 days',
       CASE
           WHEN EXTRACT(ISODOW FROM s.fecha_hora_inicio) = 3
               THEN round(s.precio_base * 0.70, 2) * 2
           ELSE s.precio_base * 2
       END,
       TRUE,
       FALSE
FROM sesiones_para_compra s
CROSS JOIN clientes c
WHERE s.numero_sesion = 1
  AND COALESCE(cardinality(c.ids), 0) > 0
  AND NOT EXISTS (
      SELECT 1 FROM compra co WHERE co.sesion_id = s.sesion_id
  );

COMMIT;

-- Comprobaciones opcionales.
SELECT p.titulo, COUNT(s.id) AS total_sesiones
FROM pelicula p
LEFT JOIN sesion s ON s.pelicula_id = p.id
GROUP BY p.id, p.titulo
ORDER BY p.id;

SELECT COUNT(*) AS compras_de_clientes
FROM compra c
JOIN usuario u ON u.id = c.usuario_id
WHERE u.rol = 'cliente';
