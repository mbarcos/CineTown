-- =====================================================================
-- CineTown · Recarga de datos de ejemplo
-- ---------------------------------------------------------------------
-- * Borra: compra, sesion, pelicula_genero, pelicula, genero y sala.
-- * NO toca la tabla usuario (ni sus datos ni su secuencia).
-- * Inserta: salas, géneros, películas, sesiones (01/10/2026 - 31/12/2027)
--   y compras de ejemplo.
-- * Todo va en una transacción: si algo falla, no se cambia nada.
--
-- Si usas un schema distinto de public (spring.jpa...default_schema),
-- descomenta y ajusta la siguiente línea:
SET search_path TO cine;
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1. LIMPIEZA
-- Las tablas se vacían en un único TRUNCATE porque se referencian entre
-- sí. RESTART IDENTITY reinicia los SERIAL a 1. usuario no se incluye.
-- ---------------------------------------------------------------------
TRUNCATE TABLE compra, sesion, pelicula_genero, pelicula, genero, sala
    RESTART IDENTITY;

-- ---------------------------------------------------------------------
-- 2. SALAS
-- ---------------------------------------------------------------------
INSERT INTO sala (numero, capacidad) VALUES
    (1, 120),
    (2, 100),
    (3, 100),
    (4, 80),
    (5, 60);

-- ---------------------------------------------------------------------
-- 3. GÉNEROS
-- ---------------------------------------------------------------------
INSERT INTO genero (nombre) VALUES
    ('Acción'),
    ('Animación'),
    ('Aventura'),
    ('Ciencia ficción'),
    ('Comedia'),
    ('Crimen'),
    ('Drama'),
    ('Familiar'),
    ('Fantasía'),
    ('Romance'),
    ('Terror'),
    ('Thriller');

-- ---------------------------------------------------------------------
-- 4. PELÍCULAS
-- Interstellar es la primera (id 1). La última está retirada de cartelera
-- (activo = false) y no tendrá sesiones.
-- Todas duran menos de 175 min para que las 3 franjas diarias no se pisen.
-- ---------------------------------------------------------------------
INSERT INTO pelicula (titulo, sinopsis, duracion, anio, clasificacion, activo) VALUES
    ('Interstellar',
     'Un grupo de astronautas atraviesa un agujero de gusano en busca de un nuevo hogar para la humanidad.',
     169, 2014, 'P12', TRUE),
    ('Origen',
     'Un ladrón especializado en robar secretos desde los sueños recibe una misión imposible: implantar una idea.',
     148, 2010, 'P12', TRUE),
    ('Parásitos',
     'Una familia sin recursos se infiltra poco a poco en la vida de una familia adinerada.',
     132, 2019, 'P16', TRUE),
    ('Matrix',
     'Un programador descubre que la realidad que conoce es una simulación controlada por las máquinas.',
     136, 1999, 'P16', TRUE),
    ('Coco',
     'Un niño amante de la música viaja a la Tierra de los Muertos para descubrir la historia de su familia.',
     105, 2017, 'TP', TRUE),
    ('El rey león',
     'Un joven león debe asumir su destino tras la muerte de su padre y recuperar su reino.',
     88, 1994, 'TP', TRUE),
    ('Toy Story',
     'Los juguetes de un niño cobran vida cuando nadie los mira y afrontan la llegada de un nuevo rival.',
     81, 1995, 'TP', TRUE),
    ('El viaje de Chihiro',
     'Una niña queda atrapada en un mundo de espíritus y debe trabajar en unos baños para rescatar a sus padres.',
     125, 2001, 'P7', TRUE),
    ('Joker',
     'Un cómico fracasado se hunde en la locura y se convierte en el criminal más temido de Gotham.',
     122, 2019, 'P16', TRUE),
    ('Mad Max: Furia en la carretera',
     'En un desierto postapocalíptico, una guerrera y un vagabundo huyen de un tirano en una persecución interminable.',
     120, 2015, 'P16', TRUE),
    ('Amélie',
     'Una joven parisina decide mejorar en secreto la vida de quienes la rodean mientras busca su propio amor.',
     122, 2001, 'P12', TRUE),
    ('Regreso al futuro',
     'Un adolescente viaja treinta años atrás en una máquina del tiempo y pone en riesgo su propia existencia.',
     116, 1985, 'P7', TRUE),
    ('Pulp Fiction',
     'Varias historias de gánsteres, boxeadores y atracadores se entrelazan en la ciudad de Los Ángeles.',
     154, 1994, 'P18', TRUE),
    ('Saw',
     'Dos desconocidos despiertan encadenados en un baño y deben seguir los juegos macabros de un asesino.',
     103, 2004, 'P18', FALSE);

-- ---------------------------------------------------------------------
-- 5. PELÍCULA - GÉNERO
-- Se relaciona por título y nombre para no depender de los id.
-- ---------------------------------------------------------------------
INSERT INTO pelicula_genero (pelicula_id, genero_id)
SELECT p.id, g.id
FROM (VALUES
    ('Interstellar',                    'Ciencia ficción'),
    ('Interstellar',                    'Drama'),
    ('Interstellar',                    'Aventura'),
    ('Origen',                          'Ciencia ficción'),
    ('Origen',                          'Acción'),
    ('Origen',                          'Thriller'),
    ('Parásitos',                       'Drama'),
    ('Parásitos',                       'Thriller'),
    ('Parásitos',                       'Comedia'),
    ('Matrix',                          'Ciencia ficción'),
    ('Matrix',                          'Acción'),
    ('Coco',                            'Animación'),
    ('Coco',                            'Familiar'),
    ('Coco',                            'Aventura'),
    ('El rey león',                     'Animación'),
    ('El rey león',                     'Familiar'),
    ('El rey león',                     'Drama'),
    ('Toy Story',                       'Animación'),
    ('Toy Story',                       'Familiar'),
    ('Toy Story',                       'Comedia'),
    ('El viaje de Chihiro',             'Animación'),
    ('El viaje de Chihiro',             'Fantasía'),
    ('El viaje de Chihiro',             'Aventura'),
    ('Joker',                           'Drama'),
    ('Joker',                           'Thriller'),
    ('Joker',                           'Crimen'),
    ('Mad Max: Furia en la carretera',  'Acción'),
    ('Mad Max: Furia en la carretera',  'Ciencia ficción'),
    ('Mad Max: Furia en la carretera',  'Aventura'),
    ('Amélie',                          'Comedia'),
    ('Amélie',                          'Romance'),
    ('Regreso al futuro',               'Ciencia ficción'),
    ('Regreso al futuro',               'Comedia'),
    ('Regreso al futuro',               'Aventura'),
    ('Pulp Fiction',                    'Crimen'),
    ('Pulp Fiction',                    'Thriller'),
    ('Pulp Fiction',                    'Comedia'),
    ('Saw',                             'Terror'),
    ('Saw',                             'Thriller')
) AS rel (titulo, genero)
JOIN pelicula p ON p.titulo = rel.titulo
JOIN genero   g ON g.nombre = rel.genero;

-- ---------------------------------------------------------------------
-- 6. SESIONES (01/10/2026 - 31/12/2027)
-- 5 salas x 3 franjas diarias (16:00, 19:00 y 22:00), solo con películas
-- activas, rotando para que cambie la programación cada día y cada sala.
-- Precio base por franja; +1,50 € de viernes a domingo.
-- ---------------------------------------------------------------------
INSERT INTO sesion (pelicula_id, sala_id, fecha_hora_inicio, fecha_hora_fin, precio_base)
WITH activas AS (
    SELECT id, duracion, row_number() OVER (ORDER BY id) - 1 AS pos
    FROM pelicula
    WHERE activo
),
total AS (
    SELECT count(*) AS n FROM activas
),
dias AS (
    SELECT d::date AS dia, d::date - DATE '2026-10-01' AS idx
    FROM generate_series(DATE '2026-10-01', DATE '2027-12-31', INTERVAL '1 day') AS d
),
franjas (franja, hora, precio) AS (
    VALUES (0, TIME '16:00', 6.50),
           (1, TIME '19:00', 8.50),
           (2, TIME '22:00', 9.50)
),
salas AS (
    SELECT id, row_number() OVER (ORDER BY numero) - 1 AS pos
    FROM sala
)
SELECT p.id,
       sa.id,
       d.dia + f.hora,
       d.dia + f.hora + p.duracion * INTERVAL '1 minute',
       f.precio + CASE WHEN EXTRACT(ISODOW FROM d.dia) >= 5 THEN 1.50 ELSE 0 END
FROM dias d
CROSS JOIN franjas f
CROSS JOIN salas sa
CROSS JOIN total t
JOIN activas p ON p.pos = (d.idx * 2 + sa.pos * 5 + f.franja) % t.n;

-- ---------------------------------------------------------------------
-- 7. COMPRAS DE EJEMPLO
-- * Sesiones hasta el 31/03/2027, dejando 1 de cada 4 sin ventas.
-- * Códigos CIN00001, CIN00002...
-- * Se reparten entre los usuarios que YA existen con rol 'cliente'
--   (1 de cada 4 compras queda sin usuario, como una venta en taquilla).
--   Si no hay clientes, todas quedan sin usuario.
-- * 1 de cada 9 está cancelada (estado = false).
-- * Máximo 3 compras x 6 entradas por sesión (18), siempre por debajo de
--   la capacidad de la sala más pequeña.
-- ---------------------------------------------------------------------
INSERT INTO compra (sesion_id, usuario_id, email, num_entradas, cod_compra,
                    fecha_compra, precio_total, estado, parking_usado)
WITH clientes AS (
    SELECT array_agg(id ORDER BY id) AS ids
    FROM usuario
    WHERE rol = 'cliente'
),
base AS (
    SELECT s.id AS sesion_id,
           s.fecha_hora_inicio,
           s.precio_base,
           row_number() OVER (ORDER BY s.id, k) AS rn
    FROM sesion s
    CROSS JOIN LATERAL generate_series(1, 1 + (s.id % 3)) AS k
    WHERE s.fecha_hora_inicio < TIMESTAMP '2027-04-01'
      AND s.id % 4 <> 0
)
SELECT b.sesion_id,
       CASE
           WHEN COALESCE(cardinality(c.ids), 0) > 0 AND b.rn % 4 <> 0
               THEN c.ids[1 + (b.rn % cardinality(c.ids))::int]
       END,
       'cliente' || b.rn || '@cinetown.test',
       x.entradas,
       'CIN' || lpad(b.rn::text, 5, '0'),
       LEAST(b.fecha_hora_inicio - ((b.rn % 5) + 1) * INTERVAL '5 hours',
             TIMESTAMP '2026-11-09 12:00'),
       x.entradas * b.precio_base,
       b.rn % 9 <> 0,
       FALSE
FROM base b
CROSS JOIN clientes c
CROSS JOIN LATERAL (SELECT 1 + (b.rn % 6)::int AS entradas) AS x;

COMMIT;

-- ---------------------------------------------------------------------
-- COMPROBACIONES RÁPIDAS (opcionales)
-----------------------------------------------------------------------
SELECT count(*) FROM sesion;
SELECT count(*) FROM sesion WHERE fecha_hora_inicio >= '2027-01-01';
SELECT count(*), count(usuario_id) AS con_usuario FROM compra;
SELECT cod_compra FROM compra ORDER BY id LIMIT 3;
