-- =============================================
-- Initial data for MASTER datasource
-- Loaded on startup for the master (write) database
-- =============================================

INSERT INTO t_product (id, name, category, price, stock, status, create_time, update_time) VALUES
(1, 'iPhone 16 Pro', 'Electronics', 9999.00, 100, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'MacBook Pro M4', 'Electronics', 18999.00, 50, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'AirPods Pro 3', 'Electronics', 1999.00, 200, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Nike Air Max', 'Footwear', 899.00, 300, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Lululemon Leggings', 'Apparel', 599.00, 150, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
