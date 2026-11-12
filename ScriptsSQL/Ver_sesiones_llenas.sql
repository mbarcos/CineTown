-- Muestra las sesiones cuyo aforo está completo.
-- Solo cuenta compras activas (estado = TRUE).

SELECT
    s.id AS sesion_id,
    p.titulo AS pelicula,
    sa.numero AS sala,
    s.fecha_hora_inicio,
    sa.capacidad,
    COALESCE(SUM(c.num_entradas), 0) AS entradas_vendidas
FROM sesion s
JOIN pelicula p ON p.id = s.pelicula_id
JOIN sala sa ON sa.id = s.sala_id
LEFT JOIN compra c
       ON c.sesion_id = s.id
      AND c.estado = TRUE
GROUP BY s.id, p.titulo, sa.numero, s.fecha_hora_inicio, sa.capacidad
HAVING COALESCE(SUM(c.num_entradas), 0) >= sa.capacidad
ORDER BY s.fecha_hora_inicio;
