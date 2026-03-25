USE food_forum;
SET NAMES utf8mb4;

UPDATE food_post
SET cover_image = CASE title
  WHEN '【种子数据】root3的江城热干面' THEN 'https://image.pollinations.ai/prompt/%E7%83%AD%E5%B9%B2%E9%9D%A2%20food%20photography%20closeup?width=1200&height=800&seed=3003&nologo=true'
  WHEN '【种子数据】root4的牛油九宫格' THEN 'https://image.pollinations.ai/prompt/%E7%89%9B%E6%B2%B9%E4%B9%9D%E5%AE%AB%E6%A0%BC%E7%81%AB%E9%94%85%20food%20photography?width=1200&height=800&seed=3004&nologo=true'
  WHEN '【种子数据】root5的炭烤拼盘' THEN 'https://image.pollinations.ai/prompt/%E7%82%AD%E7%83%A4%E6%8B%BC%E7%9B%98%20bbq%20food%20photography?width=1200&height=800&seed=3005&nologo=true'
  WHEN '【种子数据】root6的巷子口生煎' THEN 'https://image.pollinations.ai/prompt/%E7%94%9F%E7%85%8E%E5%8C%85%20chinese%20street%20food?width=1200&height=800&seed=3006&nologo=true'
  WHEN '【种子数据】root7的奶油蛋糕' THEN 'https://image.pollinations.ai/prompt/%E5%A5%B6%E6%B2%B9%E8%9B%8B%E7%B3%95%20dessert%20photography?width=1200&height=800&seed=3007&nologo=true'
  WHEN '【种子数据】root8的桥头烧烤' THEN 'https://image.pollinations.ai/prompt/%E6%A1%A5%E5%A4%B4%E7%83%A7%E7%83%A4%20street%20bbq?width=1200&height=800&seed=3008&nologo=true'
  WHEN '【种子数据】root9的牛肉火锅' THEN 'https://image.pollinations.ai/prompt/%E7%89%9B%E8%82%89%E7%81%AB%E9%94%85%20food%20photo?width=1200&height=800&seed=3009&nologo=true'
  WHEN '【种子数据】root10的糯米鸡' THEN 'https://image.pollinations.ai/prompt/%E7%B3%AF%E7%B1%B3%E9%B8%A1%20traditional%20chinese%20food?width=1200&height=800&seed=3010&nologo=true'
  WHEN '【种子数据】root11的拌面档口' THEN 'https://image.pollinations.ai/prompt/%E6%8B%8C%E9%9D%A2%20chinese%20noodles%20food%20photo?width=1200&height=800&seed=3011&nologo=true'
  WHEN '【种子数据】root12的芝士挞' THEN 'https://image.pollinations.ai/prompt/%E8%8A%9D%E5%A3%AB%E6%8C%9E%20dessert%20tart?width=1200&height=800&seed=3012&nologo=true'
  WHEN '【种子数据】root13的牛板筋' THEN 'https://image.pollinations.ai/prompt/%E7%83%A4%E7%89%9B%E6%9D%BF%E7%AD%8B%20bbq%20skewer?width=1200&height=800&seed=3013&nologo=true'
  WHEN '【种子数据】root14的番茄锅底' THEN 'https://image.pollinations.ai/prompt/%E7%95%AA%E8%8C%84%E9%94%85%E5%BA%95%20hotpot%20soup?width=1200&height=800&seed=3014&nologo=true'
  WHEN '【种子数据】root15的酱香饼' THEN 'https://image.pollinations.ai/prompt/%E9%85%B1%E9%A6%99%E9%A5%BC%20chinese%20street%20snack?width=1200&height=800&seed=3015&nologo=true'
  WHEN '【种子数据】root16的杨枝甘露' THEN 'https://image.pollinations.ai/prompt/%E6%9D%A8%E6%9E%9D%E7%94%98%E9%9C%B2%20mango%20pomelo%20sago%20dessert?width=1200&height=800&seed=3016&nologo=true'
  WHEN '【种子数据】root17的牛肉面' THEN 'https://image.pollinations.ai/prompt/%E7%89%9B%E8%82%89%E9%9D%A2%20chinese%20beef%20noodle%20soup?width=1200&height=800&seed=3017&nologo=true'
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
  WHEN '【种子数据】root3的江城热干面' THEN 'https://image.pollinations.ai/prompt/%E6%8B%9B%E7%89%8C%E7%83%AD%E5%B9%B2%E9%9D%A2%20dish%20photo?width=900&height=900&seed=4013&nologo=true'
  WHEN '【种子数据】root4的牛油九宫格' THEN 'https://image.pollinations.ai/prompt/%E7%89%9B%E6%B2%B9%E7%BA%A2%E6%B1%A4%E9%94%85%20dish%20photo?width=900&height=900&seed=4014&nologo=true'
  WHEN '【种子数据】root5的炭烤拼盘' THEN 'https://image.pollinations.ai/prompt/%E7%82%AD%E7%83%A4%E6%8B%BC%E7%9B%98%20dish%20photo?width=900&height=900&seed=4015&nologo=true'
  ELSE d.image_url
END
WHERE p.title LIKE '【种子数据】root%';

SELECT id,title,cover_image FROM food_post WHERE title LIKE '【种子数据】root%' ORDER BY id;
