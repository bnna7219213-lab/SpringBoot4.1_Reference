-- =============================================
-- Master Database Schema
-- This is the WRITE datasource.
-- All INSERT, UPDATE, DELETE operations go here.
-- =============================================

-- Run on master datasource only
-- (spring.sql.init will run this on the master since it's primary)

DROP TABLE IF EXISTS t_product;

CREATE TABLE t_product (
    id           BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(128)    NOT NULL,
    category     VARCHAR(64),
    price        DECIMAL(10, 2)  NOT NULL,
    stock        INT             DEFAULT 0,
    status       INT             DEFAULT 1   COMMENT '1=online, 0=offline',
    create_time  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_product_category ON t_product(category);
CREATE INDEX idx_product_status   ON t_product(status);
