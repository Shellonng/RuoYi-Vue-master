-- ====================================================
-- 课程管理菜单SQL（参照系统管理标准格式）
-- 数据库：smartclassv2
-- 执行方式：复制以下SQL在数据库工具中执行
-- ====================================================

USE smartclassv2;

-- 第一步：清理旧的课程管理菜单
-- ====================================================
DELETE FROM sys_role_menu WHERE menu_id >= 5000 AND menu_id < 5100;
DELETE FROM sys_menu WHERE menu_id >= 5000 AND menu_id < 5100;

-- 第二步：插入课程管理菜单（完全参照系统管理格式）
-- ====================================================

-- 5000: 课程管理（一级目录）- 参照 menu_id=1 的格式
INSERT INTO `sys_menu` VALUES (
  5000,                           -- menu_id
  '课程管理',                      -- menu_name
  0,                              -- parent_id（0=顶级菜单）
  4,                              -- order_num（显示顺序：首页后面）
  'course',                       -- path
  NULL,                           -- component（⭐ 目录必须为NULL）
  '',                             -- query
  '',                             -- route_name
  1,                              -- is_frame（1=否，不是外链）
  0,                              -- is_cache（0=缓存）
  'M',                            -- menu_type（M=目录）
  '0',                            -- visible（0=显示）
  '0',                            -- status（0=正常）
  '',                             -- perms（目录为空）
  'education',                    -- icon
  'admin',                        -- create_by
  '2025-11-19 16:54:30',          -- create_time
  '',                             -- update_by
  NULL,                           -- update_time
  '课程管理目录'                   -- remark
);

-- 5001: 课程列表（二级菜单）- 参照 menu_id=100 的格式
INSERT INTO `sys_menu` VALUES (
  5001,                           -- menu_id
  '课程列表',                      -- menu_name
  5000,                           -- parent_id（⭐ 指向课程管理）
  1,                              -- order_num
  'list',                         -- path（相对路径）
  'course/index',                 -- component（⭐ 对应 views/course/index.vue）
  '',                             -- query
  '',                             -- route_name
  1,                              -- is_frame
  0,                              -- is_cache
  'C',                            -- menu_type（C=菜单）
  '0',                            -- visible
  '0',                            -- status
  'course:course:list',           -- perms（⭐ 权限标识）
  'list',                         -- icon
  'admin',                        -- create_by
  '2025-11-19 16:54:30',          -- create_time
  '',                             -- update_by
  NULL,                           -- update_time
  '课程列表菜单'                   -- remark
);

-- 按钮权限（参照 menu_id=1000 系列的格式）
-- ====================================================

-- 5002: 课程查询
INSERT INTO `sys_menu` VALUES (5002, '课程查询', 5001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:query', '#', 'admin', '2025-11-19 16:54:30', '', NULL, '');

-- 5003: 课程新增
INSERT INTO `sys_menu` VALUES (5003, '课程新增', 5001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:add', '#', 'admin', '2025-11-19 16:54:30', '', NULL, '');

-- 5004: 课程修改
INSERT INTO `sys_menu` VALUES (5004, '课程修改', 5001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:edit', '#', 'admin', '2025-11-19 16:54:30', '', NULL, '');

-- 5005: 课程删除
INSERT INTO `sys_menu` VALUES (5005, '课程删除', 5001, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:remove', '#', 'admin', '2025-11-19 16:54:30', '', NULL, '');

-- 5006: 课程导出
INSERT INTO `sys_menu` VALUES (5006, '课程导出', 5001, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:export', '#', 'admin', '2025-11-19 16:54:30', '', NULL, '');

-- 5007: 课程导入
INSERT INTO `sys_menu` VALUES (5007, '课程导入', 5001, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'course:course:import', '#', 'admin', '2025-11-19 16:54:30', '', NULL, '');

-- 第三步：给管理员角色分配权限
-- ====================================================
-- 假设管理员角色ID为1（如果不是请修改）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 5000);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 5001);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 5002);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 5003);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 5004);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 5005);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 5006);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 5007);

COMMIT;

-- 第四步：验证配置
-- ====================================================

-- 对比系统管理和课程管理的结构
SELECT
  '对比：系统管理 vs 课程管理' AS '说明';

SELECT
  menu_id AS 'ID',
  menu_name AS '菜单名',
  parent_id AS '父ID',
  menu_type AS '类型',
  path AS '路径',
  component AS '组件',
  perms AS '权限'
FROM sys_menu
WHERE menu_id IN (1, 100, 5000, 5001)
ORDER BY menu_id;

-- 查看完整的课程管理菜单树
SELECT
  '课程管理完整菜单树' AS '说明';

SELECT
  menu_id AS 'ID',
  CASE
    WHEN parent_id = 0 THEN CONCAT('📁 ', menu_name)
    WHEN menu_type = 'C' THEN CONCAT('  📄 ', menu_name)
    WHEN menu_type = 'F' THEN CONCAT('    🔘 ', menu_name)
  END AS '菜单树',
  menu_type AS '类型',
  path AS '路径',
  component AS '组件',
  visible AS '可见',
  status AS '状态'
FROM sys_menu
WHERE menu_id >= 5000 AND menu_id < 5100
ORDER BY parent_id, order_num;

-- 检查角色权限
SELECT
  '角色权限检查' AS '说明';

SELECT
  r.role_name AS '角色',
  COUNT(rm.menu_id) AS '课程菜单权限数'
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id AND rm.menu_id >= 5000 AND rm.menu_id < 5100
WHERE r.role_id = 1
GROUP BY r.role_id, r.role_name;

-- 预期结果验证
SELECT
  '✅ 预期结果' AS '说明',
  '一级目录component=NULL，二级菜单component=course/index' AS '关键配置';

-- ====================================================
-- 执行后的操作步骤
-- ====================================================
/*
1. ✅ 确认SQL执行成功
2. 🔄 重启后端 Spring Boot 应用
3. 🗑️ 清除浏览器缓存：
   - 按 Ctrl+Shift+Delete
   - 选择"全部时间"
   - 清除"缓存"和"Cookie"
4. 🔐 重新登录系统
5. 👀 点击左侧"课程管理" → "课程列表"
6. ✨ 应该能在布局框架内看到课程页面了！
*/

SELECT '
🎉 菜单配置完成！

配置说明：
- 一级目录（5000）：component=NULL ✅
- 二级菜单（5001）：component=course/index ✅
- 完整URL：http://localhost/course/list
- 对应文件：ruoyi-ui/src/views/course/index.vue

如果仍然全屏显示，请检查：
1. 后端是否正常重启
2. 浏览器是否完全清除缓存
3. 是否重新登录
4. views/course/index.vue 文件是否存在
5. 浏览器Console是否有错误信息
' AS '完成提示';
