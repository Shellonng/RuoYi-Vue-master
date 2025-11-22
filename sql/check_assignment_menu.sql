-- 查询任务管理菜单及其子菜单的当前配置
SELECT 
    m.menu_id,
    m.menu_name,
    m.parent_id,
    m.path,
    m.component,
    m.menu_type,
    m.is_frame,
    m.visible,
    m.status,
    m.order_num,
    p.menu_name AS parent_name,
    p.component AS parent_component,
    p.menu_type AS parent_type
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.menu_id
WHERE m.menu_id = 5017 OR m.parent_id = 5017
ORDER BY m.parent_id, m.order_num;

-- 查询管理员角色对这些菜单的权限
SELECT 
    rm.role_id,
    r.role_name,
    m.menu_id,
    m.menu_name,
    m.parent_id
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.role_id
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE m.menu_id = 5017 OR m.parent_id = 5017
ORDER BY rm.role_id, m.parent_id, m.menu_id;
