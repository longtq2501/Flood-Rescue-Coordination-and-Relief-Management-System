CREATE TABLE daily_request_snapshots (
    id                 BIGINT  NOT NULL AUTO_INCREMENT,
    snapshot_date      DATE    NOT NULL,
    total_requests     INT     DEFAULT 0,
    critical_count     INT     DEFAULT 0,
    high_count         INT     DEFAULT 0,
    medium_count       INT     DEFAULT 0,
    low_count          INT     DEFAULT 0,
    completed_count    INT     DEFAULT 0,
    cancelled_count    INT     DEFAULT 0,
    avg_response_min   DECIMAL(8,2),
    avg_complete_min   DECIMAL(8,2),
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_snapshot_date (snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE daily_resource_snapshots (
    id                    BIGINT  NOT NULL AUTO_INCREMENT,
    snapshot_date         DATE    NOT NULL,
    warehouse_id          BIGINT  NOT NULL,
    total_distributions   INT     DEFAULT 0,
    vehicles_deployed     INT     DEFAULT 0,
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_date_warehouse (snapshot_date, warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE team_performance_snapshots (
    id                  BIGINT  NOT NULL AUTO_INCREMENT,
    snapshot_date       DATE    NOT NULL,
    team_id             BIGINT  NOT NULL,
    missions_completed  INT     DEFAULT 0,
    avg_duration_min    DECIMAL(8,2),
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_date_team (snapshot_date, team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
