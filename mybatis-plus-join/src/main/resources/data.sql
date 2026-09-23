-- =============================================
-- Sample data for mybatis-plus-join demo
-- =============================================

-- Products
INSERT INTO t_product (id, product_name, category, price, stock, status) VALUES
(1, 'iPhone 16 Pro', 'Electronics', 9999.00, 100, 1),
(2, 'MacBook Pro M4', 'Electronics', 18999.00, 50, 1),
(3, 'AirPods Pro 3', 'Electronics', 1999.00, 200, 1),
(4, 'Nike Air Max', 'Footwear', 899.00, 300, 1),
(5, 'Lululemon Leggings', 'Apparel', 599.00, 150, 1),
(6, 'Dyson V15 Vacuum', 'Home', 4999.00, 80, 1),
(7, 'Kindle Paperwhite', 'Electronics', 1099.00, 120, 1);

-- Orders
INSERT INTO t_order (id, order_no, customer_name, total_amount, status, create_time) VALUES
(1, 'ORD-2024-001', 'Zhang Wei', 11998.00, 3, '2024-09-01 10:00:00'),
(2, 'ORD-2024-002', 'Li Ming', 18999.00, 2, '2024-09-02 14:30:00'),
(3, 'ORD-2024-003', 'Wang Fang', 899.00, 1, '2024-09-03 09:15:00'),
(4, 'ORD-2024-004', 'Zhang Wei', 5998.00, 3, '2024-09-05 16:45:00'),
(5, 'ORD-2024-005', 'Chen Hui', 1999.00, 1, '2024-09-10 11:20:00');

-- Order Items
INSERT INTO t_order_item (order_id, product_id, quantity, unit_price, subtotal) VALUES
-- Order 1: iPhone 16 Pro + AirPods Pro 3
(1, 1, 1, 9999.00, 9999.00),
(1, 3, 1, 1999.00, 1999.00),
-- Order 2: MacBook Pro M4
(2, 2, 1, 18999.00, 18999.00),
-- Order 3: Nike Air Max
(3, 4, 1, 899.00, 899.00),
-- Order 4: AirPods Pro 3 + Kindle Paperwhite
(4, 3, 2, 1999.00, 3998.00),
(4, 7, 2, 1000.00, 2000.00),
-- Order 5: AirPods Pro 3
(5, 3, 1, 1999.00, 1999.00);
