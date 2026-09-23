-- =============================================
-- Slave Database Schema
-- This is the READ datasource.
-- All SELECT operations go here for read-write splitting.
--
-- In production, this schema is replicated from master.
-- In demo, we use H2 file-based DB to simulate two databases.
-- =============================================

-- Note: In production with real replication, schema is mirrored automatically.
-- For H2 demo, we manually ensure the same schema exists.

-- This file should be loaded on the slave datasource.
-- Use spring.sql.init for master, and separate initialization for slave if needed.
-- In simple H2 demo, both master and slave start with the same data from data scripts.

-- Schema is identical to master:
-- CREATE TABLE t_product (same columns...)

-- The demo relies on the following approach:
-- 1. Master receives writes (via @DS("master"))
-- 2. Slave receives reads (via @DS("slave"))
-- 3. Both databases share the same initial data from data scripts
