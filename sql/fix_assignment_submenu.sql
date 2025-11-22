-- 修正任务管理子菜单配置
-- 确保父菜单是Layout,子菜单路径和组件配置正确

-- 1. 先检查并修正父菜单(任务管理)的配置
-- 父菜单必须是目录类型(M),component为空或ParentView,才能显示子菜单
UPDATE sys_menu 
SET menu_type = 'M',       -- 必须是目录类型
    component = '',        -- 目录类型component为空
    path = 'assignment',   -- 父菜单路径不带斜杠
    is_frame = 1           -- 非外链
WHERE menu_id = 5017;

-- 2. 删除旧的子菜单权限和菜单(如果存在)
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu WHERE menu_name IN ('考试管理', '作业管理') AND parent_id = 5017
);
DELETE FROM sys_menu WHERE menu_name IN ('考试管理', '作业管理') AND parent_id = 5017;

-- 3. 插入考试管理子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (
    '考试管理',           -- 菜单名称
    5017,                -- 父菜单ID(任务管理)
    1,                   -- 显示顺序
    'exam',              -- 路由路径(相对路径,不带/)
    'assignment/exam/index',  -- 组件路径(会自动拼接@/views/)
    1,                   -- 是否外链(1否 0是)
    0,                   -- 是否缓存(0缓存 1不缓存)
    'C',                 -- 菜单类型(M目录 C菜单 F按钮)
    '0',                 -- 菜单状态(0显示 1隐藏)
    '0',                 -- 菜单状态(0正常 1停用)
    'system:assignment:list', -- 权限标识
    'education',         -- 菜单图标
    'admin',             -- 创建者
    NOW(),               -- 创建时间
    '',                  -- 更新者
    NULL,                -- 更新时间
    '考试管理菜单'        -- 备注
);

-- 4. 插入作业管理子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (
    '作业管理',           -- 菜单名称
    5017,                -- 父菜单ID(任务管理)
    2,                   -- 显示顺序
    'homework',          -- 路由路径(相对路径,不带/)
    'assignment/homework/index',  -- 组件路径(会自动拼接@/views/)
    1,                   -- 是否外链(1否 0是)
    0,                   -- 是否缓存(0缓存 1不缓存)
    'C',                 -- 菜单类型(M目录 C菜单 F按钮)
    '0',                 -- 菜单状态(0显示 1隐藏)
    '0',                 -- 菜单状态(0正常 1停用)
    'system:assignment:list', -- 权限标识
    'form',              -- 菜单图标
    'admin',             -- 创建者
    NOW(),               -- 创建时间
    '',                  -- 更新者
    NULL,                -- 更新时间
    '作业管理菜单'        -- 备注
);

-- 5. 为所有拥有任务管理权限的角色授予子菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, m.menu_id 
FROM sys_role_menu rm
CROSS JOIN sys_menu m
WHERE rm.menu_id = 5017  -- 拥有任务管理权限的角色
AND m.menu_name IN ('考试管理', '作业管理') 
AND m.parent_id = 5017
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm2 
    WHERE rm2.role_id = rm.role_id 
    AND rm2.menu_id = m.menu_id
);

-- 6. 查看配置结果
SELECT 
    m.menu_id,
    m.menu_name,
    m.parent_id,
    m.path,
    m.component,
    m.menu_type,
    m.is_frame,
    p.menu_name AS parent_name,
    p.component AS parent_component
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.menu_id
WHERE m.menu_id = 5017 OR m.parent_id = 5017
ORDER BY m.parent_id, m.order_num;
