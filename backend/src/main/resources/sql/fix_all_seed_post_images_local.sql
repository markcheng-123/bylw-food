USE food_forum;
SET NAMES utf8mb4;

UPDATE food_post
SET cover_image = CASE title
  WHEN '【种子数据】root3的江城热干面' THEN '/uploads/seed-assets/post_root3_hotdrynoodles.svg'
  WHEN '【种子数据】root4的牛油九宫格' THEN '/uploads/seed-assets/post_root4_hotpot.svg'
  WHEN '【种子数据】root5的炭烤拼盘' THEN '/uploads/seed-assets/post_root5_bbq.svg'
  WHEN '【种子数据】root6的巷子口生煎' THEN '/uploads/seed-assets/post_root6_shengjian.svg'
  WHEN '【种子数据】root7的奶油蛋糕' THEN '/uploads/seed-assets/post_root7_cake.svg'
  WHEN '【种子数据】root8的桥头烧烤' THEN '/uploads/seed-assets/post_root8_bbq.svg'
  WHEN '【种子数据】root9的牛肉火锅' THEN '/uploads/seed-assets/post_root9_beefhotpot.svg'
  WHEN '【种子数据】root10的糯米鸡' THEN '/uploads/seed-assets/post_root10_nuomiji.svg'
  WHEN '【种子数据】root11的拌面档口' THEN '/uploads/seed-assets/post_root11_mian.svg'
  WHEN '【种子数据】root12的芝士挞' THEN '/uploads/seed-assets/post_root12_tart.svg'
  WHEN '【种子数据】root13的牛板筋' THEN '/uploads/seed-assets/post_root13_skewer.svg'
  WHEN '【种子数据】root14的番茄锅底' THEN '/uploads/seed-assets/post_root14_tomatohotpot.svg'
  WHEN '【种子数据】root15的酱香饼' THEN '/uploads/seed-assets/post_root15_jiangxiangbing.svg'
  WHEN '【种子数据】root16的杨枝甘露' THEN '/uploads/seed-assets/post_root16_yangzhiganlu.svg'
  WHEN '【种子数据】root17的牛肉面' THEN '/uploads/seed-assets/post_root17_beefnoodle.svg'
  ELSE cover_image
END
WHERE title LIKE '【种子数据】root%';

UPDATE food_image fi
JOIN food_post p ON p.id = fi.post_id
SET fi.image_url = p.cover_image
WHERE p.title LIKE '【种子数据】root%';

SELECT id, title, cover_image
FROM food_post
WHERE title LIKE '【种子数据】root%'
ORDER BY id;
