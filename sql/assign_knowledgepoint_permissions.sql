-- ==========================================
-- 知识点详情页权限分配脚本
-- 说明：将知识点详情页权限分配给指定角色
-- 注意：需要先执行 knowledgepoint_detail_menu.sql 创建菜单后再执行此脚本
-- ==========================================

-- role_id=1: 超级管理员
-- role_id=2: 普通角色
-- role_id=100: 教师
-- role_id=101: 学生

-- 首先查询知识点相关的所有菜单ID
SELECT 
    menu_id,
    menu_name,
    parent_id,
    path,
    visible,
    perms
FROM sys_menu
WHERE menu_name LIKE '%知识点%' OR path LIKE '%knowledge%'
ORDER BY parent_id, order_num;

-- ==========================================
-- 为各角色分配知识点详情页权限
-- ==========================================

-- 为 role_id=1 (超级管理员) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE (m.menu_name LIKE '%知识点%' OR m.path LIKE '%knowledge%' OR m.perms LIKE '%knowledgepoint%')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

-- 为 role_id=2 (普通角色) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, m.menu_id
FROM sys_menu m
WHERE (m.menu_name LIKE '%知识点%' OR m.path LIKE '%knowledge%' OR m.perms LIKE '%knowledgepoint%')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 2 AND rm.menu_id = m.menu_id
  );

-- 为 role_id=100 (教师) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 100, m.menu_id
FROM sys_menu m
WHERE (m.menu_name LIKE '%知识点%' OR m.path LIKE '%knowledge%' OR m.perms LIKE '%knowledgepoint%')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 100 AND rm.menu_id = m.menu_id
  );

-- 为 role_id=101 (学生) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 101, m.menu_id
FROM sys_menu m
WHERE (m.menu_name LIKE '%知识点%' OR m.path LIKE '%knowledge%' OR m.perms LIKE '%knowledgepoint%')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 101 AND rm.menu_id = m.menu_id
  );

-- ==========================================
-- 验证分配结果
-- ==========================================

-- 查看每个角色的知识点相关权限
SELECT 
    r.role_id,
    r.role_name,
    r.role_key,
    COUNT(m.menu_id) as permission_count,
    GROUP_CONCAT(m.menu_name ORDER BY m.menu_id SEPARATOR ', ') as permissions
FROM sys_role r
INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_id IN (1, 2, 100, 101)
  AND (m.menu_name LIKE '%知识点%' OR m.path LIKE '%knowledge%' OR m.perms LIKE '%knowledgepoint%')
GROUP BY r.role_id, r.role_name, r.role_key
ORDER BY r.role_id;

-- 详细查看各角色的知识点权限明细
SELECT 
    r.role_id,
    r.role_name,
    m.menu_id,
    m.menu_name,
    m.path,
    m.visible,
    m.menu_type,
    m.perms
FROM sys_role r
INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_id IN (1, 2, 100, 101)
  AND (m.menu_name LIKE '%知识点%' OR m.path LIKE '%knowledge%' OR m.perms LIKE '%knowledgepoint%')
ORDER BY r.role_id, m.parent_id, m.order_num;

-- ==========================================
-- 回滚脚本（如需删除权限分配）
-- ==========================================

-- 删除所有角色的知识点详情页权限
/*
DELETE rm FROM sys_role_menu rm
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE m.menu_name = '知识点详情'
  AND rm.role_id IN (1, 2, 100, 101);
*/
