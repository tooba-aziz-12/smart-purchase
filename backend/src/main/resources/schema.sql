CREATE TABLE products (
                          id BIGINT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          category VARCHAR(255) NOT NULL,
                          price DECIMAL(10,2) NOT NULL,
                          delivery_date DATE NOT NULL
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
                                     quantity INT NOT NULL
);