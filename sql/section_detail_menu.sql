-- =============================================
-- 小节详情页菜单配置
-- 说明：为课程详情页添加隐藏的小节详情子路由
-- =============================================

-- 第一步：查询课程详情页的菜单ID（需要先确认）
-- SELECT menu_id, menu_name, path FROM sys_menu WHERE menu_name = '课程详情';
-- 假设课程详情的 menu_id = 2001（请根据实际情况修改）

-- 第二步：插入小节详情页菜单
-- 注意：parent_id 需要改成实际的课程详情菜单ID
INSERT INTO sys_menu (
    menu_name,
    parent_id,      -- 课程详情的 menu_id，需要根据实际情况修改
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
    2001,            -- ★ 重要：改成实际的课程详情菜单ID
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
-- 1. 先查询课程详情的菜单ID：
--    SELECT menu_id FROM sys_menu WHERE menu_name = '课程详情';
--
-- 2. 将上面查询到的 menu_id 替换脚本中的 parent_id 值
--
-- 3. 执行插入语句
--
-- 4. 重启后端服务
--
-- 5. 前端清除缓存，重新登录
--
-- 6. 跳转代码示例：
--    this.$router.push({
--      path: `/course/detail/${courseId}/section/${sectionId}`
--    });
--
-- 7. 或使用命名路由：
--    this.$router.push({
--      name: 'SectionDetail',
--      params: { courseId: courseId, sectionId: sectionId }
--    });
-- =============================================
