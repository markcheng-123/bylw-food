CREATE TABLE IF NOT EXISTS `user_legacy_backup` AS
SELECT *
FROM `user`;

ALTER TABLE `user`
    CHANGE COLUMN `password_hash` `password` VARCHAR(255) NOT NULL,
    CHANGE COLUMN `avatar_url` `avatar` VARCHAR(255) NULL,
    ADD COLUMN `phone` VARCHAR(20) NULL AFTER `avatar`,
    ADD COLUMN `email` VARCHAR(128) NULL AFTER `phone`,
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`;

ALTER TABLE `user`
    ADD COLUMN `role_code` TINYINT NULL AFTER `bio`,
    ADD COLUMN `status_code` TINYINT NULL AFTER `role_code`;

UPDATE `user`
SET `role_code` = CASE `role`
        WHEN 'ADMIN' THEN 9
        WHEN 'MERCHANT' THEN 2
        ELSE 1
    END,
    `status_code` = CASE `status`
        WHEN 'ENABLED' THEN 1
        ELSE 0
    END;

ALTER TABLE `user`
    DROP COLUMN `role`,
    DROP COLUMN `status`,
    CHANGE COLUMN `role_code` `role` TINYINT NOT NULL DEFAULT 1,
    CHANGE COLUMN `status_code` `status` TINYINT NOT NULL DEFAULT 1;
