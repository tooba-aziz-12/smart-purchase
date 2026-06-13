DROP TABLE IF EXISTS warehouse_inventory;
DROP TABLE IF EXISTS warehouses;
DROP TABLE IF EXISTS products;

CREATE TABLE products (
                          id BIGINT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          category VARCHAR(255) NOT NULL,
                          price DECIMAL(10,2) NOT NULL,
                          image_url VARCHAR(500) NOT NULL
);

CREATE TABLE warehouses (
                            id BIGINT PRIMARY KEY,
                            city VARCHAR(100) NOT NULL
);

CREATE TABLE warehouse_inventory (
                                     id BIGINT PRIMARY KEY,
                                     warehouse_id BIGINT NOT NULL,
                                     product_id BIGINT NOT NULL,
                                     size VARCHAR(10) NOT NULL,
                                     quantity INT NOT NULL,
                                     CONSTRAINT fk_warehouse_inventory_warehouse
                                         FOREIGN KEY (warehouse_id)
                                         REFERENCES warehouses(id),
                                     CONSTRAINT fk_warehouse_inventory_product
                                         FOREIGN KEY (product_id)
                                         REFERENCES products(id),
                                     CONSTRAINT uq_warehouse_inventory_product_size
                                         UNIQUE (warehouse_id, product_id, size),
                                     CONSTRAINT chk_warehouse_inventory_quantity
                                         CHECK (quantity >= 0)
);