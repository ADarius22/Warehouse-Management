-- =====================================================================
-- Demo data so the application has something to show on first start
-- =====================================================================

INSERT INTO products (sku, name, description, unit, weight_kg, min_stock_level, created_at, updated_at) VALUES
('BIN-6040',   'Plastic bin 600x400',        'Stackable small-load carrier for shuttle systems', 'PIECE',  1.900,  50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('BIN-4030',   'Plastic bin 400x300',        'Compact small-load carrier',                         'PIECE',  1.100,  40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PAL-EUR',    'Euro pallet 1200x800',       'EPAL wooden pallet',                                 'PIECE', 25.000,  30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('BOX-M',      'Cardboard box M',            'Shipping carton 400x300x200 mm, pack of 25',         'BOX',    4.500,  20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('WRAP-500',   'Stretch wrap roll 500 mm',   'Hand stretch film, 23 micron',                       'PIECE',  2.300,  15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SCAN-2D',    'Handheld 2D barcode scanner','Wireless scanner for picking stations',              'PIECE',  0.350,   5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('LBL-100',    'Thermal labels 100x150',     'Roll of 500 shipping labels',                        'BOX',    1.200,  10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TAPE-48',    'Packing tape 48 mm',         'Pack of 36 rolls',                                   'BOX',    6.000,   8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('GLOVE-L',    'Work gloves size L',         'Pack of 12 pairs',                                   'BOX',    0.900,  10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SENS-PHO',   'Photoelectric sensor',       'Spare sensor for conveyor lines',                    'PIECE',  0.150,  12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO storage_locations (code, zone, aisle, rack, shelf, capacity, active) VALUES
('RCV-01-01', 'RECEIVING', 'RCV', 1, 1, 2000, TRUE),
('A-01-01', 'STORAGE', 'A', 1, 1, 300, TRUE),
('A-01-02', 'STORAGE', 'A', 1, 2, 300, TRUE),
('A-01-03', 'STORAGE', 'A', 1, 3, 300, TRUE),
('A-02-01', 'STORAGE', 'A', 2, 1, 300, TRUE),
('A-02-02', 'STORAGE', 'A', 2, 2, 300, TRUE),
('A-02-03', 'STORAGE', 'A', 2, 3, 300, TRUE),
('B-01-01', 'STORAGE', 'B', 1, 1, 200, TRUE),
('B-01-02', 'STORAGE', 'B', 1, 2, 200, TRUE),
('B-01-03', 'STORAGE', 'B', 1, 3, 200, TRUE),
('B-02-01', 'STORAGE', 'B', 2, 1, 200, TRUE),
('B-02-02', 'STORAGE', 'B', 2, 2, 200, FALSE),
('P-01-01', 'PICKING', 'P', 1, 1, 100, TRUE),
('P-01-02', 'PICKING', 'P', 1, 2, 100, TRUE),
('P-01-03', 'PICKING', 'P', 1, 3, 100, TRUE),
('SHP-01-01', 'SHIPPING', 'SHP', 1, 1, 1000, TRUE);

-- Initial stock: (sku, location code, quantity)
INSERT INTO stock_items (product_id, location_id, quantity, version, updated_at)
SELECT p.id, l.id, s.qty, 0, CURRENT_TIMESTAMP
FROM (VALUES
        ('BIN-6040', 'A-01-01', 180),
        ('BIN-6040', 'P-01-01',  40),
        ('BIN-4030', 'A-01-02', 25),
        ('PAL-EUR',  'A-02-01', 60),
        ('BOX-M',    'A-02-02', 45),
        ('WRAP-500', 'B-01-01', 30),
        ('SCAN-2D',  'P-01-02',  3),
        ('LBL-100',  'B-01-02', 22),
        ('TAPE-48',  'B-01-03', 12),
        ('GLOVE-L',  'P-01-03', 18)
     ) AS s (sku, code, qty)
JOIN products p ON p.sku = s.sku
JOIN storage_locations l ON l.code = s.code;

INSERT INTO stock_movements (product_id, from_location_id, to_location_id, quantity, type, reference, created_at)
SELECT product_id, NULL, location_id, quantity, 'INBOUND', 'INITIAL-LOAD', CURRENT_TIMESTAMP
FROM stock_items;

INSERT INTO customer_orders (order_number, customer_name, status, created_at, updated_at) VALUES
('ORD-001000', 'Logistic Center Arad', 'NEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO order_lines (order_id, product_id, quantity)
SELECT o.id, p.id, l.qty
FROM (VALUES ('BIN-6040', 60), ('BOX-M', 10)) AS l (sku, qty)
JOIN products p ON p.sku = l.sku
CROSS JOIN customer_orders o
WHERE o.order_number = 'ORD-001000';
