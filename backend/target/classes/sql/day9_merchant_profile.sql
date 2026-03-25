USE food_forum;

CREATE TABLE IF NOT EXISTS `merchant_profile` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `merchant_name` VARCHAR(128) DEFAULT NULL,
    `store_name` VARCHAR(128) DEFAULT NULL,
    `store_address` VARCHAR(255) DEFAULT NULL,
    `average_cost` INT DEFAULT NULL,
    `business_license_url` VARCHAR(255) DEFAULT NULL,
    `food_safety_cert_url` VARCHAR(255) DEFAULT NULL,
    `chef_team_intro` VARCHAR(500) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `reject_reason` VARCHAR(255) DEFAULT NULL,
    `last_audited_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_merchant_profile_user` (`user_id`),
    KEY `idx_merchant_profile_status` (`status`),
    CONSTRAINT `fk_merchant_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `chef_info` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `merchant_profile_id` BIGINT NOT NULL,
    `chef_name` VARCHAR(64) NOT NULL,
    `title` VARCHAR(64) DEFAULT NULL,
    `avatar_url` VARCHAR(255) DEFAULT NULL,
    `intro` VARCHAR(500) DEFAULT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_chef_profile` (`merchant_profile_id`),
    CONSTRAINT `fk_chef_profile` FOREIGN KEY (`merchant_profile_id`) REFERENCES `merchant_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
