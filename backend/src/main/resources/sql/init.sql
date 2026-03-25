CREATE DATABASE IF NOT EXISTS food_forum
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE food_forum;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(64) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(64) NOT NULL,
    `avatar` VARCHAR(255) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `email` VARCHAR(128) DEFAULT NULL,
    `role` TINYINT NOT NULL DEFAULT 1,
    `status` TINYINT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_username` (`username`),
    KEY `idx_user_role` (`role`),
    KEY `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `food_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL,
    `icon` VARCHAR(255) DEFAULT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `description` VARCHAR(255) DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_category_name` (`name`),
    KEY `idx_category_status` (`status`),
    KEY `idx_category_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `food_post` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `author_user_id` BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    `title` VARCHAR(120) NOT NULL,
    `summary` VARCHAR(255) DEFAULT NULL,
    `content` TEXT NOT NULL,
    `cover_image` VARCHAR(255) DEFAULT NULL,
    `address` VARCHAR(255) DEFAULT NULL,
    `per_capita` INT DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `view_count` INT NOT NULL DEFAULT 0,
    `like_count` INT NOT NULL DEFAULT 0,
    `comment_count` INT NOT NULL DEFAULT 0,
    `published_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_post_author` (`author_user_id`),
    KEY `idx_post_category` (`category_id`),
    KEY `idx_post_status` (`status`),
    KEY `idx_post_created` (`created_at`),
    CONSTRAINT `fk_post_author` FOREIGN KEY (`author_user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_post_category` FOREIGN KEY (`category_id`) REFERENCES `food_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `food_image` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `image_url` VARCHAR(255) NOT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_food_image_post` (`post_id`),
    CONSTRAINT `fk_food_image_post` FOREIGN KEY (`post_id`) REFERENCES `food_post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `food_dish` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `dish_name` VARCHAR(64) NOT NULL,
    `ingredients` VARCHAR(500) NOT NULL,
    `image_url` VARCHAR(2000) DEFAULT NULL,
    `allergens` VARCHAR(255) DEFAULT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_food_dish_post` (`post_id`),
    CONSTRAINT `fk_food_dish_post` FOREIGN KEY (`post_id`) REFERENCES `food_post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT NULL,
    `content` VARCHAR(500) NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_comment_post` (`post_id`),
    KEY `idx_comment_user` (`user_id`),
    KEY `idx_comment_created` (`created_at`),
    CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `food_post` (`id`),
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `like_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_like_user_post` (`user_id`, `post_id`),
    KEY `idx_like_post` (`post_id`),
    KEY `idx_like_user` (`user_id`),
    CONSTRAINT `fk_like_post` FOREIGN KEY (`post_id`) REFERENCES `food_post` (`id`),
    CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `merchant_application` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `merchant_name` VARCHAR(128) NOT NULL,
    `contact_name` VARCHAR(64) NOT NULL,
    `contact_phone` VARCHAR(20) NOT NULL,
    `business_license` VARCHAR(255) DEFAULT NULL,
    `address` VARCHAR(255) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `reject_reason` VARCHAR(255) DEFAULT NULL,
    `submitted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `audited_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_merchant_user` (`user_id`),
    KEY `idx_merchant_status` (`status`),
    CONSTRAINT `fk_merchant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE IF NOT EXISTS `strategy` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `author_user_id` BIGINT DEFAULT NULL,
    `title` VARCHAR(120) NOT NULL,
    `summary` VARCHAR(255) DEFAULT NULL,
    `content` TEXT NOT NULL,
    `cover_image` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `sort` INT NOT NULL DEFAULT 0,
    `published_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_strategy_status` (`status`),
    KEY `idx_strategy_created` (`created_at`),
    CONSTRAINT `fk_strategy_author` FOREIGN KEY (`author_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `audit_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `business_type` VARCHAR(32) NOT NULL,
    `business_id` BIGINT NOT NULL,
    `operator_admin_id` BIGINT NOT NULL,
    `result` TINYINT NOT NULL,
    `remark` VARCHAR(255) DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_audit_business` (`business_type`, `business_id`),
    KEY `idx_audit_operator` (`operator_admin_id`),
    CONSTRAINT `fk_audit_operator` FOREIGN KEY (`operator_admin_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
