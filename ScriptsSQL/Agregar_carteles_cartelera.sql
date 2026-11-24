BEGIN;

ALTER TABLE cine.pelicula ADD COLUMN IF NOT EXISTS cartel_url VARCHAR(255);

-- Carteles de TMDB (w500) verificados para la cartelera existente.
UPDATE cine.pelicula AS pelicula
SET cartel_url = 'https://image.tmdb.org/t/p/w500/' || cartel.archivo
FROM (VALUES
    ('Interstellar', 2014, 'd1QKiYtceF3GDtxvTFXFAqwwah9.jpg'),
    ('Origen', 2010, 'tXQvtRWfkUUnWJAn2tN3jERIUG.jpg'),
    ('Parásitos', 2019, '4N55tgxDW0RRATyrZHbx0q9HUKv.jpg'),
    ('Matrix', 1999, '8rT9kG2EYkZpJmYCuTJNnPDEube.jpg'),
    ('Coco', 2017, 'vwsFGblLYxWBNjg9pdWN1Mm5YfW.jpg'),
    ('El rey león', 1994, 'b0MxU37dNmMwKtoPVYPKOZSIrIn.jpg'),
    ('Toy Story', 1995, 'jvn7wy3RSNEXnFSXLpH2of2LcV6.jpg'),
    ('El viaje de Chihiro', 2001, '2RcxjDykOssx4SfqshewyI9vfSl.jpg'),
    ('Joker', 2019, 'v0eQLbzT6sWelfApuYsEkYpzufl.jpg'),
    ('La llegada', 2016, 'tbVITeytclB4JKx2LxxasrCCmZx.jpg'),
    ('Mad Max: Furia en la carretera', 2015, 'fnY0VRsb31A5RLVCBjuR6EFUlo2.jpg'),
    ('Amélie', 2001, 'mYvUib00miJWxWkiXhy3QnrCj96.jpg'),
    ('Regreso al futuro', 1985, 'owk40tn1sFJmC7bhamEpmhdZPKa.jpg'),
    ('Pulp Fiction', 1994, 'hNcQAuquJxTxl2fJFs1R42DrWcf.jpg')
) AS cartel(titulo, anio, archivo)
WHERE pelicula.titulo = cartel.titulo
  AND pelicula.anio = cartel.anio
  AND pelicula.activo
  AND pelicula.cartel_url IS NULL;

COMMIT;
