INSERT INTO products (id, name, category, price, delivery_date)
VALUES
    (1, 'Blue Lawn Suit', 'Lawn', 7500, '2026-06-18'),
    (2, 'Green Lawn Suit', 'Lawn', 6900, '2026-06-17'),
    (3, 'Ivory Embroidered Lawn', 'Lawn', 8200, '2026-06-19'),
    (4, 'Black Festive Kurta', 'Festive', 9500, '2026-06-20'),
    (5, 'Pink Printed Lawn', 'Lawn', 7200, '2026-06-18'),
    (6, 'Maroon Formal Suit', 'Formal', 12500, '2026-06-22'),
    (7, 'Mustard Summer Lawn', 'Lawn', 6500, '2026-06-17'),
    (8, 'Navy Blue Kurta', 'Casual', 5800, '2026-06-16'),
    (9, 'White Eid Collection', 'Festive', 11000, '2026-06-21'),
    (10, 'Olive Everyday Wear', 'Casual', 5400, '2026-06-16'),
    (11, 'Lavender Embroidered Lawn', 'Lawn', 8900, '2026-06-20'),
    (12, 'Beige Luxury Pret', 'Formal', 14500, '2026-06-23');

INSERT INTO warehouses (id, city)
VALUES
    (1, 'Karachi'),
    (2, 'Lahore'),
    (3, 'Islamabad');

INSERT INTO warehouse_inventory (
    id,
    warehouse_id,
    product_id,
    size,
    quantity
)
VALUES
    (1, 1, 1, 'M', 3),
    (2, 1, 2, 'M', 5),
    (3, 2, 1, 'L', 2),
    (4, 2, 3, 'M', 4),
    (5, 3, 4, 'L', 1),
    (6, 3, 5, 'S', 7),

    (7, 1, 6, 'L', 4),
    (8, 2, 7, 'M', 8),
    (9, 3, 8, 'S', 6),

    (10, 1, 9, 'L', 3),
    (11, 2, 10, 'M', 9),
    (12, 3, 11, 'L', 5),

    (13, 1, 12, 'M', 2);