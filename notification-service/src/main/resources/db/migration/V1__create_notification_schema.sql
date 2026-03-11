CREATE TABLE IF NOT EXISTS notification_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    target_user_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL DEFAULT 'SSE',
    payload JSON NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at DATETIME,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS notification_delivery_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    error TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    logged_at DATETIME NOT NULL,
    CONSTRAINT fk_notification_event FOREIGN KEY (event_id) REFERENCES notification_events(id) ON DELETE CASCADE
);
