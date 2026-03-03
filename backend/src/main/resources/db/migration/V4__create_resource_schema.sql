CREATE TABLE warehouses (
    id         BIGINT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100)    NOT NULL,
    address    VARCHAR(500),
    manager_id BIGINT          NOT NULL,
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE relief_items (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    warehouse_id    BIGINT          NOT NULL,
    name            VARCHAR(200)    NOT NULL,
    category        VARCHAR(100),
    unit            VARCHAR(50)     NOT NULL,
    quantity        INT             DEFAULT 0,
    low_threshold   INT             DEFAULT 10,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_warehouse (warehouse_id),
    INDEX idx_quantity (quantity),
    CONSTRAINT fk_relief_items_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE vehicles (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    plate_number    VARCHAR(20)     NOT NULL,
    type            VARCHAR(20)     NOT NULL,
    capacity        INT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
    current_lat     DECIMAL(10,8),
    current_lng     DECIMAL(11,8),
    assigned_team   BIGINT,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_plate_number (plate_number),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE vehicle_logs (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    vehicle_id   BIGINT   NOT NULL,
    assignment_id BIGINT,
    action       VARCHAR(20) NOT NULL,
    note         TEXT,
    logged_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_vehicle_id (vehicle_id),
    CONSTRAINT fk_vehicle_logs_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE distributions (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    request_id      BIGINT   NOT NULL,
    recipient_id    BIGINT   NOT NULL,
    coordinator_id  BIGINT   NOT NULL,
    note            TEXT,
    distributed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_request (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE distribution_items (
    id               BIGINT  NOT NULL AUTO_INCREMENT,
    distribution_id  BIGINT  NOT NULL,
    relief_item_id   BIGINT  NOT NULL,
    quantity         INT     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_distribution_items_dist FOREIGN KEY (distribution_id) REFERENCES distributions(id) ON DELETE CASCADE,
    CONSTRAINT fk_distribution_items_item FOREIGN KEY (relief_item_id) REFERENCES relief_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
