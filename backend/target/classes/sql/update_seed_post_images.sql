USE food_forum;
SET NAMES utf8mb4;

UPDATE food_post p
LEFT JOIN food_category c ON c.id = p.category_id
SET p.cover_image = CASE
  WHEN c.name = '热干面' THEN 'https://images.unsplash.com/photo-1555126634-323283e090fa?auto=format&fit=crop&w=1200&q=80'
  WHEN c.name = '火锅'   THEN 'https://images.unsplash.com/photo-1514516345957-556ca7f68fc9?auto=format&fit=crop&w=1200&q=80'
  WHEN c.name = '烧烤'   THEN 'https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?auto=format&fit=crop&w=1200&q=80'
  WHEN c.name = '甜品'   THEN 'https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=1200&q=80'
  WHEN c.name = '小吃'   THEN 'https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&w=1200&q=80'
  ELSE p.cover_image
END
WHERE p.title LIKE '【种子数据】root%';

UPDATE food_image fi
JOIN food_post p ON p.id = fi.post_id
SET fi.image_url = p.cover_image
WHERE p.title LIKE '【种子数据】root%';

UPDATE food_dish d
JOIN food_post p ON p.id = d.post_id
SET d.image_url = CASE
  WHEN p.title = '【种子数据】root3的江城热干面' THEN 'https://images.unsplash.com/photo-1585032226651-759b368d7246?auto=format&fit=crop&w=1200&q=80'
  WHEN p.title = '【种子数据】root4的牛油九宫格' THEN 'https://images.unsplash.com/photo-1544145945-f90425340c7e?auto=format&fit=crop&w=1200&q=80'
  WHEN p.title = '【种子数据】root5的炭烤拼盘' THEN 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?auto=format&fit=crop&w=1200&q=80'
  ELSE d.image_url
END
WHERE p.title LIKE '【种子数据】root%';

SELECT p.id, p.title, p.cover_image
FROM food_post p
WHERE p.title LIKE '【种子数据】root%'
ORDER BY p.id;
