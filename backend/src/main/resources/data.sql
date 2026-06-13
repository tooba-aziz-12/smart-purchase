INSERT INTO products (id, name, category, price, image_url)
VALUES
    (1, 'Sky Blue Embroidered Lawn Suit', 'Lawn', 7500, '/products/Blue Lawn Suit.png'),
    (2, 'Mint Green Embroidered Lawn Suit', 'Lawn', 6900, '/products/Green Lawn suit.png'),
    (3, 'Ivory Embroidered Lawn', 'Lawn', 8200, '/products/Ivory Embroidered lawn suit.png'),
    (4, 'Black Festive Embroidered Kurta', 'Festive', 9500, '/products/Black Festive Kurta.png'),
    (5, 'Pink Printed Lawn', 'Lawn', 7200, '/products/Pink Printed Lawn.png'),
    (6, 'Maroon Formal Embroidered Suit', 'Formal', 12500, '/products/Maroon Formal Suit.png'),
    (7, 'Mustard Summer Lawn Suit', 'Lawn', 6500, '/products/Mustard Summer Lawn suit.png'),
    (8, 'Navy Blue Casual Kurta', 'Casual', 5800, '/products/Navy Blue Kurta.png'),
    (9, 'White Eid Embroidered Kurta', 'Festive', 11000, '/products/White Eid Collection Kurta.png'),
    (10, 'Olive Everyday Kurta', 'Casual', 5400, '/products/Olive Everyday Wear Kurta.png'),
    (11, 'Lavender Embroidered Lawn', 'Lawn', 8900, '/products/Lavendar Emroidered Lawn.png'),
    (12, 'Beige Luxury Pret Suit', 'Formal', 14500, '/products/Beige Luxury Pret.png');

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