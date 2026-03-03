CREATE DATABASE IF NOT EXISTS db_auth      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_request   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_dispatch  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_resource  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_report    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON db_auth.*         TO 'rescueuser'@'%';
GRANT ALL PRIVILEGES ON db_request.*      TO 'rescueuser'@'%';
GRANT ALL PRIVILEGES ON db_dispatch.*     TO 'rescueuser'@'%';
GRANT ALL PRIVILEGES ON db_resource.*     TO 'rescueuser'@'%';
GRANT ALL PRIVILEGES ON db_notification.* TO 'rescueuser'@'%';
GRANT ALL PRIVILEGES ON db_report.*       TO 'rescueuser'@'%';
FLUSH PRIVILEGES;