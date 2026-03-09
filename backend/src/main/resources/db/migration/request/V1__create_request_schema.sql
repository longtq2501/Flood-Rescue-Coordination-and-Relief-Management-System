CREATE TABLE rescue_requests (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    citizen_id      BIGINT          NOT NULL,
    lat             DECIMAL(10,8)   NOT NULL,
    lng             DECIMAL(11,8)   NOT NULL,
    address_text    VARCHAR(500),
    description     TEXT            NOT NULL,
    num_people      INT             DEFAULT 1,
    urgency_level   VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    coordinator_id  BIGINT,
    verified_at     DATETIME,
    completed_at    DATETIME,
    confirmed_at    DATETIME,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_status (status),
    INDEX idx_citizen (citizen_id),
    INDEX idx_location (lat, lng),
    INDEX idx_urgency (urgency_level),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE request_images (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    request_id  BIGINT          NOT NULL,
    image_url   VARCHAR(500)    NOT NULL,
    uploaded_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_request_id (request_id),
    CONSTRAINT fk_request_images_request FOREIGN KEY (request_id) REFERENCES rescue_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE status_history (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    request_id   BIGINT      NOT NULL,
    from_status  VARCHAR(20),
    to_status    VARCHAR(20) NOT NULL,
    changed_by   BIGINT      NOT NULL,
    note         TEXT,
    changed_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_request_id (request_id),
    CONSTRAINT fk_status_history_request FOREIGN KEY (request_id) REFERENCES rescue_requests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
