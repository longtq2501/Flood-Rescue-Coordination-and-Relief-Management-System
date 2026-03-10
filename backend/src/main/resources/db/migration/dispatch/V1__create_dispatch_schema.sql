CREATE TABLE rescue_teams (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100)    NOT NULL,
    leader_id    BIGINT          NOT NULL,
    capacity     INT             DEFAULT 4,
    status       VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
    current_lat  DECIMAL(10,8),
    current_lng  DECIMAL(11,8),
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE team_members (
    id        BIGINT   NOT NULL AUTO_INCREMENT,
    team_id   BIGINT   NOT NULL,
    user_id   BIGINT   NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_team_user (team_id, user_id),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES rescue_teams(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE assignments (
    id              BIGINT   NOT NULL AUTO_INCREMENT,
    request_id      BIGINT   NOT NULL,
    team_id         BIGINT   NOT NULL,
    vehicle_id      BIGINT   NOT NULL,
    citizen_id      BIGINT   NOT NULL,
    coordinator_id  BIGINT   NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    assigned_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at      DATETIME,
    completed_at    DATETIME,
    result_note     TEXT,
    PRIMARY KEY (id),
    INDEX idx_request (request_id),
    INDEX idx_team (team_id),
    INDEX idx_status (status),
    CONSTRAINT fk_assignments_team FOREIGN KEY (team_id) REFERENCES rescue_teams(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE location_logs (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    team_id     BIGINT          NOT NULL,
    lat         DECIMAL(10,8)   NOT NULL,
    lng         DECIMAL(11,8)   NOT NULL,
    speed       DECIMAL(5,2),
    heading     DECIMAL(5,2),
    logged_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_team_time (team_id, logged_at),
    CONSTRAINT fk_location_logs_team FOREIGN KEY (team_id) REFERENCES rescue_teams(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
