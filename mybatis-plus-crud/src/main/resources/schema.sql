-- =============================================
-- Schema for mybatis-plus-crud demo
-- =============================================

DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL,
    email       VARCHAR(128),
    age         INT,
    status      INT          DEFAULT 1  COMMENT '1=active, 0=inactive',
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    deleted     INT          DEFAULT 0  COMMENT '0=not deleted, 1=deleted'
);

CREATE INDEX idx_user_username ON t_user(username);
CREATE INDEX idx_user_status ON t_user(status);
CREATE INDEX idx_user_age ON t_user(age);
