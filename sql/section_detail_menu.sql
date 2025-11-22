-- =============================================
-- 小节详情页菜单配置
-- 说明：小节详情与课程详情同级，都在根路径下
-- =============================================

-- 第一步：查询现有菜单结构（可选，用于确认）
-- SELECT menu_id, menu_name, parent_id, path FROM sys_menu WHERE menu_name IN ('课程管理', '课程详情', '小节详情');
-- 课程管理: menu_id=2000, parent_id=0, path=course
-- 课程详情: menu_id=5009, parent_id=0, path=detail/:id
-- 小节详情: 将要创建，parent_id=0, path=section/:courseId/:sectionId

-- 第二步：插入小节详情页菜单
-- 注意：parent_id 设置为 0，表示与课程详情同级
INSERT INTO sys_menu (
    menu_name,
    parent_id,      -- 设置为 0，与课程详情同级
    order_num,
    path,
    component,
    is_frame,
    is_cache,
    menu_type,
    visible,        -- '1' 表示隐藏，不在侧边栏显示
    status,
    perms,
    icon,
    create_by,
    create_time,
    remark
) VALUES (
    '小节详情',
    0,               -- ★ 与课程详情同级，parent_id=0
    3,
    'section/:courseId/:sectionId',  -- 动态参数路由
    'course/section',                -- 对应 views/course/section.vue
    1,
    0,
    'C',
    '1',             -- ★ 关键：隐藏菜单，不在侧边栏显示
    '0',
    'course:section:query',
    '',
    'admin',
    NOW(),
    '小节详情页面，从课程详情页跳转'
);

-- 验证插入结果
SELECT 
    menu_id,
    menu_name,
    parent_id,
    path,
    component,
    visible,
    perms
FROM sys_menu 
WHERE menu_name = '小节详情'
ORDER BY create_time DESC
LIMIT 1;

-- =============================================
-- 使用说明
-- =============================================
-- 1. 小节详情菜单与课程详情同级（parent_id=0）
--
-- 2. 直接执行插入语句（不需要修改parent_id）
--
-- 3. 重启后端服务
--
-- 4. 前端清除缓存，重新登录
--
-- 5. 跳转代码示例：
--    this.$router.push({
--      path: `/section/${courseId}/${sectionId}`
--    });
--
-- 6. 完整URL示例：
--    http://localhost/section/30/5
-- =============================================
