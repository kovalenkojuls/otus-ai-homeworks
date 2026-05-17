-- liquibase formatted sql
-- changeset petshelter:1
CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(20) NOT NULL,
    breed VARCHAR(100),
    age INT,
    color VARCHAR(50),
    weight_kg DECIMAL(5,2),
    arrival_date DATE NOT NULL,
    health_status VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- changeset petshelter:2
CREATE INDEX idx_pets_status ON pets(status);
CREATE INDEX idx_pets_species ON pets(species);
CREATE INDEX idx_pets_arrival_date ON pets(arrival_date);

-- changeset petshelter:3
INSERT INTO pets (name, species, breed, age, color, weight_kg, arrival_date, health_status, description, status, created_at, updated_at)
VALUES
    ('Барсик', 'CAT', 'шотландский вислоухий', 2, 'серый', 4.5, '2026-05-15', 'HEALTHY', 'Ласковый, любит играть с мячиком', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Рекс', 'DOG', 'немецкая овчарка', 5, 'черный с подпалинами', 28.0, '2026-04-10', 'HEALTHY', 'Собака-поводырь, дрессирована', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Кеша', 'BIRD', 'акула', 3, 'зеленый', 0.3, '2026-03-22', 'RECOVERING', 'Оправился от травмы крыла', 'FOSTERED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);