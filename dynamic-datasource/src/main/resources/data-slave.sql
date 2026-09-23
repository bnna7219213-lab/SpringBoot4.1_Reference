-- =============================================
-- Initial data for SLAVE datasource
-- Loaded on startup for the slave (read) database
--
-- In production, this data comes from master replication.
-- In demo, we manually insert the same data to simulate fully replicated state.
--
-- Note: spring.sql.init runs only against the primary (master) datasource.
-- For H2 demo, slave needs its own data initialization.
-- =============================================

-- This file is referenced by DataSourceConfig for runtime slave initialization.
-- In a real scenario with MySQL replication, slave data is automatic.

-- Demo behavior:
-- - Master and slave both start with id=1..5 products (from master init + slave sync)
-- - When you POST to create a product, it goes to MASTER
-- - When you GET, it reads from SLAVE (which won't see the new product in real replication)

-- For simple H2 demo purposes, both databases share the same sample data:
-- (Same as data-master.sql)
