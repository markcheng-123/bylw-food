USE food_forum;
SET NAMES utf8mb4;

START TRANSACTION;

CREATE TEMPORARY TABLE tmp_target_users (username VARCHAR(64) PRIMARY KEY);
INSERT INTO tmp_target_users (username) VALUES
('root3'),('root4'),('root5'),
('root6'),('root7'),('root8'),('root9'),
('root10'),('root11'),('root12'),('root13'),('root14'),('root15'),('root16'),('root17');

CREATE TEMPORARY TABLE tmp_user_ids AS
SELECT u.id
FROM `user` u
JOIN tmp_target_users t ON t.username = u.username;

CREATE TEMPORARY TABLE tmp_old_posts AS
SELECT p.id
FROM food_post p
WHERE p.title LIKE '【种子数据】root%'
   OR p.author_user_id IN (SELECT id FROM tmp_user_ids);

DELETE FROM like_record WHERE user_id IN (SELECT id FROM tmp_user_ids) OR post_id IN (SELECT id FROM tmp_old_posts);
DELETE FROM comment WHERE user_id IN (SELECT id FROM tmp_user_ids) OR post_id IN (SELECT id FROM tmp_old_posts);
DELETE FROM food_image WHERE post_id IN (SELECT id FROM tmp_old_posts);
DELETE FROM food_dish WHERE post_id IN (SELECT id FROM tmp_old_posts);
DELETE FROM food_post WHERE id IN (SELECT id FROM tmp_old_posts);
DELETE FROM chef_info WHERE merchant_profile_id IN (SELECT id FROM merchant_profile WHERE user_id IN (SELECT id FROM tmp_user_ids));
DELETE FROM merchant_profile WHERE user_id IN (SELECT id FROM tmp_user_ids);
DELETE FROM merchant_application WHERE user_id IN (SELECT id FROM tmp_user_ids);
DELETE FROM `user` WHERE id IN (SELECT id FROM tmp_user_ids);

DROP TEMPORARY TABLE tmp_old_posts;
DROP TEMPORARY TABLE tmp_user_ids;

INSERT IGNORE INTO food_category (`name`, `icon`, `sort`, `status`, `description`) VALUES
('热干面', '🍜', 1, 1, '武汉本地特色面食'),
('烧烤', '🍢', 2, 1, '夜宵烧烤与烤串'),
('火锅', '🍲', 3, 1, '川渝火锅与地方锅物'),
('甜品', '🍰', 4, 1, '奶茶甜品与烘焙'),
('小吃', '🥟', 5, 1, '街头小吃与馆子推荐');

SET @pwd123456 = '$2a$10$0FMQgD5zyI46nSti6Sj95uWim3KPpI5Dng3SD6lAxGfXMuZmosAfm';

INSERT INTO `user`
(`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `role`, `status`, `created_at`, `updated_at`)
VALUES
('root3',  @pwd123456, 'merchant-3', 'https://picsum.photos/seed/root3/300/300',  '13800001003', 'root3@example.com',  2, 1, NOW() - INTERVAL 90 DAY, NOW()),
('root4',  @pwd123456, 'merchant-4', 'https://picsum.photos/seed/root4/300/300',  '13800001004', 'root4@example.com',  2, 1, NOW() - INTERVAL 89 DAY, NOW()),
('root5',  @pwd123456, 'merchant-5', 'https://picsum.photos/seed/root5/300/300',  '13800001005', 'root5@example.com',  2, 1, NOW() - INTERVAL 88 DAY, NOW()),
('root6',  @pwd123456, 'user-6',     'https://picsum.photos/seed/root6/300/300',  '13900000006', 'root6@example.com',  1, 1, NOW() - INTERVAL 87 DAY, NOW()),
('root7',  @pwd123456, 'user-7',     'https://picsum.photos/seed/root7/300/300',  '13900000007', 'root7@example.com',  1, 1, NOW() - INTERVAL 86 DAY, NOW()),
('root8',  @pwd123456, 'user-8',     'https://picsum.photos/seed/root8/300/300',  '13900000008', 'root8@example.com',  1, 1, NOW() - INTERVAL 85 DAY, NOW()),
('root9',  @pwd123456, 'user-9',     'https://picsum.photos/seed/root9/300/300',  '13900000009', 'root9@example.com',  1, 1, NOW() - INTERVAL 84 DAY, NOW()),
('root10', @pwd123456, 'user-10',    'https://picsum.photos/seed/root10/300/300', '13900000010', 'root10@example.com', 1, 1, NOW() - INTERVAL 83 DAY, NOW()),
('root11', @pwd123456, 'user-11',    'https://picsum.photos/seed/root11/300/300', '13900000011', 'root11@example.com', 1, 1, NOW() - INTERVAL 82 DAY, NOW()),
('root12', @pwd123456, 'user-12',    'https://picsum.photos/seed/root12/300/300', '13900000012', 'root12@example.com', 1, 1, NOW() - INTERVAL 81 DAY, NOW()),
('root13', @pwd123456, 'user-13',    'https://picsum.photos/seed/root13/300/300', '13900000013', 'root13@example.com', 1, 1, NOW() - INTERVAL 80 DAY, NOW()),
('root14', @pwd123456, 'user-14',    'https://picsum.photos/seed/root14/300/300', '13900000014', 'root14@example.com', 1, 1, NOW() - INTERVAL 79 DAY, NOW()),
('root15', @pwd123456, 'user-15',    'https://picsum.photos/seed/root15/300/300', '13900000015', 'root15@example.com', 1, 1, NOW() - INTERVAL 78 DAY, NOW()),
('root16', @pwd123456, 'user-16',    'https://picsum.photos/seed/root16/300/300', '13900000016', 'root16@example.com', 1, 1, NOW() - INTERVAL 77 DAY, NOW()),
('root17', @pwd123456, 'user-17',    'https://picsum.photos/seed/root17/300/300', '13900000017', 'root17@example.com', 1, 1, NOW() - INTERVAL 76 DAY, NOW());

INSERT INTO merchant_application
(`user_id`, `merchant_name`, `contact_name`, `contact_phone`, `business_license`, `address`, `description`, `status`, `reject_reason`, `submitted_at`, `audited_at`, `created_at`, `updated_at`)
VALUES
((SELECT id FROM `user` WHERE username='root3'), '江城风味馆', '王强', '13800001003', '/uploads/seed-assets/license_root3.svg', '湖北省 武汉市 武昌区中北路88号', '主打湖北本地风味。', 1, NULL, NOW()-INTERVAL 50 DAY, NOW()-INTERVAL 49 DAY, NOW()-INTERVAL 50 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root4'), '渝味火锅局', '李娜', '13800001004', '/uploads/seed-assets/license_root4.svg', '重庆市 渝中区解放碑步行街9号', '川渝火锅，重麻重辣。', 1, NULL, NOW()-INTERVAL 48 DAY, NOW()-INTERVAL 47 DAY, NOW()-INTERVAL 48 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root5'), '岭南夜宵档', '张敏', '13800001005', '/uploads/seed-assets/license_root5.svg', '广东省 广州市天河区体育西路66号', '粤式夜宵与砂锅粥。', 1, NULL, NOW()-INTERVAL 46 DAY, NOW()-INTERVAL 45 DAY, NOW()-INTERVAL 46 DAY, NOW());

INSERT INTO merchant_profile
(`user_id`, `merchant_name`, `store_name`, `store_address`, `average_cost`, `business_license_url`, `food_safety_cert_url`, `chef_team_intro`, `status`, `reject_reason`, `last_audited_at`, `created_at`, `updated_at`)
VALUES
((SELECT id FROM `user` WHERE username='root3'), '江城风味馆', '江城风味馆', '湖北省 武汉市 武昌区中北路88号', 58, '/uploads/seed-assets/license_root3.svg', '/uploads/seed-assets/health_root3.svg', '团队擅长楚菜与湖北小吃研发。', 1, NULL, NOW()-INTERVAL 49 DAY, NOW()-INTERVAL 50 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root4'), '渝味火锅局', '渝味火锅局', '重庆市 渝中区解放碑步行街9号', 96, '/uploads/seed-assets/license_root4.svg', '/uploads/seed-assets/health_root4.svg', '主厨团队来自重庆老火锅门店。', 1, NULL, NOW()-INTERVAL 47 DAY, NOW()-INTERVAL 48 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root5'), '岭南夜宵档', '岭南夜宵档', '广东省 广州市天河区体育西路66号', 72, '/uploads/seed-assets/license_root5.svg', '/uploads/seed-assets/health_root5.svg', '擅长粤式夜宵与广式煲汤。', 1, NULL, NOW()-INTERVAL 45 DAY, NOW()-INTERVAL 46 DAY, NOW());

INSERT INTO chef_info
(`merchant_profile_id`, `chef_name`, `title`, `avatar_url`, `intro`, `sort`, `status`, `created_at`, `updated_at`)
VALUES
((SELECT id FROM merchant_profile WHERE user_id=(SELECT id FROM `user` WHERE username='root3')), '周师傅', '主理厨师', '/uploads/seed-assets/chef_root3_zhou.svg', '擅长热干面与藕汤。', 1, 1, NOW()-INTERVAL 30 DAY, NOW()),
((SELECT id FROM merchant_profile WHERE user_id=(SELECT id FROM `user` WHERE username='root3')), '刘师傅', '面点主管', '/uploads/seed-assets/chef_root3_liu.svg', '负责手工面点和早餐档。', 2, 1, NOW()-INTERVAL 29 DAY, NOW()),
((SELECT id FROM merchant_profile WHERE user_id=(SELECT id FROM `user` WHERE username='root4')), '陈师傅', '锅底研发', '/uploads/seed-assets/chef_root4_chen.svg', '负责火锅底料与新菜研发。', 1, 1, NOW()-INTERVAL 28 DAY, NOW()),
((SELECT id FROM merchant_profile WHERE user_id=(SELECT id FROM `user` WHERE username='root4')), '何师傅', '后厨总管', '/uploads/seed-assets/chef_root4_he.svg', '把控出品与食材质量。', 2, 1, NOW()-INTERVAL 27 DAY, NOW()),
((SELECT id FROM merchant_profile WHERE user_id=(SELECT id FROM `user` WHERE username='root5')), '黄师傅', '粤菜主厨', '/uploads/seed-assets/chef_root5_huang.svg', '擅长砂锅粥与煲仔菜。', 1, 1, NOW()-INTERVAL 26 DAY, NOW()),
((SELECT id FROM merchant_profile WHERE user_id=(SELECT id FROM `user` WHERE username='root5')), '杨师傅', '夜宵档主理', '/uploads/seed-assets/chef_root5_yang.svg', '主打夜宵烧烤与小炒。', 2, 1, NOW()-INTERVAL 25 DAY, NOW());

INSERT INTO food_post
(`author_user_id`, `category_id`, `title`, `summary`, `content`, `cover_image`, `address`, `per_capita`, `status`, `view_count`, `like_count`, `comment_count`, `published_at`, `created_at`, `updated_at`)
VALUES
((SELECT id FROM `user` WHERE username='root3'),  (SELECT id FROM food_category WHERE name='热干面' LIMIT 1), '【种子数据】root3的江城热干面', '芝麻酱浓郁，面条筋道。', '门店早高峰排队较多，建议错峰前往。', '/uploads/seed-assets/post_root3_hotdrynoodles.svg', '湖北省 武汉市 武昌区中北路88号', 58, 1, 320, 0, 0, NOW()-INTERVAL 20 DAY, NOW()-INTERVAL 20 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root4'),  (SELECT id FROM food_category WHERE name='火锅' LIMIT 1),   '【种子数据】root4的牛油九宫格', '锅底厚重，毛肚脆爽。', '中辣起步，建议搭配冰粉解辣。', '/uploads/seed-assets/post_root4_hotpot.svg', '重庆市 渝中区解放碑步行街9号', 96, 1, 410, 0, 0, NOW()-INTERVAL 19 DAY, NOW()-INTERVAL 19 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root5'),  (SELECT id FROM food_category WHERE name='烧烤' LIMIT 1),   '【种子数据】root5的炭烤拼盘', '海鲜和肉串都很新鲜。', '夜宵氛围很好，招牌砂锅粥值得回点。', '/uploads/seed-assets/post_root5_bbq.svg', '广东省 广州市天河区体育西路66号', 72, 1, 287, 0, 0, NOW()-INTERVAL 18 DAY, NOW()-INTERVAL 18 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root6'),  (SELECT id FROM food_category WHERE name='小吃' LIMIT 1),   '【种子数据】root6的巷子口生煎', '皮薄汁多，现煎现卖。', '晚餐后路过尝了一份，建议搭配酸梅汤。', 'https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=1200&q=80', '湖北省 武汉市 洪山区', 28, 1, 135, 0, 0, NOW()-INTERVAL 17 DAY, NOW()-INTERVAL 17 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root7'),  (SELECT id FROM food_category WHERE name='甜品' LIMIT 1),   '【种子数据】root7的奶油蛋糕', '甜度适中，颜值在线。', '适合下午茶，环境安静，拍照很出片。', 'https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=1200&q=80', '湖南省 长沙市 开福区', 42, 1, 122, 0, 0, NOW()-INTERVAL 16 DAY, NOW()-INTERVAL 16 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root8'),  (SELECT id FROM food_category WHERE name='烧烤' LIMIT 1),   '【种子数据】root8的桥头烧烤', '烤五花肉很香。', '夜里11点后人多，建议早点去占位。', 'https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?auto=format&fit=crop&w=1200&q=80', '江西省 南昌市 红谷滩区', 46, 1, 141, 0, 0, NOW()-INTERVAL 15 DAY, NOW()-INTERVAL 15 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root9'),  (SELECT id FROM food_category WHERE name='火锅' LIMIT 1),   '【种子数据】root9的牛肉火锅', '牛肉新鲜，蘸料丰富。', '服务响应很快，锅底偏鲜香。', 'https://images.unsplash.com/photo-1514516345957-556ca7f68fc9?auto=format&fit=crop&w=1200&q=80', '浙江省 杭州市 西湖区', 88, 1, 188, 0, 0, NOW()-INTERVAL 14 DAY, NOW()-INTERVAL 14 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root10'), (SELECT id FROM food_category WHERE name='小吃' LIMIT 1),   '【种子数据】root10的糯米鸡', '软糯不腻，性价比高。', '早上8点前到店最佳，卖完很快。', 'https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=1200&q=80', '广东省 广州市 海珠区', 19, 1, 95, 0, 0, NOW()-INTERVAL 13 DAY, NOW()-INTERVAL 13 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root11'), (SELECT id FROM food_category WHERE name='热干面' LIMIT 1), '【种子数据】root11的拌面档口', '面条劲道，咸香平衡。', '加一勺辣油风味更好，价格亲民。', 'https://images.unsplash.com/photo-1555126634-323283e090fa?auto=format&fit=crop&w=1200&q=80', '湖北省 襄阳市 樊城区', 24, 1, 108, 0, 0, NOW()-INTERVAL 12 DAY, NOW()-INTERVAL 12 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root12'), (SELECT id FROM food_category WHERE name='甜品' LIMIT 1),   '【种子数据】root12的芝士挞', '出炉后口感最佳。', '建议搭配美式咖啡，甜度更平衡。', 'https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=1200&q=80', '江苏省 南京市 鼓楼区', 33, 1, 116, 0, 0, NOW()-INTERVAL 11 DAY, NOW()-INTERVAL 11 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root13'), (SELECT id FROM food_category WHERE name='烧烤' LIMIT 1),   '【种子数据】root13的牛板筋', '口感脆韧有嚼劲。', '调味偏重口，适合配冰镇饮料。', 'https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?auto=format&fit=crop&w=1200&q=80', '四川省 成都市 武侯区', 52, 1, 156, 0, 0, NOW()-INTERVAL 10 DAY, NOW()-INTERVAL 10 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root14'), (SELECT id FROM food_category WHERE name='火锅' LIMIT 1),   '【种子数据】root14的番茄锅底', '酸甜开胃，孩子也能吃。', '食材新鲜，服务态度不错。', 'https://images.unsplash.com/photo-1514516345957-556ca7f68fc9?auto=format&fit=crop&w=1200&q=80', '山东省 青岛市 市南区', 79, 1, 129, 0, 0, NOW()-INTERVAL 9 DAY, NOW()-INTERVAL 9 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root15'), (SELECT id FROM food_category WHERE name='小吃' LIMIT 1),   '【种子数据】root15的酱香饼', '饼皮酥脆，酱香足。', '路边摊但很干净，晚高峰排队。', 'https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=1200&q=80', '河南省 郑州市 金水区', 16, 1, 102, 0, 0, NOW()-INTERVAL 8 DAY, NOW()-INTERVAL 8 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root16'), (SELECT id FROM food_category WHERE name='甜品' LIMIT 1),   '【种子数据】root16的杨枝甘露', '果香明显，奶味顺滑。', '店里环境清爽，适合短暂停留。', 'https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=1200&q=80', '福建省 厦门市 思明区', 36, 1, 119, 0, 0, NOW()-INTERVAL 7 DAY, NOW()-INTERVAL 7 DAY, NOW()),
((SELECT id FROM `user` WHERE username='root17'), (SELECT id FROM food_category WHERE name='热干面' LIMIT 1), '【种子数据】root17的牛肉面', '汤头浓郁，牛肉软烂。', '适合早餐和午餐，分量足。', 'https://images.unsplash.com/photo-1555126634-323283e090fa?auto=format&fit=crop&w=1200&q=80', '陕西省 西安市 雁塔区', 32, 1, 147, 0, 0, NOW()-INTERVAL 6 DAY, NOW()-INTERVAL 6 DAY, NOW());

INSERT INTO food_image (`post_id`, `image_url`, `sort`, `created_at`)
SELECT id, cover_image, 0, created_at
FROM food_post
WHERE title LIKE '【种子数据】root%';

INSERT INTO food_dish (`post_id`, `dish_name`, `ingredients`, `image_url`, `allergens`, `sort`, `created_at`) VALUES
((SELECT id FROM food_post WHERE title='【种子数据】root3的江城热干面' LIMIT 1), '招牌热干面', '碱水面,芝麻酱,萝卜丁,葱花', '/uploads/seed-assets/dish_root3_hotdrynoodles.svg', '麸质,芝麻', 1, NOW()-INTERVAL 20 DAY),
((SELECT id FROM food_post WHERE title='【种子数据】root4的牛油九宫格' LIMIT 1), '牛油红汤锅', '牛油,辣椒,花椒,牛骨汤', '/uploads/seed-assets/dish_root4_hotpot.svg', '大豆,麸质', 1, NOW()-INTERVAL 19 DAY),
((SELECT id FROM food_post WHERE title='【种子数据】root5的炭烤拼盘' LIMIT 1), '炭烤拼盘', '鸡翅,牛肉串,鱿鱼,辣椒粉', '/uploads/seed-assets/dish_root5_bbq.svg', '海鲜,麸质', 1, NOW()-INTERVAL 18 DAY);

INSERT IGNORE INTO like_record (`post_id`, `user_id`, `created_at`)
SELECT p.id, u.id, NOW() - INTERVAL MOD(p.id + u.id, 12) DAY
FROM food_post p
JOIN `user` u ON u.username IN (SELECT username FROM tmp_target_users)
WHERE p.title LIKE '【种子数据】root%'
  AND u.id <> p.author_user_id
  AND MOD(p.id + u.id, 3) = 0;

UPDATE food_post p
SET like_count = (SELECT COUNT(1) FROM like_record l WHERE l.post_id = p.id)
WHERE p.title LIKE '【种子数据】root%';

DROP TEMPORARY TABLE tmp_target_users;

COMMIT;

SELECT
  COUNT(*) AS total_users,
  SUM(CASE WHEN role = 2 THEN 1 ELSE 0 END) AS merchant_users,
  SUM(CASE WHEN role = 1 THEN 1 ELSE 0 END) AS normal_users
FROM `user`
WHERE username IN ('root3','root4','root5','root6','root7','root8','root9','root10','root11','root12','root13','root14','root15','root16','root17');

SELECT COUNT(*) AS total_posts
FROM food_post
WHERE title LIKE '【种子数据】root%';

SELECT COUNT(*) AS total_likes
FROM like_record
WHERE user_id IN (
  SELECT id FROM `user`
  WHERE username IN ('root3','root4','root5','root6','root7','root8','root9','root10','root11','root12','root13','root14','root15','root16','root17')
);
