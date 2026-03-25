USE food_forum;

INSERT INTO `strategy` (`author_user_id`, `title`, `summary`, `content`, `cover_image`, `status`, `sort`, `published_at`)
SELECT
    (SELECT `id` FROM `user` ORDER BY `id` ASC LIMIT 1),
    '第一次来这座城，三顿饭该怎么安排',
    '从早餐到夜宵，帮你用一天吃到最有代表性的本地味道。',
    '早餐建议从老街豆花和锅盔开始，中午优先安排本地老店的招牌面食，晚上再去夜市体验最热闹的小吃路线。正文里可以继续补充具体店名、排队时段、避坑建议和交通方式。',
    '/uploads/strategy-cover-1.jpg',
    1,
    30,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM `strategy` WHERE `title` = '第一次来这座城，三顿饭该怎么安排');

INSERT INTO `strategy` (`author_user_id`, `title`, `summary`, `content`, `cover_image`, `status`, `sort`, `published_at`)
SELECT
    (SELECT `id` FROM `user` ORDER BY `id` ASC LIMIT 1),
    '雨天也适合逛的室内美食路线',
    '适合答辩展示的主题型攻略，强调路线感和场景感。',
    '可以先从商圈里的老字号甜品店开始，接着去附近的特色小馆解决正餐，最后安排一家适合拍照和休息的饮品店。建议补充路线距离、推荐时间段和人均预算。',
    '/uploads/strategy-cover-2.jpg',
    1,
    20,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM `strategy` WHERE `title` = '雨天也适合逛的室内美食路线');

INSERT INTO `strategy` (`author_user_id`, `title`, `summary`, `content`, `cover_image`, `status`, `sort`, `published_at`)
SELECT
    (SELECT `id` FROM `user` ORDER BY `id` ASC LIMIT 1),
    '学生党 50 元以内也能吃好的平价清单',
    '突出预算控制、真实消费和高性价比推荐。',
    '这篇攻略适合把不同价位的小吃、主食和甜品做组合推荐，重点描述份量、口味、最佳下单方式以及容易踩坑的点。作为答辩展示，也可以加入“适合谁去”的说明。',
    '/uploads/strategy-cover-3.jpg',
    1,
    10,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM `strategy` WHERE `title` = '学生党 50 元以内也能吃好的平价清单');
