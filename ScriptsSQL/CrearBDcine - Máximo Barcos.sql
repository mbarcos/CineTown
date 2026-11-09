CREATE TYPE clasificacion_edad AS ENUM ('TP', 'P7', 'P12', 'P16', 'P18');
CREATE TYPE rol_usuario AS ENUM ('admin', 'taquilla', 'cliente');

CREATE TABLE sala (
    id SERIAL PRIMARY KEY,
    numero INT UNIQUE NOT NULL,
    capacidad INT NOT NULL CHECK (capacidad > 0)
);

CREATE TABLE pelicula (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    sinopsis TEXT,
    duracion INT NOT NULL CHECK (duracion > 0),
    anio INT NOT NULL,
    clasificacion clasificacion_edad NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE genero (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE pelicula_genero (
    id SERIAL PRIMARY KEY,
    pelicula_id INT NOT NULL REFERENCES pelicula(id) ON DELETE CASCADE,
    genero_id INT NOT NULL REFERENCES genero(id) ON DELETE CASCADE,
    UNIQUE (pelicula_id, genero_id)
);

CREATE TABLE sesion (
    id SERIAL PRIMARY KEY,
    pelicula_id INT NOT NULL REFERENCES pelicula(id),
    sala_id INT NOT NULL REFERENCES sala(id),
    fecha_hora_inicio TIMESTAMP NOT NULL,
    fecha_hora_fin TIMESTAMP NOT NULL,
    precio_base DECIMAL(10, 2) NOT NULL CHECK (precio_base >= 0),
    CHECK (fecha_hora_fin > fecha_hora_inicio)
);

CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nombre_usuario VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol rol_usuario NOT NULL
);

CREATE TABLE compra (
    id SERIAL PRIMARY KEY,
    sesion_id INT NOT NULL REFERENCES sesion(id),
    usuario_id INT REFERENCES usuario(id), 
    email VARCHAR(255) NOT NULL,
    num_entradas INT NOT NULL CHECK (num_entradas > 0 AND num_entradas <= 10),
    cod_compra CHAR(8) UNIQUE NOT NULL,
    fecha_compra TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    precio_total DECIMAL(10, 2) NOT NULL CHECK (precio_total >= 0),
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    parking_usado BOOLEAN NOT NULL DEFAULT FALSE
);
