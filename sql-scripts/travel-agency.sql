CREATE DATABASE IF NOT EXISTS travel_agency;
USE travel_agency;

CREATE TABLE IF NOT EXISTS users (
    id              BINARY(16)                          NOT NULL,
    username        VARCHAR(50)                         NOT NULL,
    first_name      VARCHAR(50)                         NOT NULL,
    last_name       VARCHAR(50)                         NOT NULL,
    email           VARCHAR(255)                        NOT NULL,
    password        VARCHAR(255)                        NOT NULL,
    role            ENUM('ADMIN','MANAGER','CUSTOMER')  NOT NULL DEFAULT 'CUSTOMER',
    phone_number    VARCHAR(20),
    balance         DECIMAL(10,2)                       NOT NULL DEFAULT 0.00,
    active          BOOLEAN                              NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_users PRIMARY KEY (id),
    CONSTRAINT UQ_users_username UNIQUE (username),
    CONSTRAINT UQ_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS vouchers (
    id              BINARY(16)                                           NOT NULL,
    title           VARCHAR(255)                                         NOT NULL,
    description     VARCHAR(1000)                                        NOT NULL,
    price           DOUBLE                                               NOT NULL,
    tour_type       ENUM('HEALTH','SPORTS','LEISURE','SAFARI','WINE','ECO','ADVENTURE','CULTURAL') NOT NULL,
    transfer_type   ENUM('BUS','TRAIN','PLANE','SHIP','PRIVATE_CAR','JEEPS','MINIBUS','ELECTRICAL_CARS') NOT NULL,
    hotel_type      ENUM('ONE_STAR','TWO_STARS','THREE_STARS','FOUR_STARS','FIVE_STARS') NOT NULL,
    status          ENUM('REGISTERED','PAID','CANCELED')                 NOT NULL DEFAULT 'REGISTERED',
    arrival_date    DATE                                                NOT NULL,
    eviction_date   DATE                                                NOT NULL,
    user_id         BINARY(16),
    is_hot          BOOLEAN                                             NOT NULL DEFAULT FALSE,
    CONSTRAINT PK_vouchers PRIMARY KEY (id),
    CONSTRAINT FK_vouchers_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- encoded password is: 12345
INSERT INTO users (id, username, first_name, last_name, email, password, role, phone_number, balance, active) VALUES
(UUID_TO_BIN(UUID()), 'admin', 'Liam', 'Hawthorne', 'admin@epam.com', '$2a$10$Q.0b1Vz1WrMLXQPCrCDvVOd5FSuyVIqfPTaxhyfa/OK2NwBC5THeC', 'ADMIN', '+380671234567', 10000.00, TRUE),
(UUID_TO_BIN(UUID()), 'manager','Zara', 'Montague', 'manager@epam.com', '$2a$10$Q.0b1Vz1WrMLXQPCrCDvVOd5FSuyVIqfPTaxhyfa/OK2NwBC5THeC', 'MANAGER', '+380509876543', 5000.00, TRUE),
(UUID_TO_BIN(UUID()), 'user1', 'Ethan', 'Calder', 'user1@epam.com', '$2a$10$Q.0b1Vz1WrMLXQPCrCDvVOd5FSuyVIqfPTaxhyfa/OK2NwBC5THeC', 'CUSTOMER', '+380684321987', 1500.00, TRUE);

INSERT INTO vouchers (
    id, title, description, price, tour_type, transfer_type,
    hotel_type, status, arrival_date, eviction_date, user_id, is_hot
) VALUES
(UUID_TO_BIN(UUID()), 'Safari Adventure', 'Explore wildlife in Africa.', 2500.00, 'SAFARI', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2026-06-10', '2026-06-20', NULL, TRUE),
(UUID_TO_BIN(UUID()), 'Wine Tour France', 'Taste wines in Bordeaux.', 1200.00, 'WINE', 'TRAIN', 'THREE_STARS', 'REGISTERED', '2026-05-01', '2026-05-07', NULL, FALSE),
(UUID_TO_BIN(UUID()), 'Cultural Tour Italy', 'Visit historic cities.', 1500.00, 'CULTURAL', 'PLANE', 'FIVE_STARS', 'REGISTERED', '2026-07-15', '2026-07-25', NULL, FALSE),
(UUID_TO_BIN(UUID()), 'Adventure in Peru', 'Hike the Andes mountains.', 2000.00, 'ADVENTURE', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2026-08-10', '2026-08-20', NULL, TRUE),
(UUID_TO_BIN(UUID()), 'Eco Tour Costa Rica', 'Rainforest and wildlife.', 1800.00, 'ECO', 'BUS', 'THREE_STARS', 'REGISTERED', '2026-09-05', '2026-09-15', NULL, FALSE),
(UUID_TO_BIN(UUID()), 'Health Retreat Thailand', 'Relax with spa and yoga.', 1400.00, 'HEALTH', 'PLANE', 'FIVE_STARS', 'REGISTERED', '2026-03-10', '2026-03-20', NULL, TRUE),
(UUID_TO_BIN(UUID()), 'Cultural Tour Greece', 'Ancient ruins and islands.', 1600.00, 'CULTURAL', 'SHIP', 'THREE_STARS', 'REGISTERED', '2026-06-20', '2026-06-30', NULL, FALSE),
(UUID_TO_BIN(UUID()), 'Adventure in New Zealand', 'Bungee and hiking.', 2200.00, 'ADVENTURE', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2026-09-10', '2026-09-25', NULL, TRUE),
(UUID_TO_BIN(UUID()), 'Eco Tour Amazon', 'Jungle exploration.', 1900.00, 'ECO', 'JEEPS', 'THREE_STARS', 'REGISTERED', '2026-08-01', '2026-08-10', NULL, FALSE),
(UUID_TO_BIN(UUID()), 'Health Spa Hungary', 'Thermal baths and wellness.', 1250.00, 'HEALTH', 'TRAIN', 'THREE_STARS', 'REGISTERED', '2026-03-20', '2026-03-30', NULL, FALSE),
(UUID_TO_BIN(UUID()), 'Sports Camp USA', 'Surfing and beach sports.', 2100.00, 'SPORTS', 'PLANE', 'FOUR_STARS', 'REGISTERED', '2026-06-05', '2026-06-15', NULL, TRUE),
(UUID_TO_BIN(UUID()), 'Beach Escape', 'Relax on quiet beaches.', 800.00, 'LEISURE', 'BUS', 'ONE_STAR', 'REGISTERED', '2026-07-01', '2026-07-07', NULL, FALSE),
(UUID_TO_BIN(UUID()), 'Jungle Trek', 'Explore remote jungles.', 950.00, 'ADVENTURE', 'JEEPS', 'TWO_STARS', 'REGISTERED', '2026-08-05', '2026-08-15', NULL, TRUE),
(UUID_TO_BIN(UUID()), 'City Break', 'Short city getaway.', 600.00, 'CULTURAL', 'TRAIN', 'TWO_STARS', 'REGISTERED', '2026-09-10', '2026-09-12', NULL, FALSE),
(UUID_TO_BIN(UUID()), 'Mountain Hike', 'Climb scenic mountains.', 700.00, 'SPORTS', 'MINIBUS', 'ONE_STAR', 'REGISTERED', '2026-10-01', '2026-10-07', NULL, TRUE),
(UUID_TO_BIN(UUID()), 'Wine Escape', 'Taste local wines.', 1100.00, 'WINE', 'PRIVATE_CAR', 'TWO_STARS', 'REGISTERED', '2026-11-05', '2026-11-10', NULL, FALSE);
