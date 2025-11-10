-- ======== ACCESS_LOG ========
SELECT * FROM access_log;

INSERT INTO access_log (at, ip, type, user_id)
VALUES (NOW(), '127.0.0.1', 'LOGIN', 1);

UPDATE access_log SET ip = '192.168.0.1' WHERE id = 1;

DELETE FROM access_log WHERE id = 1;

DROP TABLE IF EXISTS access_log;

-- ======== CART_ITEMS ========
SELECT * FROM cart_items;

INSERT INTO cart_items (cart_id, product_id, quantity, unit_price, product_category)
VALUES (1, 1, 3, 1500, 'POKEMON');

UPDATE cart_items SET quantity = 2 WHERE id = 1;

DELETE FROM cart_items WHERE id = 1;

DROP TABLE IF EXISTS cart_items;

-- ======== CARTS ========
SELECT * FROM carts;

INSERT INTO carts (user_id) VALUES (1);

UPDATE carts SET user_id = 2 WHERE id = 1;

DELETE FROM carts WHERE id = 1;

DROP TABLE IF EXISTS carts;

-- ======== DELIVERY ========
SELECT * FROM delivery;

INSERT INTO delivery (order_id, method, address, scheduled_date, tracking_code, notes)
VALUES (1, 'Correo', 'Calle Falsa 123', '2025-11-10', 'TRK1234', 'Entrega urgente');

UPDATE delivery SET tracking_code = 'TRK9999' WHERE id = 1;

DELETE FROM delivery WHERE id = 1;

DROP TABLE IF EXISTS delivery;

-- ======== LOGIN_AUDIT ========
SELECT * FROM login_audit;

INSERT INTO login_audit (user_id, username, action)
VALUES (1, 'admin', 'LOGIN');

UPDATE login_audit SET action = 'LOGOUT' WHERE id = 1;

DELETE FROM login_audit WHERE id = 1;

DROP TABLE IF EXISTS login_audit;

-- ======== ORDER_ITEMS ========
SELECT * FROM order_items;

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES (1, 1, 2, 3000);

UPDATE order_items SET quantity = 3 WHERE id = 1;

DELETE FROM order_items WHERE id = 1;

DROP TABLE IF EXISTS order_items;

-- ======== ORDERS ========
SELECT * FROM orders;

INSERT INTO orders (user_id, customer_name, customer_email, customer_phone, status)
VALUES (1, 'Ash Ketchum', 'ash@pallet.town', '8888-9999', 'CREADO');

UPDATE orders SET status = 'ENVIADO' WHERE id = 1;

DELETE FROM orders WHERE id = 1;

DROP TABLE IF EXISTS orders;

-- ======== PACKAGING ========
SELECT * FROM packaging;

INSERT INTO packaging (order_id, size, type, materials, fragile, notes)
VALUES (1, 'M', 'Caja', 'Cartón', 1, 'Manipular con cuidado');

UPDATE packaging SET size = 'L' WHERE id = 1;

DELETE FROM packaging WHERE id = 1;

DROP TABLE IF EXISTS packaging;


-- ======== PAYMENT ========
SELECT * FROM payment;

INSERT INTO payment (order_id, currency, item_count, gross_amount, net_amount, method)
VALUES (1, 'CRC', 3, 4500.00, 4300.00, 'Tarjeta');

UPDATE payment SET method = 'Efectivo' WHERE id = 1;

DELETE FROM payment WHERE id = 1;

DROP TABLE IF EXISTS payment;

-- ======== POKEMON_RULES ========
SELECT * FROM pokemon_rules;

INSERT INTO pokemon_rules (pokemon_id, origin_country, available_countries_csv, banned_countries_csv, notes)
VALUES (1, 'Japón', 'CR,MX,CO', 'RU,US', 'Distribución especial');

UPDATE pokemon_rules SET notes = 'Evento 2025' WHERE id = 1;

DELETE FROM pokemon_rules WHERE id = 1;

DROP TABLE IF EXISTS pokemon_rules;

-- ======== PRODUCTS ========
SELECT * FROM products;

INSERT INTO products (category, name, image_url, price, description)
VALUES ('POKEMON', 'Charmander', 'https://img.pokemondb.net/artwork/charmander.jpg', 1500, 'Producto de colección');

UPDATE products SET price = 2000 WHERE id = 1;

DELETE FROM products WHERE id = 1;

DROP TABLE IF EXISTS products;

-- ======== SESSION_CONFIG ========
SELECT * FROM session_config;

INSERT INTO session_config (timeout_seconds) VALUES (3600);

UPDATE session_config SET timeout_seconds = 7200 WHERE id = 1;

DELETE FROM session_config WHERE id = 1;

DROP TABLE IF EXISTS session_config;

-- ======== WAREHOUSE ========
SELECT * FROM warehouse;

INSERT INTO warehouse (order_id, in_date, stock_qty, location, origin_country)
VALUES (1, NOW(), 50, 'Almacén Central', 'Japón');

UPDATE warehouse SET stock_qty = 45 WHERE id = 1;

DELETE FROM warehouse WHERE id = 1;

DROP TABLE IF EXISTS warehouse;

CREATE DATABASE IF NOT EXISTS portal_ventas
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE portal_ventas;
