-- =============================================
-- Schema for mybatis-plus-join demo
-- 3 tables: Order -> OrderItem -> Product
-- =============================================

-- Table: t_product (product catalog)
DROP TABLE IF EXISTS t_product;
CREATE TABLE t_product (
    id           BIGINT          AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(128)    NOT NULL,
    category     VARCHAR(64),
    price        DECIMAL(10, 2)  NOT NULL,
    stock        INT             DEFAULT 0,
    status       INT             DEFAULT 1   COMMENT '1=online, 0=offline'
);

-- Table: t_order (purchase orders)
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_no      VARCHAR(32)     NOT NULL UNIQUE,
    customer_name VARCHAR(64)     NOT NULL,
    total_amount  DECIMAL(12, 2)  NOT NULL,
    status        INT             DEFAULT 1   COMMENT '1=pending, 2=shipped, 3=completed, 4=cancelled',
    create_time   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- Table: t_order_item (line items within orders)
DROP TABLE IF EXISTS t_order_item;
CREATE TABLE t_order_item (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT          NOT NULL,
    product_id  BIGINT          NOT NULL,
    quantity    INT             NOT NULL,
    unit_price  DECIMAL(10, 2)  NOT NULL,
    subtotal    DECIMAL(12, 2)  NOT NULL
);

-- Indexes for join performance
CREATE INDEX idx_order_item_order_id   ON t_order_item(order_id);
CREATE INDEX idx_order_item_product_id ON t_order_item(product_id);
CREATE INDEX idx_order_customer        ON t_order(customer_name);
CREATE INDEX idx_order_status          ON t_order(status);
