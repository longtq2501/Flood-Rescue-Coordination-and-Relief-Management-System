CREATE TABLE notification_events (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    event_type     VARCHAR(100)  NOT NULL,
    target_user_id BIGINT        NOT NULL,
    channel        VARCHAR(20),
    payload        JSON          NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    sent_at        DATETIME,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_user_status (target_user_id, status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE user_subscriptions (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    topic         VARCHAR(200) NOT NULL,
    subscribed_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active        TINYINT(1)   DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_topic (user_id, topic)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE delivery_logs (
    id                    BIGINT   NOT NULL AUTO_INCREMENT,
    notification_event_id BIGINT   NOT NULL,
    attempt_number        INT      DEFAULT 1,
    status                VARCHAR(20) NOT NULL,
    error_message         TEXT,
    attempted_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_event_id (notification_event_id),
    CONSTRAINT fk_delivery_logs_event FOREIGN KEY (notification_event_id) REFERENCES notification_events(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
