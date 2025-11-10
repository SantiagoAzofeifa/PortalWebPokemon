

CREATE DATABASE IF NOT EXISTS portal_ventas
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE portal_ventas;

-- =========================================================
-- Tabla: users
-- =========================================================
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     username VARCHAR(60) NOT NULL UNIQUE,
                                     password VARCHAR(200) NOT NULL,
                                     role VARCHAR(20) NOT NULL DEFAULT 'USER',
                                     active TINYINT(1) NOT NULL DEFAULT 1,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    -- CHECK (role IN ('ADMIN','USER'))
) ENGINE=InnoDB;

CREATE INDEX idx_users_active ON users(active);

-- =========================================================
-- Tabla: audits (auditoría de login/logout)
-- =========================================================
CREATE TABLE IF NOT EXISTS audits (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      user_id BIGINT,
                                      username_snapshot VARCHAR(60),
                                      action VARCHAR(20) NOT NULL,             -- LOGIN / LOGOUT / RENEW / REGISTER
                                      timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      CONSTRAINT fk_audits_user FOREIGN KEY (user_id) REFERENCES users(id)
                                          ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_audits_user ON audits(user_id);
CREATE INDEX idx_audits_action ON audits(action);
CREATE INDEX idx_audits_timestamp ON audits(timestamp);

-- =========================================================
-- Tabla: products (productos internos opcionales)
-- =========================================================
CREATE TABLE IF NOT EXISTS products (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        name VARCHAR(120) NOT NULL,
                                        description TEXT,
                                        category VARCHAR(40) NOT NULL,           -- POKEMON / SPECIES / ITEMS / GENERATIONS (interno)
                                        price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                                        image_url VARCHAR(300),
                                        country_of_origin VARCHAR(120),
                                        available_countries_csv VARCHAR(400),
                                        banned_countries_csv VARCHAR(400),
                                        active TINYINT(1) NOT NULL DEFAULT 1,
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_price ON products(price);

-- =========================================================
-- Tabla: pokemon_rules (reglas específicas para Pokémon dinámicos)
-- =========================================================
CREATE TABLE IF NOT EXISTS pokemon_rules (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                             pokemon_id BIGINT NOT NULL UNIQUE,
                                             origin_country VARCHAR(120),
                                             available_countries_csv VARCHAR(255),
                                             banned_countries_csv VARCHAR(255),
                                             notes VARCHAR(500)
) ENGINE=InnoDB;

-- =========================================================
-- Tabla: carts (un carrito por usuario)
-- =========================================================
CREATE TABLE IF NOT EXISTS carts (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     user_id BIGINT NOT NULL UNIQUE,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id)
                                         ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- Tabla: cart_items (ítems del carrito)
-- product_id: id dinámico (Pokémon / Item / Game).
-- product_category: POKEMON | ITEM | GAME
-- =========================================================
CREATE TABLE IF NOT EXISTS cart_items (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          cart_id BIGINT NOT NULL,
                                          product_id BIGINT NOT NULL,
                                          product_category VARCHAR(20) NOT NULL DEFAULT 'POKEMON',
                                          quantity INT NOT NULL DEFAULT 1,
                                          unit_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                                          CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id)
                                              ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_prod_cat ON cart_items(product_category);
CREATE INDEX idx_cart_items_pid ON cart_items(product_id);

-- =========================================================
-- Tabla: orders (encabezado)
-- =========================================================
CREATE TABLE IF NOT EXISTS orders (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      user_id BIGINT NOT NULL,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
                                      customer_name VARCHAR(120),
                                      customer_email VARCHAR(120),
                                      customer_phone VARCHAR(40),
                                      address_line1 VARCHAR(200),
                                      address_line2 VARCHAR(200),
                                      country VARCHAR(120),
                                      region VARCHAR(120),
                                      CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
                                          ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at);

-- =========================================================
-- Tabla: order_items (detalles)
-- =========================================================
CREATE TABLE IF NOT EXISTS order_items (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           order_id BIGINT NOT NULL,
                                           product_id BIGINT NOT NULL,
                                           product_category VARCHAR(20) NOT NULL DEFAULT 'POKEMON',
                                           quantity INT NOT NULL DEFAULT 1,
                                           unit_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                                           CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)
                                               ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_prod_cat ON order_items(product_category);
CREATE INDEX idx_order_items_pid ON order_items(product_id);

-- =========================================================
-- Tabla: warehouse (etapa almacén)
-- =========================================================
CREATE TABLE IF NOT EXISTS warehouse (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         order_id BIGINT NOT NULL UNIQUE,
                                         in_date TIMESTAMP NULL,
                                         out_date TIMESTAMP NULL,
                                         stock_checked TINYINT(1) NOT NULL DEFAULT 0,
                                         stock_qty INT NULL,
                                         location VARCHAR(200),
                                         origin_country VARCHAR(120),
                                         notes VARCHAR(500),
                                         CONSTRAINT fk_warehouse_order FOREIGN KEY (order_id) REFERENCES orders(id)
                                             ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- Tabla: packaging (etapa empaque)
-- =========================================================
CREATE TABLE IF NOT EXISTS packaging (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         order_id BIGINT NOT NULL UNIQUE,
                                         size VARCHAR(20),        -- GRANDE / MEDIANO / PEQUEÑO
                                         type VARCHAR(30),        -- LIBRE / AL_VACIO / RELLENO
                                         materials VARCHAR(300),
                                         fragile TINYINT(1) NOT NULL DEFAULT 0,
                                         notes VARCHAR(500),
                                         CONSTRAINT fk_packaging_order FOREIGN KEY (order_id) REFERENCES orders(id)
                                             ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- Tabla: delivery (etapa entrega)
-- =========================================================
CREATE TABLE IF NOT EXISTS delivery (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        order_id BIGINT NOT NULL UNIQUE,
                                        method VARCHAR(30),          -- CORREO / MENSAJERO / CASILLERO
                                        address VARCHAR(300),
                                        scheduled_date TIMESTAMP NULL,
                                        tracking_code VARCHAR(120),
                                        notes VARCHAR(500),
                                        CONSTRAINT fk_delivery_order FOREIGN KEY (order_id) REFERENCES orders(id)
                                            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- Tabla: payment (etapa pago)
-- =========================================================
CREATE TABLE IF NOT EXISTS payment (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       order_id BIGINT NOT NULL UNIQUE,
                                       currency VARCHAR(10),            -- USD, CRC, etc.
                                       item_count INT NOT NULL DEFAULT 0,
                                       gross_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                                       net_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                                       method VARCHAR(40),              -- TARJETA / PAYPAL / etc.
                                       paid_at TIMESTAMP NULL,
                                       notes VARCHAR(500),
                                       CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
                                           ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;


-- =========================================================
-- Vistas útiles (opcionales)
-- =========================================================
CREATE OR REPLACE VIEW v_order_totals AS
SELECT o.id AS order_id,
       o.user_id,
       o.status,
       o.created_at,
       COALESCE(SUM(i.quantity * i.unit_price),0) AS total_amount
FROM orders o
         LEFT JOIN order_items i ON i.order_id = o.id
GROUP BY o.id, o.user_id, o.status, o.created_at;
