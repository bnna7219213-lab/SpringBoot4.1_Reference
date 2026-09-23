-- =============================================
-- Schema for mybatis-plus-generator demo
-- This table is used as input for the code generator
-- =============================================

DROP TABLE IF EXISTS t_product;

CREATE TABLE t_product (
    id           BIGINT          AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(128)    NOT NULL,
    category     VARCHAR(64),
    price        DECIMAL(10, 2)  NOT NULL,
    stock        INT             DEFAULT 0,
    description  VARCHAR(512),
    status       TINYINT         DEFAULT 1   COMMENT '1=online, 0=offline',
    create_time  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    deleted      TINYINT         DEFAULT 0   COMMENT '0=not deleted, 1=deleted'
);
