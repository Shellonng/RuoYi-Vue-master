-- ====================================================
-- 调试课程管理菜单配置
-- 诊断为什么页面空白
-- ====================================================

USE smartclassv2;

-- 1️⃣ 查看课程管理的完整配置（重点检查 component 字段）
SELECT
  menu_id AS 'ID',
  menu_name AS '菜单名',
  parent_id AS '父ID',
  menu_type AS '类型',
  path AS '路径',
  component AS '组件',
  CASE
    WHEN menu_type = 'M' AND component IS NULL THEN '✅ 目录配置正确'
    WHEN menu_type = 'M' AND component = '' THEN '⚠️ 空字符串，建议改为NULL'
    WHEN menu_type = 'M' THEN '❌ 目录component应该是NULL'
    WHEN menu_type = 'C' AND component IS NULL THEN '❌ 菜单component不能是NULL（页面会空白）'
    WHEN menu_type = 'C' AND component = '' THEN '❌ 菜单component不能是空字符串（页面会空白）'
    WHEN menu_type = 'C' THEN CONCAT('✅ 菜单配置：', component)
    ELSE '⚠️ 其他类型'
  END AS '配置检查'
FROM sys_menu
WHERE menu_id IN (2000, 5000, 5001)
   OR (menu_name LIKE '%课程%' AND parent_id = 0)
   OR (menu_name LIKE '%课程%' AND menu_type = 'C')
ORDER BY menu_id;

-- 2️⃣ 查看所有与课程相关的菜单
SELECT
  menu_id AS 'ID',
  CASE
    WHEN parent_id = 0 THEN CONCAT('📁 ', menu_name)
    WHEN menu_type = 'C' THEN CONCAT('  📄 ', menu_name)
    WHEN menu_type = 'F' THEN CONCAT('    🔘 ', menu_name)
  END AS '菜单树',
  parent_id AS '父ID',
  menu_type AS '类型',
  path AS '路径',
  component AS '组件路径',
  visible AS '可见',
  status AS '状态'
FROM sys_menu
WHERE menu_name LIKE '%课程%'
ORDER BY parent_id, order_num;

-- 3️⃣ 检查是否有二级菜单
SELECT
  COUNT(*) AS '课程管理的子菜单数量',
  CASE
    WHEN COUNT(*) = 0 THEN '❌ 没有子菜单！需要添加二级菜单（如：课程列表）'
    ELSE '✅ 有子菜单'
  END AS '说明'
FROM sys_menu
WHERE parent_id IN (
  SELECT menu_id FROM sys_menu WHERE menu_name LIKE '%课程管理%' AND menu_type = 'M'
);

-- 4️⃣ 如果是 menu_id=2000，显示完整信息
SELECT
  '当前课程管理菜单详细信息' AS '说明',
  menu_id,
  menu_name,
  parent_id,
  order_num,
  path,
  component,
  query,
  route_name,
  is_frame,
  is_cache,
  menu_type,
  visible,
  status,
  perms,
  icon
FROM sys_menu
WHERE menu_id = 2000;

-- 5️⃣ 查找 menu_id=2000 的子菜单
SELECT
  '课程管理(2000)的子菜单' AS '说明',
  menu_id,
  menu_name,
  path,
  component,
  menu_type,
  CASE
    WHEN component IS NULL OR component = '' THEN '❌ component为空，页面会空白'
    ELSE '✅ 有component配置'
  END AS '检查结果'
FROM sys_menu
WHERE parent_id = 2000
ORDER BY order_num;

-- ====================================================
-- 诊断结果说明
-- ====================================================

SELECT '
🔍 诊断步骤：

1️⃣ 查看上面的查询结果
2️⃣ 检查关键问题：

问题A：一级目录（课程管理）的 component 字段
   ✅ 应该是：NULL 或者 空字符串（已经修复，因为现在框架正常显示）

问题B：二级菜单（课程列表）的 component 字段
   ❌ 如果是 NULL 或 空字符串 → 页面空白（最可能的原因）
   ✅ 应该是：course/index

问题C：没有二级菜单
   ❌ 如果只有一级目录，没有二级菜单 → 页面空白
   ✅ 应该至少有一个二级菜单

3️⃣ 根据上面查询结果判断：

情况1：如果二级菜单的 component 是 NULL
   → 执行下面的修复SQL A

情况2：如果根本没有二级菜单
   → 执行下面的修复SQL B

情况3：如果二级菜单的 component 路径错误
   → 执行下面的修复SQL C
' AS '诊断指南';

-- ====================================================
-- 修复SQL（根据上面诊断结果选择执行）
-- ====================================================

-- 修复SQL A：如果二级菜单 component 是 NULL，修复为 course/index
-- UPDATE sys_menu SET component = 'course/index' WHERE parent_id = 2000 AND menu_type = 'C';

-- 修复SQL B：如果没有二级菜单，添加课程列表菜单
-- INSERT INTO `sys_menu` VALUES (
--   2001,
--   '课程列表',
--   2000,
--   1,
--   'list',
--   'course/index',
--   '',
--   '',
--   1,
--   0,
--   'C',
--   '0',
--   '0',
--   'course:course:list',
--   'list',
--   'admin',
--   NOW(),
--   '',
--   NULL,
--   '课程列表菜单'
-- );
--
-- -- 给管理员分配权限
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2001);

-- 修复SQL C：如果 component 路径错误，查看现有配置后手动修复
-- SELECT menu_id, component FROM sys_menu WHERE parent_id = 2000 AND menu_type = 'C';
