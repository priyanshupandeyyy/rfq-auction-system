-- ============================================================
-- RFQ Auction System — MySQL Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS rfq_auction;
USE rfq_auction;

-- ----------------------------------------------------------------
-- Users Table
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(150) NOT NULL UNIQUE,
    role        ENUM('BUYER', 'SUPPLIER') NOT NULL DEFAULT 'SUPPLIER',
    company     VARCHAR(200),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- Auctions Table  (one auction = one RFQ event)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auctions (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    rfq_number              VARCHAR(50)  NOT NULL UNIQUE,
    title                   VARCHAR(300) NOT NULL,
    description             TEXT,
    created_by              BIGINT NOT NULL,
    status                  ENUM('ACTIVE', 'CLOSED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',

    -- Timing
    start_time              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scheduled_close_time    DATETIME     NOT NULL,           -- may be extended
    hard_close_time         DATETIME     NOT NULL,           -- NEVER extended

    -- Bid Extension Configuration
    extension_type          ENUM('TIME_BASED', 'RANK_BASED', 'COMBINED') NOT NULL DEFAULT 'TIME_BASED',
    extension_trigger_mins  INT          NOT NULL DEFAULT 5,  -- bid within X mins triggers
    extension_duration_mins INT          NOT NULL DEFAULT 5,  -- how many mins to add

    -- Metadata
    item_name               VARCHAR(300),
    quantity                DECIMAL(15,3),
    unit                    VARCHAR(50),
    base_price              DECIMAL(15,2),
    currency                VARCHAR(10)  NOT NULL DEFAULT 'INR',

    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ----------------------------------------------------------------
-- Bids Table
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bids (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    auction_id      BIGINT          NOT NULL,
    supplier_id     BIGINT          NOT NULL,
    bid_amount      DECIMAL(15,2)   NOT NULL,
    remarks         VARCHAR(500),
    bid_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_latest       BOOLEAN         NOT NULL DEFAULT TRUE,   -- only latest bid per supplier is ranked
    rank_at_submit  INT,                                     -- captured rank when bid was placed

    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (auction_id)  REFERENCES auctions(id),
    FOREIGN KEY (supplier_id) REFERENCES users(id),
    INDEX idx_bid_auction (auction_id),
    INDEX idx_bid_supplier (supplier_id, auction_id)
);

-- ----------------------------------------------------------------
-- Activity Logs Table
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS activity_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    auction_id  BIGINT       NOT NULL,
    event_type  VARCHAR(100) NOT NULL,   -- BID_SUBMITTED, AUCTION_EXTENDED, AUCTION_CLOSED, etc.
    description TEXT,
    actor_id    BIGINT,                  -- user who triggered the event (nullable for system events)
    metadata    JSON,                    -- flexible extra data (old deadline, new deadline, etc.)
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    INDEX idx_log_auction (auction_id),
    INDEX idx_log_time (created_at)
);

-- ================================================================
-- Seed Data
-- ================================================================

-- Users
INSERT INTO users (username, email, role, company) VALUES
  ('admin_buyer',    'buyer@rfqauction.com',    'BUYER',    'Acme Corp'),
  ('supplier_alpha', 'alpha@suppliers.com',     'SUPPLIER', 'Alpha Supplies Ltd'),
  ('supplier_beta',  'beta@suppliers.com',      'SUPPLIER', 'Beta Trading Co'),
  ('supplier_gamma', 'gamma@suppliers.com',     'SUPPLIER', 'Gamma Logistics'),
  ('supplier_delta', 'delta@suppliers.com',     'SUPPLIER', 'Delta Enterprises');

-- Active Auction (closes 2 hours from now, hard close 4 hours from now)
INSERT INTO auctions (
    rfq_number, title, description, created_by, status,
    start_time, scheduled_close_time, hard_close_time,
    extension_type, extension_trigger_mins, extension_duration_mins,
    item_name, quantity, unit, base_price, currency
) VALUES (
    'RFQ-2026-001',
    'Steel Pipes — Grade A — 500 Units',
    'Procurement of Grade-A seamless steel pipes for the Q3 infrastructure project.',
    1, 'ACTIVE',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 2 HOUR),
    DATE_ADD(NOW(), INTERVAL 4 HOUR),
    'COMBINED', 5, 5,
    'Steel Pipe Grade-A 6 inch', 500, 'Units', 12000.00, 'INR'
),
(
    'RFQ-2026-002',
    'Industrial Lubricants — 200 Litres',
    'High-viscosity industrial lubricants for manufacturing plant maintenance.',
    1, 'ACTIVE',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 3 HOUR),
    DATE_ADD(NOW(), INTERVAL 6 HOUR),
    'TIME_BASED', 10, 10,
    'Industrial Lubricant SAE 40', 200, 'Litres', 850.00, 'INR'
),
(
    'RFQ-2026-003',
    'Electrical Cables — 1000 Metres',
    'Armoured electrical cables for the factory expansion project.',
    1, 'CLOSED',
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_SUB(NOW(), INTERVAL 20 HOUR),
    'RANK_BASED', 5, 5,
    'Armoured Cable 4-core 16mm', 1000, 'Metres', 320.00, 'INR'
);

-- Sample Bids for RFQ-001
INSERT INTO bids (auction_id, supplier_id, bid_amount, remarks, is_latest, rank_at_submit) VALUES
  (1, 2, 11500.00, 'Best quality guaranteed', TRUE,  1),
  (1, 3, 11800.00, 'Bulk discount applied',   TRUE,  2),
  (1, 4, 12000.00, 'On-time delivery assured', TRUE, 3);

-- Sample Bids for RFQ-002
INSERT INTO bids (auction_id, supplier_id, bid_amount, remarks, is_latest, rank_at_submit) VALUES
  (2, 2, 820.00, 'Premium brand',    TRUE, 1),
  (2, 5, 830.00, 'Local supplier',   TRUE, 2);

-- Sample Bids for Closed RFQ-003
INSERT INTO bids (auction_id, supplier_id, bid_amount, remarks, is_latest, rank_at_submit) VALUES
  (3, 3, 300.00, 'ISO certified',           TRUE, 1),
  (3, 4, 315.00, 'Fast delivery',           TRUE, 2),
  (3, 5, 318.00, 'Competitive pricing',     TRUE, 3);

-- Activity Logs
INSERT INTO activity_logs (auction_id, event_type, description, actor_id) VALUES
  (1, 'AUCTION_CREATED',  'RFQ-2026-001 created by admin_buyer', 1),
  (1, 'BID_SUBMITTED',    'Supplier Alpha submitted bid at ₹11,500', 2),
  (1, 'BID_SUBMITTED',    'Supplier Beta submitted bid at ₹11,800',  3),
  (1, 'BID_SUBMITTED',    'Supplier Gamma submitted bid at ₹12,000', 4),
  (2, 'AUCTION_CREATED',  'RFQ-2026-002 created by admin_buyer', 1),
  (2, 'BID_SUBMITTED',    'Supplier Alpha submitted bid at ₹820',    2),
  (3, 'AUCTION_CREATED',  'RFQ-2026-003 created by admin_buyer', 1),
  (3, 'AUCTION_CLOSED',   'RFQ-2026-003 closed — winner: Supplier Beta at ₹300', 1);
