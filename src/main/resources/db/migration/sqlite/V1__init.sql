PRAGMA foreign_keys = ON;

-- -------------------------------
-- ba_lots
-- -------------------------------
CREATE TABLE ba_lots
(
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    slot                INTEGER   NOT NULL,
    item_data           TEXT      NOT NULL,
    start_price         REAL      NOT NULL,
    min_bid_step        REAL      NOT NULL,
    start_time          TIMESTAMP NOT NULL,
    end_time            TIMESTAMP NOT NULL,
    status              INTEGER   NOT NULL,
    created_by          TEXT      NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    current_price       REAL      NOT NULL,
    current_winner_uuid TEXT
);

CREATE INDEX idx_ba_lots_status ON ba_lots(status);
CREATE INDEX idx_ba_lots_time ON ba_lots(start_time, end_time);

-- -------------------------------
-- ba_bids
-- -------------------------------
CREATE TABLE ba_bids
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    lot_id      INTEGER   NOT NULL,
    player_uuid TEXT      NOT NULL,
    player_name TEXT      NOT NULL,
    bid_amount  REAL      NOT NULL,
    bid_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (lot_id)
        REFERENCES ba_lots (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_ba_bids_lot ON ba_bids(lot_id);
CREATE INDEX idx_ba_bids_player ON ba_bids(player_uuid);

-- -------------------------------
-- ba_history
-- -------------------------------
CREATE TABLE ba_history
(
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    lot_id           INTEGER   NOT NULL,
    item_name        TEXT      NOT NULL,
    item_data        TEXT      NOT NULL,
    winner_uuid      TEXT,
    winner_name      TEXT,
    final_price      REAL,
    commission_taken REAL      NOT NULL DEFAULT 0.0,
    start_time       TIMESTAMP NOT NULL,
    end_time         TIMESTAMP NOT NULL,
    completed_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (lot_id)
        REFERENCES ba_lots (id)
);

CREATE INDEX idx_ba_history_winner ON ba_history(winner_uuid);
CREATE INDEX idx_ba_history_completed ON ba_history(completed_at);

-- -------------------------------
-- ba_claims
-- -------------------------------
CREATE TABLE ba_claims
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    player_uuid TEXT      NOT NULL,
    lot_id      INTEGER   NOT NULL,
    item_data   TEXT      NOT NULL,
    price_paid  REAL      NOT NULL,
    won_at      TIMESTAMP NOT NULL,
    added_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (lot_id)
        REFERENCES ba_lots (id),

    UNIQUE (player_uuid, lot_id)
);

CREATE INDEX idx_ba_claims_player ON ba_claims(player_uuid);
CREATE INDEX idx_ba_claims_lot ON ba_claims(lot_id);

-- -------------------------------
-- ba_player_history
-- -------------------------------
CREATE TABLE ba_player_history
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    player_uuid TEXT      NOT NULL,
    lot_id      INTEGER   NOT NULL,
    item_name   TEXT      NOT NULL,
    final_price REAL      NOT NULL,
    won_at      TIMESTAMP NOT NULL,
    claimed_at  TIMESTAMP
);

CREATE INDEX idx_ba_ph_player ON ba_player_history(player_uuid);
CREATE INDEX idx_ba_ph_won ON ba_player_history(won_at);