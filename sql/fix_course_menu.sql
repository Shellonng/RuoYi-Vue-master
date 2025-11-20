-- ====================================================
-- 课程管理菜单修复脚本（完整版）
-- 执行前请先备份数据库！
-- ====================================================

USE smartclassv2;

-- 第一步：清理旧数据
-- ====================================================
DELETE FROM sys_role_menu WHERE menu_id >= 5000 AND menu_id < 5100;
DELETE FROM sys_menu WHERE menu_id >= 5000 AND menu_id < 5100;

-- 第二步：插入一级菜单（目录）
-- ====================================================
-- 关键点：component 字段必须为 NULL 或 'Layout'
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component,
  query, route_name, is_frame, is_cache, menu_type, visible,
  status, perms, icon, create_by, create_time, update_by,
  update_time, remark
) VALUES (
  5000,                    -- menu_id
  '课程管理',               -- menu_name
  0,                       -- parent_id (0表示顶级菜单)
  4,                       -- order_num
  'course',                -- path (一级路由地址)
  NULL,                    -- component (🔥 必须为NULL，框架会自动使用Layout)
  '',                      -- query
  '',                      -- route_name
  1,                       -- is_frame (1=否，表示不是外链)
  0,                       -- is_cache (0=缓存)
  'M',                     -- menu_type (M=目录)
  '0',                     -- visible (0=显示)
  '0',                     -- status (0=正常)
  '',                      -- perms (目录不需要权限)
  'education',             -- icon
  'admin',                 -- create_by
  NOW(),                   -- create_time
  '',                      -- update_by
  NULL,                    -- update_time
  '课程管理目录'            -- remark
);

-- 第三步：插入二级菜单（页面）
-- ====================================================
-- 关键点：component 字段格式必须是 'course/index'
INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component,
  query, route_name, is_frame, is_cache, menu_type, visible,
  status, perms, icon, create_by, create_time, update_by,
  update_time, remark
) VALUES (
  5001,                    -- menu_id
  '课程列表',               -- menu_name
  5000,                    -- parent_id (🔥 指向课程管理目录)
  1,                       -- order_num
  'list',                  -- path (相对路径，完整路径会是 /course/list)
  'course/index',          -- component (🔥 对应 views/course/index.vue)
  '',                      -- query
  '',                      -- route_name
  1,                       -- is_frame
  0,                       -- is_cache
  'C',                     -- menu_type (C=菜单)
  '0',                     -- visible
  '0',                     -- status
  'course:course:list',    -- perms (权限标识)
  'list',                  -- icon
  'admin',                 -- create_by
  NOW(),                   -- create_time
  '',                      -- update_by
  NULL,                    -- update_time
  '课程列表菜单'            -- remark
);

-- 第四步：插入按钮权限
-- ====================================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (5002, '课程查询', 5001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:query', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (5003, '课程新增', 5001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:add', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (5004, '课程修改', 5001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:edit', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (5005, '课程删除', 5001, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:remove', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (5006, '课程导出', 5001, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:export', '#', 'admin', NOW(), '', NULL, '');

-- 第五步：给管理员角色分配权限
-- ====================================================
-- 假设管理员角色ID为1，如果不是请修改
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id >= 5000 AND menu_id < 5100;

-- 提交
COMMIT;

-- ====================================================
-- 验证菜单配置
-- ====================================================
SELECT
  '✅ 菜单配置验证' AS '检查项';

SELECT
  menu_id AS 'ID',
  menu_name AS '菜单名称',
  parent_id AS '父ID',
  menu_type AS '类型',
  path AS '路由地址',
  CASE
    WHEN component IS NULL THEN '✅ NULL (正确)'
    WHEN component = '' THEN '✅ 空字符串'
    ELSE component
  END AS '组件路径',
  perms AS '权限标识',
  visible AS '可见',
  status AS '状态'
FROM sys_menu
WHERE menu_id >= 5000 AND menu_id < 5100
ORDER BY menu_id;

-- 检查父子关系
SELECT
  '父子关系检查' AS '检查项',
  p.menu_name AS '父菜单',
  c.menu_name AS '子菜单',
  c.component AS '子菜单组件路径'
FROM sys_menu p
INNER JOIN sys_menu c ON p.menu_id = c.parent_id
WHERE p.menu_id = 5000;

-- 检查角色权限
SELECT
  '角色权限检查' AS '检查项',
  r.role_name AS '角色名',
  m.menu_name AS '菜单名'
FROM sys_role r
INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE m.menu_id >= 5000 AND m.menu_id < 5100
ORDER BY r.role_id, m.menu_id;

-- ====================================================
-- 预期结果说明
-- ====================================================
/*
预期配置：
1. menu_id=5000 (课程管理目录)
   - parent_id = 0
   - menu_type = 'M'
   - component = NULL ✅
   - path = 'course'

2. menu_id=5001 (课程列表菜单)
   - parent_id = 5000
   - menu_type = 'C'
   - component = 'course/index' ✅
   - path = 'list'
   - 完整URL = /course/list

3. 按钮权限 (5002-5006)
   - parent_id = 5001
   - menu_type = 'F'
   - component = ''
   - path = ''
*/

SELECT '
🎉 菜单配置完成！

下一步操作：
1. 重启后端 Spring Boot 应用
2. 清除浏览器缓存（Ctrl+Shift+Delete）
3. 重新登录系统
4. 点击左侧"课程管理"→"课程列表"

如果仍然全屏显示，请检查：
- 后端是否正常启动
- 浏览器控制台是否有错误（F12）
- 确认 views/course/index.vue 文件存在
' AS '提示信息';
