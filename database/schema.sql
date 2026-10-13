CREATE DATABASE IF NOT EXISTS concesionario
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE concesionario;

CREATE TABLE IF NOT EXISTS coches (
    matricula VARCHAR(7) NOT NULL,
    marca VARCHAR(40) NOT NULL,
    modelo VARCHAR(60) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    extras VARCHAR(160) NOT NULL,
    PRIMARY KEY (matricula)
);

INSERT IGNORE INTO coches (matricula, marca, modelo, precio, extras) VALUES
    ('1234ABC', 'Opel', 'Astra', 22500.00, 'Hands-free system'),
    ('2356CBA', 'Peugeot', '208', 22000.00, 'No extras'),
    ('4854YTG', 'Ford', 'Focus', 18750.00, 'Parking sensors'),
    ('9876ZYX', 'Ford', 'Fiesta', 15950.00, 'ABS');
