CREATE TABLE users (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(100)    NOT NULL,
    phone         VARCHAR(20)     NOT NULL,
    email         VARCHAR(100),
    password_hash VARCHAR(255)    NOT NULL,
    role          VARCHAR(20)     NOT NULL,
    status        VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    lat           DECIMAL(10,8),
    lng           DECIMAL(11,8),
    avatar_url    VARCHAR(500),
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    deleted_at    DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE refresh_tokens (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(500) NOT NULL,
    expires_at DATETIME     NOT NULL,
    revoked    TINYINT(1)   NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_token (token),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
