-- ====================================================
-- 智能分析菜单SQL
-- 包含：学情分析、视频学习分析
-- 数据库：education_platform_v1
-- ====================================================

USE education_platform_v1;

-- 第一步：清理旧的智能分析菜单（如果存在）
-- ====================================================
DELETE FROM sys_role_menu WHERE menu_id >= 6000 AND menu_id < 6100;
DELETE FROM sys_menu WHERE menu_id >= 6000 AND menu_id < 6100;

-- 第二步：插入智能分析菜单
-- ====================================================

-- 6000: 智能分析（一级目录）
INSERT INTO `sys_menu` VALUES (
  6000,                           -- menu_id
  '智能分析',                      -- menu_name
  0,                              -- parent_id（0=顶级菜单）
  6,                              -- order_num（显示顺序）
  'analysis',                     -- path
  NULL,                           -- component（目录必须为NULL）
  '',                             -- query
  '',                             -- route_name
  1,                              -- is_frame（1=否，不是外链）
  0,                              -- is_cache（0=缓存）
  'M',                            -- menu_type（M=目录）
  '0',                            -- visible（0=显示）
  '0',                            -- status（0=正常）
  '',                             -- perms（目录为空）
  'chart',                        -- icon
  'admin',                        -- create_by
  NOW(),                          -- create_time
  '',                             -- update_by
  NULL,                           -- update_time
  '智能分析目录'                   -- remark
);

-- 6001: 学情分析（二级菜单）
INSERT INTO `sys_menu` VALUES (
  6001,                           -- menu_id
  '学情分析',                      -- menu_name
  6000,                           -- parent_id（指向智能分析）
  1,                              -- order_num
  'student',                      -- path（相对路径）
  'analysis/student/index',       -- component（对应 views/analysis/student/index.vue）
  '',                             -- query
  '',                             -- route_name
  1,                              -- is_frame
  0,                              -- is_cache
  'C',                            -- menu_type（C=菜单）
  '0',                            -- visible（0=显示）
  '0',                            -- status（0=正常）
  'analysis:student:list',        -- perms
  'peoples',                      -- icon
  'admin',                        -- create_by
  NOW(),                          -- create_time
  '',                             -- update_by
  NULL,                           -- update_time
  '学情分析菜单'                   -- remark
);

-- 6002: 视频学习分析（二级菜单）
INSERT INTO `sys_menu` VALUES (
  6002,                           -- menu_id
  '视频学习分析',                  -- menu_name
  6000,                           -- parent_id（指向智能分析）
  2,                              -- order_num
  'video',                        -- path（相对路径）
  'analysis/video/index',         -- component（对应 views/analysis/video/index.vue）
  '',                             -- query
  '',                             -- route_name
  1,                              -- is_frame
  0,                              -- is_cache
  'C',                            -- menu_type（C=菜单）
  '0',                            -- visible（0=显示）
  '0',                            -- status（0=正常）
  'analysis:video:list',          -- perms
  'video-play',                   -- icon
  'admin',                        -- create_by
  NOW(),                          -- create_time
  '',                             -- update_by
  NULL,                           -- update_time
  '视频学习分析菜单'               -- remark
);

-- 第三步：为管理员角色分配菜单权限
-- ====================================================
-- 假设管理员角色ID为1，根据实际情况调整
INSERT INTO `sys_role_menu` (role_id, menu_id) VALUES (1, 6000);
INSERT INTO `sys_role_menu` (role_id, menu_id) VALUES (1, 6001);
INSERT INTO `sys_role_menu` (role_id, menu_id) VALUES (1, 6002);

-- ====================================================
-- 执行完成后，清除浏览器缓存并重新登录即可看到菜单
-- ====================================================

-- 验证SQL
SELECT menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible 
FROM sys_menu 
WHERE menu_id >= 6000 AND menu_id < 6100
ORDER BY menu_id;
