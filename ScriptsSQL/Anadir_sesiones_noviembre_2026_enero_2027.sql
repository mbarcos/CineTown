-- =====================================================================
-- CineTown · Sesiones y pocas compras (13/11/2026 - 13/01/2027)
-- ---------------------------------------------------------------------
-- Programa 3 o 4 sesiones semanales por película activa, repartidas
-- durante la semana. Usa las 5 salas en dos franjas (19:00 y 23:30).
--
-- Requisito: el rango debe estar vacío de sesiones. Si aún hay datos,
-- ejecuta primero Vaciar_sesiones.sql. Este script no borra datos.
--
-- Ventas: agota una sesión en sala 5 (10 entradas por compra) y añade
-- solo dos compras pequeñas más. Muestra los datos de la sesión agotada
-- al final. Todo se puede revertir con ROLLBACK si se revisa antes.
-- =====================================================================

BEGIN;
SET LOCAL search_path TO cine;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM sesion
        WHERE fecha_hora_inicio >= TIMESTAMP '2026-11-13 00:00:00'
          AND fecha_hora_inicio <  TIMESTAMP '2027-01-14 00:00:00'
    ) THEN
        RAISE EXCEPTION
            'El rango ya contiene sesiones. Ejecuta primero Vaciar_sesiones.sql.';
    END IF;

    IF (SELECT COUNT(*) FROM sala) <> 5 THEN
        RAISE EXCEPTION 'Este calendario necesita exactamente 5 salas.';
    END IF;

    IF EXISTS (SELECT 1 FROM pelicula WHERE activo AND duracion > 250) THEN
        RAISE EXCEPTION
            'Hay películas de más de 250 minutos; no caben en las franjas previstas.';
    END IF;
END $$;

CREATE TEMPORARY TABLE nuevas_sesiones ON COMMIT DROP AS
WITH peliculas AS (
    SELECT id,
           duracion,
           row_number() OVER (ORDER BY id) - 1 AS posicion
    FROM pelicula
    WHERE activo = TRUE
),
semanas AS (
    SELECT n AS numero
    FROM generate_series(0, 8) AS serie(n)
),
salas AS (
    SELECT id,
           numero,
           capacidad,
           row_number() OVER (ORDER BY numero) - 1 AS posicion
    FROM sala
),
sesiones_por_semana AS (
    SELECT p.id AS pelicula_id,
           p.duracion,
           p.posicion,
           w.numero AS semana,
           CASE WHEN (p.posicion + w.numero) % 4 = 0 THEN 4 ELSE 3 END
               AS cantidad
    FROM peliculas p
    CROSS JOIN semanas w
),
fechas AS (
    SELECT ps.pelicula_id,
           ps.duracion,
           ps.posicion,
           ps.semana,
           ocurrencia.n AS numero_sesion_semana,
           DATE '2026-11-13'
               + (ps.semana * 7
                  + ((ps.posicion + ps.semana + ocurrencia.n * 2) % 7))::integer
               AS dia
    FROM sesiones_por_semana ps
    CROSS JOIN LATERAL generate_series(0, ps.cantidad - 1)
        AS ocurrencia(n)
),
fechas_en_rango AS (
    SELECT *
    FROM fechas
    WHERE dia <= DATE '2027-01-13'
),
ordenadas AS (
    SELECT f.*,
           row_number() OVER (
               PARTITION BY f.dia
               ORDER BY f.posicion, f.numero_sesion_semana
           ) - 1 AS orden_dia
    FROM fechas_en_rango f
),
asignadas AS (
    SELECT o.*,
           sa.id AS sala_id,
           sa.numero AS numero_sala,
           sa.capacidad,
           (o.orden_dia / (SELECT COUNT(*) FROM salas))::integer AS franja
    FROM ordenadas o
    JOIN salas sa
      ON sa.posicion = o.orden_dia % (SELECT COUNT(*) FROM salas)
)
SELECT a.pelicula_id,
       a.dia,
       a.posicion AS posicion_pelicula,
       a.numero_sesion_semana,
       a.sala_id,
       a.numero_sala,
       a.capacidad,
       a.duracion,
       a.dia + CASE a.franja
           WHEN 0 THEN TIME '19:00'
           WHEN 1 THEN TIME '23:30'
       END AS fecha_hora_inicio,
       CASE a.franja
           WHEN 0 THEN NUMERIC '8.50'
           WHEN 1 THEN NUMERIC '9.50'
       END
       + CASE WHEN EXTRACT(ISODOW FROM a.dia) >= 5
              THEN NUMERIC '1.50' ELSE NUMERIC '0.00' END AS precio_base
FROM asignadas a;

DO $$
BEGIN
    IF EXISTS (
        SELECT dia
        FROM nuevas_sesiones
        GROUP BY dia
        HAVING COUNT(*) > 10
    ) THEN
        RAISE EXCEPTION
            'Algún día supera las 10 sesiones que caben en estas franjas y salas.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM nuevas_sesiones
        WHERE dia = DATE '2026-11-14'
          AND numero_sala = 5
          AND fecha_hora_inicio = TIMESTAMP '2026-11-14 19:00:00'
    ) THEN
        RAISE EXCEPTION
            'No se pudo reservar la sesión que debía quedar agotada.';
    END IF;
END $$;

INSERT INTO sesion (pelicula_id, sala_id, fecha_hora_inicio,
                    fecha_hora_fin, precio_base)
SELECT ns.pelicula_id,
       ns.sala_id,
       ns.fecha_hora_inicio,
       ns.fecha_hora_inicio + ns.duracion * INTERVAL '1 minute',
       ns.precio_base
FROM nuevas_sesiones ns
ORDER BY ns.fecha_hora_inicio, ns.numero_sala;

CREATE TEMPORARY TABLE sesion_agotada ON COMMIT DROP AS
SELECT s.id AS sesion_id,
       p.titulo,
       sa.numero AS numero_sala,
       sa.capacidad,
       s.fecha_hora_inicio,
       s.precio_base
FROM sesion s
JOIN pelicula p ON p.id = s.pelicula_id
JOIN sala sa ON sa.id = s.sala_id
WHERE s.fecha_hora_inicio = TIMESTAMP '2026-11-14 19:00:00'
  AND sa.numero = 5;

-- Sala 5 tiene 60 asientos: seis compras de 10 entradas la dejan llena.
INSERT INTO compra (sesion_id, usuario_id, email, num_entradas, cod_compra,
                    fecha_compra, precio_total, estado, parking_usado)
SELECT ll.sesion_id,
       NULL,
       'aforo-script@cinetown.test',
       10,
       'CT' || lpad(nextval('compra_id_seq')::text, 6, '0'),
       ll.fecha_hora_inicio - INTERVAL '1 day',
       ll.precio_base * 10,
       TRUE,
       FALSE
FROM sesion_agotada ll
CROSS JOIN generate_series(1, ll.capacidad / 10);

-- Dos compras pequeñas adicionales, cada una en una sesión distinta.
WITH sesiones_pequenas AS (
    SELECT s.id AS sesion_id,
           s.fecha_hora_inicio,
           s.precio_base,
           row_number() OVER (ORDER BY s.fecha_hora_inicio, s.id) AS orden
    FROM sesion s
    WHERE s.fecha_hora_inicio >= TIMESTAMP '2026-11-13 00:00:00'
      AND s.fecha_hora_inicio <  TIMESTAMP '2027-01-14 00:00:00'
      AND s.id <> (SELECT sesion_id FROM sesion_agotada)
)
INSERT INTO compra (sesion_id, usuario_id, email, num_entradas, cod_compra,
                    fecha_compra, precio_total, estado, parking_usado)
SELECT sp.sesion_id,
       NULL,
       'compra-script-' || sp.orden || '@cinetown.test',
       2,
       'CT' || lpad(nextval('compra_id_seq')::text, 6, '0'),
       sp.fecha_hora_inicio - INTERVAL '1 day',
       CASE WHEN EXTRACT(ISODOW FROM sp.fecha_hora_inicio) = 3
            THEN round(sp.precio_base * 0.70, 2) * 2
            ELSE sp.precio_base * 2 END,
       TRUE,
       FALSE
FROM sesiones_pequenas sp
WHERE sp.orden <= 2;

-- Resumen de sesiones y compras generadas.
SELECT COUNT(*) AS sesiones_insertadas FROM nuevas_sesiones;

SELECT ll.titulo,
       ll.numero_sala,
       ll.fecha_hora_inicio,
       ll.capacidad AS asientos,
       COALESCE(SUM(c.num_entradas) FILTER (WHERE c.estado), 0)
           AS entradas_vendidas,
       ll.capacidad - COALESCE(SUM(c.num_entradas)
           FILTER (WHERE c.estado), 0) AS asientos_libres
FROM sesion_agotada ll
LEFT JOIN compra c ON c.sesion_id = ll.sesion_id
GROUP BY ll.titulo, ll.numero_sala, ll.fecha_hora_inicio, ll.capacidad;

COMMIT;
