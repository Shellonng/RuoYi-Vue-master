-- 查询"作业或考试"菜单授权给了哪些角色

-- 1. 查找作业或考试相关菜单
SELECT 
    m.menu_id,
    m.menu_name,
    m.parent_id,
    m.menu_type,
    m.perms,
    m.path,
    m.component
FROM sys_menu m
WHERE m.menu_name LIKE '%作业%' OR m.menu_name LIKE '%考试%' OR m.perms LIKE '%assignment%'
ORDER BY m.menu_id;

-- 2. 查询哪些角色拥有这些菜单权限
SELECT 
    r.role_id,
    r.role_name,
    r.role_key,
    r.status,
    m.menu_id,
    m.menu_name,
    m.menu_type,
    m.perms
FROM sys_role r
INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE (m.menu_name LIKE '%作业%' OR m.menu_name LIKE '%考试%' OR m.perms LIKE '%assignment%')
ORDER BY r.role_id, m.menu_id;

-- 3. 统计每个角色拥有的作业或考试相关权限数量
SELECT 
    r.role_id,
    r.role_name,
    r.role_key,
    r.status,
    COUNT(m.menu_id) as permission_count,
    GROUP_CONCAT(m.menu_name SEPARATOR ', ') as permissions
FROM sys_role r
INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE (m.menu_name LIKE '%作业%' OR m.menu_name LIKE '%考试%' OR m.perms LIKE '%assignment%')
GROUP BY r.role_id, r.role_name, r.role_key, r.status
ORDER BY r.role_id;

-- 4. 查询没有分配作业或考试权限的角色
SELECT 
    r.role_id,
    r.role_name,
    r.role_key,
    r.status
FROM sys_role r
WHERE r.role_id NOT IN (
    SELECT DISTINCT rm.role_id
    FROM sys_role_menu rm
    INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
    WHERE (m.menu_name LIKE '%作业%' OR m.menu_name LIKE '%考试%' OR m.perms LIKE '%assignment%')
)
ORDER BY r.role_id;

-- 5. 查询拥有作业或考试权限的用户（通过角色）
SELECT DISTINCT
    u.user_id,
    u.user_name,
    u.nick_name,
    u.email,
    r.role_name,
    r.role_key
FROM sys_user u
INNER JOIN sys_user_role ur ON u.user_id = ur.user_id
INNER JOIN sys_role r ON ur.role_id = r.role_id
INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE (m.menu_name LIKE '%作业%' OR m.menu_name LIKE '%考试%' OR m.perms LIKE '%assignment%')
  AND u.del_flag = '0'
  AND r.status = '0'
ORDER BY u.user_id, r.role_id;
