USE food_forum;
SET NAMES utf8mb4;

UPDATE merchant_application ma
JOIN user u ON u.id = ma.user_id
SET ma.business_license = CASE u.username
  WHEN 'root3' THEN '/uploads/seed-assets/license_root3.svg'
  WHEN 'root4' THEN '/uploads/seed-assets/license_root4.svg'
  WHEN 'root5' THEN '/uploads/seed-assets/license_root5.svg'
  ELSE ma.business_license
END
WHERE u.username IN ('root3','root4','root5');

UPDATE merchant_profile mp
JOIN user u ON u.id = mp.user_id
SET mp.business_license_url = CASE u.username
      WHEN 'root3' THEN '/uploads/seed-assets/license_root3.svg'
      WHEN 'root4' THEN '/uploads/seed-assets/license_root4.svg'
      WHEN 'root5' THEN '/uploads/seed-assets/license_root5.svg'
      ELSE mp.business_license_url
    END,
    mp.food_safety_cert_url = CASE u.username
      WHEN 'root3' THEN '/uploads/seed-assets/health_root3.svg'
      WHEN 'root4' THEN '/uploads/seed-assets/health_root4.svg'
      WHEN 'root5' THEN '/uploads/seed-assets/health_root5.svg'
      ELSE mp.food_safety_cert_url
    END
WHERE u.username IN ('root3','root4','root5');

UPDATE chef_info ci
JOIN merchant_profile mp ON mp.id = ci.merchant_profile_id
JOIN user u ON u.id = mp.user_id
SET ci.avatar_url = CASE
  WHEN u.username='root3' AND ci.chef_name='周师傅' THEN '/uploads/seed-assets/chef_root3_zhou.svg'
  WHEN u.username='root3' AND ci.chef_name='刘师傅' THEN '/uploads/seed-assets/chef_root3_liu.svg'
  WHEN u.username='root4' AND ci.chef_name='陈师傅' THEN '/uploads/seed-assets/chef_root4_chen.svg'
  WHEN u.username='root4' AND ci.chef_name='何师傅' THEN '/uploads/seed-assets/chef_root4_he.svg'
  WHEN u.username='root5' AND ci.chef_name='黄师傅' THEN '/uploads/seed-assets/chef_root5_huang.svg'
  WHEN u.username='root5' AND ci.chef_name='杨师傅' THEN '/uploads/seed-assets/chef_root5_yang.svg'
  ELSE ci.avatar_url
END
WHERE u.username IN ('root3','root4','root5');

UPDATE food_post p
JOIN user u ON u.id = p.author_user_id
SET p.cover_image = CASE u.username
  WHEN 'root3' THEN '/uploads/seed-assets/post_root3_hotdrynoodles.svg'
  WHEN 'root4' THEN '/uploads/seed-assets/post_root4_hotpot.svg'
  WHEN 'root5' THEN '/uploads/seed-assets/post_root5_bbq.svg'
  ELSE p.cover_image
END
WHERE p.title LIKE '【种子数据】root%' AND u.username IN ('root3','root4','root5');

UPDATE food_image fi
JOIN food_post p ON p.id = fi.post_id
JOIN user u ON u.id = p.author_user_id
SET fi.image_url = p.cover_image
WHERE p.title LIKE '【种子数据】root%' AND u.username IN ('root3','root4','root5');

UPDATE food_dish d
JOIN food_post p ON p.id = d.post_id
JOIN user u ON u.id = p.author_user_id
SET d.image_url = CASE u.username
  WHEN 'root3' THEN '/uploads/seed-assets/dish_root3_hotdrynoodles.svg'
  WHEN 'root4' THEN '/uploads/seed-assets/dish_root4_hotpot.svg'
  WHEN 'root5' THEN '/uploads/seed-assets/dish_root5_bbq.svg'
  ELSE d.image_url
END
WHERE p.title LIKE '【种子数据】root%' AND u.username IN ('root3','root4','root5');

SELECT u.username, mp.business_license_url, mp.food_safety_cert_url
FROM merchant_profile mp
JOIN user u ON u.id=mp.user_id
WHERE u.username IN ('root3','root4','root5');

SELECT u.username, ci.chef_name, ci.avatar_url
FROM chef_info ci
JOIN merchant_profile mp ON mp.id=ci.merchant_profile_id
JOIN user u ON u.id=mp.user_id
WHERE u.username IN ('root3','root4','root5')
ORDER BY u.username, ci.id;

SELECT u.username, p.title, p.cover_image
FROM food_post p
JOIN user u ON u.id=p.author_user_id
WHERE p.title LIKE '【种子数据】root%' AND u.username IN ('root3','root4','root5')
ORDER BY p.id;
