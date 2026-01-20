-- ===============================
-- Migration: BaseAuction tables
-- ===============================

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- -------------------------------
-- ba_lots — хранение лотов
-- -------------------------------
CREATE TABLE ba_lots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_data TEXT NOT NULL,
    start_price DECIMAL(18,2) NOT NULL,
    min_bid_step DECIMAL(18,2) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status ENUM('PLANNED', 'RUNNING', 'FINISHED', 'CANCELLED') NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_ba_lots_status (status),
    INDEX idx_ba_lots_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------
-- ba_bids — хранение ставок
-- -------------------------------
CREATE TABLE ba_bids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    lot_id INT NOT NULL,
    player_uuid VARCHAR(36) NOT NULL,
    player_name VARCHAR(16) NOT NULL,
    bid_amount DECIMAL(18,2) NOT NULL,
    bid_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_winning BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_ba_bids_lot
        FOREIGN KEY (lot_id)
            REFERENCES ba_lots(id)
            ON DELETE CASCADE,

    INDEX idx_ba_bids_lot (lot_id),
    INDEX idx_ba_bids_player (player_uuid),
    INDEX idx_ba_bids_winning (is_winning)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------
-- ba_history — история аукционов
-- -------------------------------
CREATE TABLE ba_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    lot_id INT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    item_data TEXT NOT NULL,
    winner_uuid VARCHAR(36),
    winner_name VARCHAR(16),
    final_price DECIMAL(18,2),
    commission_taken DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_ba_history_winner (winner_uuid),
    INDEX idx_ba_history_completed (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------
-- ba_claims — предметы к получению
-- -------------------------------
CREATE TABLE ba_claims (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_uuid VARCHAR(36) NOT NULL,
    lot_id INT NOT NULL,
    item_data TEXT NOT NULL,
    price_paid DECIMAL(18,2) NOT NULL,
    won_at TIMESTAMP NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_ba_claims_player (player_uuid),
    INDEX idx_ba_claims_lot (lot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------
-- ba_player_history — история игрока
-- -----------------------------------------
CREATE TABLE ba_player_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_uuid VARCHAR(36) NOT NULL,
    lot_id INT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    final_price DECIMAL(18,2) NOT NULL,
    won_at TIMESTAMP NOT NULL,
    claimed_at TIMESTAMP NULL,
    status ENUM('CLAIMED', 'PENDING') NOT NULL,

    INDEX idx_ba_ph_player (player_uuid),
    INDEX idx_ba_ph_won (won_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
