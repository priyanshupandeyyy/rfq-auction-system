-- RFQ Auction System - Database Schema
-- This script runs on startup. Tables are created if not exist.

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('BUYER', 'SUPPLIER') NOT NULL,
    company VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS auctions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    buyer_id BIGINT NOT NULL,
    base_price DECIMAL(15, 2),
    currency VARCHAR(10) DEFAULT 'INR',
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    hard_close_time TIMESTAMP NOT NULL,
    status ENUM('DRAFT', 'ACTIVE', 'CLOSED', 'CANCELLED') DEFAULT 'DRAFT',
    extension_trigger ENUM('TIME', 'RANK', 'COMBINED') DEFAULT 'TIME',
    extension_minutes INT DEFAULT 5,
    extension_window_minutes INT DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_auction_buyer FOREIGN KEY (buyer_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS bids (
    id BIGINT NOT NULL AUTO_INCREMENT,
    rfq_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    rank_position INT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_latest BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_bid_rfq FOREIGN KEY (rfq_id) REFERENCES auctions (id),
    CONSTRAINT fk_bid_supplier FOREIGN KEY (supplier_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    rfq_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    description TEXT,
    actor_id BIGINT,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_log_rfq FOREIGN KEY (rfq_id) REFERENCES auctions (id)
);

-- Seed demo data (insert only if tables are empty)
INSERT IGNORE INTO users (id, name, email, role, company) VALUES
(1, 'Buyer Corp', 'buyer@corp.com', 'BUYER', 'Buyer Corporation Ltd'),
(2, 'Supplier Alpha', 'alpha@supplier.com', 'SUPPLIER', 'Alpha Supplies Pvt Ltd'),
(3, 'Supplier Beta', 'beta@supplier.com', 'SUPPLIER', 'Beta Trading Co'),
(4, 'Supplier Gamma', 'gamma@supplier.com', 'SUPPLIER', 'Gamma Exports Ltd');
