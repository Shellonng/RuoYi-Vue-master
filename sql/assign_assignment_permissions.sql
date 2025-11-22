-- 将作业或考试菜单权限分配给指定角色
-- role_id=1: 超级管理员
-- role_id=2: 普通角色
-- role_id=100: 教师
-- role_id=101: 学生

-- 首先查询作业或考试相关的所有菜单ID
-- 注意：需要先执行 assignmentMenu.sql 创建菜单后再执行此脚本

-- 为 role_id=1 (超级管理员) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE (m.menu_name LIKE '%作业%' OR m.menu_name LIKE '%考试%' OR m.perms LIKE '%assignment%')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

-- 为 role_id=2 (普通角色) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, m.menu_id
FROM sys_menu m
WHERE (m.menu_name LIKE '%作业%' OR m.menu_name LIKE '%考试%' OR m.perms LIKE '%assignment%')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 2 AND rm.menu_id = m.menu_id
  );

-- 为 role_id=100 (教师) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 100, m.menu_id
FROM sys_menu m
WHERE (m.menu_name LIKE '%作业%' OR m.menu_name LIKE '%考试%' OR m.perms LIKE '%assignment%')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 100 AND rm.menu_id = m.menu_id
  );

-- 为 role_id=101 (学生) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 101, m.menu_id
FROM sys_menu m
WHERE (m.menu_name LIKE '%作业%' OR m.menu_name LIKE '%考试%' OR m.perms LIKE '%assignment%')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 101 AND rm.menu_id = m.menu_id
  );

-- 验证分配结果
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
  AND (m.menu_name LIKE '%作业%' OR m.menu_name LIKE '%考试%' OR m.perms LIKE '%assignment%')
GROUP BY r.role_id, r.role_name, r.role_key
ORDER BY r.role_id;
