USE food_forum;

ALTER TABLE `food_dish`
    MODIFY COLUMN `image_url` VARCHAR(2000) DEFAULT NULL;
