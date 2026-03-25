USE food_forum;

ALTER TABLE `food_dish`
    ADD COLUMN IF NOT EXISTS `image_url` VARCHAR(2000) DEFAULT NULL AFTER `ingredients`;
