USE food_forum;

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
