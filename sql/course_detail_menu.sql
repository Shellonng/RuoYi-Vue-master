-- 课程详情页菜单配置
-- 将详情页配置为课程管理的隐藏子菜单

-- 假设课程管理的菜单ID是 2000（需要根据实际情况修改）
-- 先查询课程管理的菜单ID
SELECT menu_id, menu_name FROM sys_menu WHERE menu_name = '课程管理';

-- 插入课程详情页菜单（作为课程管理的子菜单）
INSERT INTO sys_menu (
    menu_name,
    parent_id,      -- 设置为课程管理的 menu_id
    order_num,
    path,
    component,
    is_frame,
    is_cache,
    menu_type,
    visible,        -- 设置为 '1' 表示隐藏，不在侧边栏显示
    status,
    perms,
    icon,
    create_by,
    create_time,
    update_by,
    update_time,
    remark
) VALUES (
    '课程详情',      -- 菜单名称
    2000,            -- 父菜单ID（需要改成实际的课程管理菜单ID）
    2,               -- 排序
    'detail/:id',    -- 路由地址（动态参数）
    'course/detail', -- 组件路径
    1,               -- 是否为外链（0是 1否）
    0,               -- 是否缓存（0缓存 1不缓存）
    'C',             -- 菜单类型（M目录 C菜单 F按钮）
    '1',             -- 显示状态（0显示 1隐藏）★ 关键：隐藏不显示
    '0',             -- 菜单状态（0正常 1停用）
    'course:course:query', -- 权限标识
    '',              -- 菜单图标
    'admin',         -- 创建者
    sysdate(),       -- 创建时间
    '',              -- 更新者
    NULL,            -- 更新时间
    '课程详情页面'    -- 备注
);

-- 查看插入结果
SELECT
    menu_id,
    menu_name,
    parent_id,
    path,
    component,
    visible,
    menu_type
FROM sys_menu
WHERE menu_name IN ('课程管理', '课程详情')
ORDER BY parent_id, order_num;
