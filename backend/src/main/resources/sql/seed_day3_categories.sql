USE food_forum;

INSERT INTO `food_category` (`name`, `icon`, `sort`, `status`, `description`)
SELECT '面食', '🍜', 1, 1, '适合展示本地面馆、汤面、拌面等内容'
WHERE NOT EXISTS (SELECT 1 FROM `food_category` WHERE `name` = '面食');

INSERT INTO `food_category` (`name`, `icon`, `sort`, `status`, `description`)
SELECT '小吃', '🥟', 2, 1, '适合展示街头小吃、夜市摊点等内容'
WHERE NOT EXISTS (SELECT 1 FROM `food_category` WHERE `name` = '小吃');

INSERT INTO `food_category` (`name`, `icon`, `sort`, `status`, `description`)
SELECT '甜品', '🍰', 3, 1, '适合展示本地甜品、糕点、饮品等内容'
WHERE NOT EXISTS (SELECT 1 FROM `food_category` WHERE `name` = '甜品');

INSERT INTO `food_category` (`name`, `icon`, `sort`, `status`, `description`)
SELECT '馆子推荐', '🍲', 4, 1, '适合展示特色餐馆、私房菜和馆子推荐'
WHERE NOT EXISTS (SELECT 1 FROM `food_category` WHERE `name` = '馆子推荐');
