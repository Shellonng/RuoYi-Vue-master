-- ==========================================
-- 查询每个菜单的权限ID及相关信息
-- ==========================================

-- 方式1：查询所有菜单的基本信息和权限标识
SELECT
    menu_id AS '菜单ID',
    menu_name AS '菜单名称',
    parent_id AS '父菜单ID',
    menu_type AS '菜单类型',
    perms AS '权限标识',
    path AS '路由地址',
    component AS '组件路径',
    visible AS '是否显示',
    status AS '状态'
FROM sys_menu
ORDER BY parent_id, order_num;


-- 方式2：树形结构显示（带父菜单信息）
SELECT
    m.menu_id AS '菜单ID',
    m.menu_name AS '菜单名称',
    m.parent_id AS '父菜单ID',
    IFNULL(p.menu_name, '顶级菜单') AS '父菜单名称',
    m.menu_type AS '菜单类型',
    m.perms AS '权限标识',
    m.path AS '路由地址',
    m.component AS '组件路径',
    CASE m.visible
        WHEN '0' THEN '显示'
        WHEN '1' THEN '隐藏'
        ELSE '未知'
    END AS '是否显示',
    CASE m.status
        WHEN '0' THEN '正常'
        WHEN '1' THEN '停用'
        ELSE '未知'
    END AS '状态'
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.menu_id
ORDER BY m.parent_id, m.order_num;


-- 方式3：只查询有权限标识的菜单
SELECT
    menu_id AS '菜单ID',
    menu_name AS '菜单名称',
    perms AS '权限标识',
    menu_type AS '类型',
    path AS '路由',
    component AS '组件'
FROM sys_menu
WHERE perms IS NOT NULL AND perms != ''
ORDER BY menu_id;


-- 方式4：按菜单类型分组查询
SELECT
    CASE menu_type
        WHEN 'M' THEN '目录'
        WHEN 'C' THEN '菜单'
        WHEN 'F' THEN '按钮'
        ELSE '未知'
    END AS '菜单类型',
    menu_id AS '菜单ID',
    menu_name AS '菜单名称',
    perms AS '权限标识',
    path AS '路由地址'
FROM sys_menu
ORDER BY menu_type, menu_id;


-- 方式5：查询课程管理相关的所有菜单和权限
SELECT
    m.menu_id AS '菜单ID',
    m.menu_name AS '菜单名称',
    m.parent_id AS '父菜单ID',
    IFNULL(p.menu_name, '顶级菜单') AS '父菜单名称',
    CASE m.menu_type
        WHEN 'M' THEN '目录'
        WHEN 'C' THEN '菜单'
        WHEN 'F' THEN '按钮'
    END AS '菜单类型',
    m.perms AS '权限标识',
    m.path AS '路由地址',
    m.component AS '组件路径',
    CASE m.visible
        WHEN '0' THEN '显示'
        WHEN '1' THEN '隐藏'
    END AS '显示状态',
    CASE m.status
        WHEN '0' THEN '正常'
        WHEN '1' THEN '停用'
    END AS '菜单状态'
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.menu_id
WHERE m.menu_name LIKE '%课程%'
   OR p.menu_name LIKE '%课程%'
   OR m.path LIKE '%course%'
ORDER BY m.parent_id, m.order_num;


-- 方式6：查询某个用户（如admin）拥有的所有菜单权限
SELECT DISTINCT
    m.menu_id AS '菜单ID',
    m.menu_name AS '菜单名称',
    m.perms AS '权限标识',
    m.menu_type AS '类型',
    m.path AS '路由'
FROM sys_menu m
INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
INNER JOIN sys_user u ON ur.user_id = u.user_id
WHERE u.user_name = 'admin'  -- 改成你要查询的用户名
ORDER BY m.menu_id;


-- 方式7：查询所有菜单权限，并显示哪些角色拥有该权限
SELECT
    m.menu_id AS '菜单ID',
    m.menu_name AS '菜单名称',
    m.perms AS '权限标识',
    GROUP_CONCAT(r.role_name SEPARATOR ', ') AS '拥有该权限的角色'
FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
LEFT JOIN sys_role r ON rm.role_id = r.role_id
GROUP BY m.menu_id, m.menu_name, m.perms
ORDER BY m.menu_id;


-- 方式8：导出格式（CSV友好）
SELECT
    menu_id,
    menu_name,
    parent_id,
    menu_type,
    perms,
    path,
    component,
    visible,
    status
FROM sys_menu
ORDER BY parent_id, order_num;
