-- =============================================
-- 学生管理模块菜单配置脚本
-- 功能：创建三层菜单结构（目录 -> 子菜单 -> 权限按钮）
-- 结构：学生管理(M) -> 选课管理(C)/班级管理(C) -> 各自的权限按钮(F)
-- 版本：v2.0
-- 日期：2025-11-25
-- =============================================

-- ============================================= 
-- 清理：删除旧的学生管理菜单及其所有子菜单和权限
-- =============================================

-- 1. 先删除角色菜单关联（使用临时表避免子查询问题）
DELETE FROM sys_role_menu 
WHERE menu_id IN (
    SELECT menu_id FROM (
        SELECT m.menu_id 
        FROM sys_menu m
        WHERE m.menu_name = '学生管理' AND m.parent_id = 0
        UNION
        SELECT m.menu_id 
        FROM sys_menu m
        WHERE m.parent_id IN (
            SELECT menu_id FROM sys_menu WHERE menu_name = '学生管理' AND parent_id = 0
        )
        UNION
        SELECT m.menu_id 
        FROM sys_menu m
        WHERE m.parent_id IN (
            SELECT m2.menu_id FROM sys_menu m2 
            WHERE m2.parent_id IN (
                SELECT menu_id FROM sys_menu WHERE menu_name = '学生管理' AND parent_id = 0
            )
        )
    ) AS temp
);

-- 2. 删除第三层按钮（使用临时表）
DELETE FROM sys_menu 
WHERE menu_id IN (
    SELECT menu_id FROM (
        SELECT m.menu_id 
        FROM sys_menu m
        WHERE m.parent_id IN (
            SELECT m2.menu_id FROM sys_menu m2 
            WHERE m2.parent_id IN (
                SELECT menu_id FROM sys_menu WHERE menu_name = '学生管理' AND parent_id = 0
            )
        )
    ) AS temp
);

-- 3. 删除第二层子菜单（使用临时表）
DELETE FROM sys_menu 
WHERE menu_id IN (
    SELECT menu_id FROM (
        SELECT m.menu_id 
        FROM sys_menu m
        WHERE m.parent_id IN (
            SELECT menu_id FROM sys_menu WHERE menu_name = '学生管理' AND parent_id = 0
        )
    ) AS temp
);

-- 4. 删除第一层目录
DELETE FROM sys_menu WHERE menu_name = '学生管理' AND parent_id = 0;

-- ============================================= 
-- 第一层：创建学生管理目录
-- =============================================
INSERT INTO sys_menu (
    menu_name,
    parent_id,
    order_num,
    path,
    component,
    is_frame,
    is_cache,
    menu_type,
    visible,
    status,
    perms,
    icon,
    create_by,
    create_time,
    remark
) VALUES (
    '学生管理',
    0,                      -- 顶级菜单
    4,                      -- 排序（在任务管理之后）
    'student',
    NULL,                   -- 目录没有组件
    1,
    0,
    'M',                    -- M表示目录
    '0',                    -- 显示在侧边栏
    '0',
    '',
    'peoples',              -- 使用用户组图标
    'admin',
    NOW(),
    '学生管理目录'
);

-- 获取刚插入的学生管理目录ID
SET @student_menu_id = LAST_INSERT_ID();

-- ============================================= 
-- 第二层：创建选课管理子菜单
-- =============================================
INSERT INTO sys_menu (
    menu_name,
    parent_id,
    order_num,
    path,
    component,
    is_frame,
    is_cache,
    menu_type,
    visible,
    status,
    perms,
    icon,
    create_by,
    create_time,
    remark
) VALUES (
    '选课管理',
    @student_menu_id,       -- 父菜单为学生管理
    1,
    'enrollment',
    'student/enrollment/index',  -- 指向选课管理页面
    1,
    0,
    'C',                    -- C表示菜单
    '0',
    '0',
    'system:enrollment:list',
    'education',
    'admin',
    NOW(),
    '选课管理菜单'
);

-- 获取选课管理菜单ID
SET @enrollment_menu_id = LAST_INSERT_ID();

-- ============================================= 
-- 第三层：创建选课管理的权限按钮
-- =============================================
INSERT INTO sys_menu (
    menu_name,
    parent_id,
    order_num,
    path,
    component,
    is_frame,
    is_cache,
    menu_type,
    visible,
    status,
    perms,
    icon,
    create_by,
    create_time,
    remark
) VALUES 
-- 选课管理-查询
(
    '选课查询',
    @enrollment_menu_id,    -- 父菜单为选课管理
    1,
    '#',
    '',
    1,
    0,
    'F',                    -- F表示按钮
    '0',
    '0',
    'system:enrollment:query',
    '#',
    'admin',
    NOW(),
    ''
),
-- 选课管理-新增
(
    '选课新增',
    @enrollment_menu_id,
    2,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'system:enrollment:add',
    '#',
    'admin',
    NOW(),
    ''
),
-- 选课管理-修改
(
    '选课修改',
    @enrollment_menu_id,
    3,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'system:enrollment:edit',
    '#',
    'admin',
    NOW(),
    ''
),
-- 选课管理-删除
(
    '选课删除',
    @enrollment_menu_id,
    4,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'system:enrollment:remove',
    '#',
    'admin',
    NOW(),
    ''
),
-- 选课管理-导出
(
    '选课导出',
    @enrollment_menu_id,
    5,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'system:enrollment:export',
    '#',
    'admin',
    NOW(),
    ''
);

-- ============================================= 
-- 第二层：创建班级管理子菜单
-- =============================================
INSERT INTO sys_menu (
    menu_name,
    parent_id,
    order_num,
    path,
    component,
    is_frame,
    is_cache,
    menu_type,
    visible,
    status,
    perms,
    icon,
    create_by,
    create_time,
    remark
) VALUES (
    '班级管理',
    @student_menu_id,       -- 父菜单为学生管理
    2,
    'class',
    'student/class/index',  -- 指向班级管理页面
    1,
    0,
    'C',                    -- C表示菜单
    '0',
    '0',
    'system:class:list',
    'peoples',
    'admin',
    NOW(),
    '班级管理菜单'
);

-- 获取班级管理菜单ID
SET @class_menu_id = LAST_INSERT_ID();

-- ============================================= 
-- 第三层：创建班级管理的权限按钮
-- =============================================
INSERT INTO sys_menu (
    menu_name,
    parent_id,
    order_num,
    path,
    component,
    is_frame,
    is_cache,
    menu_type,
    visible,
    status,
    perms,
    icon,
    create_by,
    create_time,
    remark
) VALUES 
-- 班级管理-查询
(
    '班级查询',
    @class_menu_id,         -- 父菜单为班级管理
    1,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'system:class:query',
    '#',
    'admin',
    NOW(),
    ''
),
-- 班级管理-新增
(
    '班级新增',
    @class_menu_id,
    2,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'system:class:add',
    '#',
    'admin',
    NOW(),
    ''
),
-- 班级管理-修改
(
    '班级修改',
    @class_menu_id,
    3,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'system:class:edit',
    '#',
    'admin',
    NOW(),
    ''
),
-- 班级管理-删除
(
    '班级删除',
    @class_menu_id,
    4,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'system:class:remove',
    '#',
    'admin',
    NOW(),
    ''
),
-- 班级管理-导出
(
    '班级导出',
    @class_menu_id,
    5,
    '#',
    '',
    1,
    0,
    'F',
    '0',
    '0',
    'system:class:export',
    '#',
    'admin',
    NOW(),
    ''
);

-- ============================================= 
-- 权限分配：为角色ID 101分配所有菜单和按钮权限
-- =============================================

-- 先删除可能存在的旧权限关联
DELETE FROM sys_role_menu 
WHERE role_id = 101 
AND menu_id IN (
    SELECT menu_id FROM (
        SELECT m.menu_id 
        FROM sys_menu m
        WHERE m.menu_name = '学生管理'
           OR m.menu_name = '选课管理'
           OR m.menu_name = '班级管理'
           OR m.menu_name IN ('选课查询', '选课新增', '选课修改', '选课删除', '选课导出',
                              '班级查询', '班级新增', '班级修改', '班级删除', '班级导出')
    ) AS temp
);

-- 插入新的权限关联
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 101, menu_id FROM sys_menu 
WHERE menu_name = '学生管理'
   OR menu_name = '选课管理'
   OR menu_name = '班级管理'
   OR menu_name IN ('选课查询', '选课新增', '选课修改', '选课删除', '选课导出',
                     '班级查询', '班级新增', '班级修改', '班级删除', '班级导出');

-- =============================================
-- 验证查询
-- =============================================

-- 查看完整菜单树结构
SELECT 
    m.menu_id,
    m.menu_name,
    m.parent_id,
    m.order_num,
    m.path,
    m.component,
    CASE m.menu_type 
        WHEN 'M' THEN '目录'
        WHEN 'C' THEN '菜单'
        WHEN 'F' THEN '按钮'
    END AS '类型',
    m.perms AS '权限标识',
    m.icon
FROM sys_menu m
WHERE m.menu_name = '学生管理'
   OR m.parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '学生管理' AND parent_id = 0)
   OR m.parent_id IN (
       SELECT menu_id FROM sys_menu 
       WHERE parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '学生管理' AND parent_id = 0)
   )
ORDER BY 
    CASE WHEN m.parent_id = 0 THEN 1
         WHEN m.menu_type = 'C' THEN 2
         ELSE 3 END,
    m.parent_id, 
    m.order_num;

-- 查看权限分配（预期结果：13条记录 = 1目录 + 2菜单 + 10按钮）
SELECT 
    r.role_name AS '角色',
    m.menu_name AS '菜单名称',
    CASE m.menu_type 
        WHEN 'M' THEN '目录'
        WHEN 'C' THEN '菜单'
        WHEN 'F' THEN '按钮'
    END AS '类型',
    m.perms AS '权限标识'
FROM sys_role r
JOIN sys_role_menu rm ON r.role_id = rm.role_id
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE r.role_id = 101 
  AND (m.menu_name = '学生管理' 
       OR m.menu_name IN ('选课管理', '班级管理')
       OR m.parent_id IN (
           SELECT menu_id FROM sys_menu 
           WHERE menu_name IN ('选课管理', '班级管理')
       ))
ORDER BY m.parent_id, m.order_num;

-- =============================================
-- 预期菜单结构（三层）：
-- =============================================
-- 学生管理 (M - 目录)
--   ├── 选课管理 (C - 菜单)
--   │     ├── 选课查询 (F - 按钮)
--   │     ├── 选课新增 (F - 按钮)
--   │     ├── 选课修改 (F - 按钮)
--   │     ├── 选课删除 (F - 按钮)
--   │     └── 选课导出 (F - 按钮)
--   └── 班级管理 (C - 菜单)
--         ├── 班级查询 (F - 按钮)
--         ├── 班级新增 (F - 按钮)
--         ├── 班级修改 (F - 按钮)
--         ├── 班级删除 (F - 按钮)
--         └── 班级导出 (F - 按钮)
