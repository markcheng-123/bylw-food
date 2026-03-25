USE food_forum;
SET NAMES utf8mb4;

UPDATE food_post
SET cover_image = CASE title
  WHEN '【种子数据】root3的江城热干面' THEN 'https://images.unsplash.com/photo-1585032226651-759b368d7246?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root4的牛油九宫格' THEN 'https://images.unsplash.com/photo-1544145945-f90425340c7e?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root5的炭烤拼盘' THEN 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root6的巷子口生煎' THEN 'https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root7的奶油蛋糕' THEN 'https://images.unsplash.com/photo-1464306076886-da185f6a9d05?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root8的桥头烧烤' THEN 'https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root9的牛肉火锅' THEN 'https://images.unsplash.com/photo-1514516345957-556ca7f68fc9?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root10的糯米鸡' THEN 'https://images.unsplash.com/photo-1512058564366-18510be2db19?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root11的拌面档口' THEN 'https://images.unsplash.com/photo-1555126634-323283e090fa?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root12的芝士挞' THEN 'https://images.unsplash.com/photo-1488477304112-4944851de03d?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root13的牛板筋' THEN 'https://images.unsplash.com/photo-1558030006-450675393462?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root14的番茄锅底' THEN 'https://images.unsplash.com/photo-1534939561126-855b8675edd7?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root15的酱香饼' THEN 'https://images.unsplash.com/photo-1528137871618-79d2761e3fd5?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root16的杨枝甘露' THEN 'https://images.unsplash.com/photo-1505253716362-afaea1d3d1af?auto=format&fit=crop&w=1200&q=80'
  WHEN '【种子数据】root17的牛肉面' THEN 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=1200&q=80'
  ELSE cover_image
END
WHERE title LIKE '【种子数据】root%';

UPDATE food_image fi
JOIN food_post p ON p.id = fi.post_id
SET fi.image_url = p.cover_image
WHERE p.title LIKE '【种子数据】root%';

UPDATE food_dish d
JOIN food_post p ON p.id = d.post_id
SET d.image_url = CASE p.title
  WHEN '【种子数据】root3的江城热干面' THEN 'https://images.unsplash.com/photo-1585032226651-759b368d7246?auto=format&fit=crop&w=900&q=80'
  WHEN '【种子数据】root4的牛油九宫格' THEN 'https://images.unsplash.com/photo-1544145945-f90425340c7e?auto=format&fit=crop&w=900&q=80'
  WHEN '【种子数据】root5的炭烤拼盘' THEN 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?auto=format&fit=crop&w=900&q=80'
  ELSE d.image_url
END
WHERE p.title LIKE '【种子数据】root%';

SELECT id,title,cover_image FROM food_post WHERE title LIKE '【种子数据】root%' ORDER BY id;
