-- ==========================================
-- 知识点详情页一键配置脚本（菜单+权限）
-- 说明：一次性完成菜单创建和权限分配
-- 使用：执行本脚本 → 重启后端 → 清除浏览器缓存 → 重新登录
-- ==========================================

-- ==========================================
-- 步骤1：查询知识点管理菜单
-- ==========================================
SELECT 
    menu_id,
    menu_name,
    path,
    parent_id,
    visible
FROM sys_menu
WHERE menu_name LIKE '%知识点%' OR path LIKE '%knowledge%'
ORDER BY parent_id, order_num;

-- ==========================================
-- 步骤2：创建知识点详情页菜单
-- ==========================================
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
    update_by,
    update_time,
    remark
) VALUES (
    '知识点详情',
    0,
    99,
    'knowledgepoint/detail/:id',
    'knowledgepoint/detail',
    1,
    0,
    'C',
    '1',
    '0',
    'knowledgepoint:knowledgepoint:query',
    '#',
    'admin',
    sysdate(),
    '',
    NULL,
    '知识点详情页（独立一级菜单，隐藏，通过代码跳转访问）'
);

-- 获取刚插入的菜单ID
SET @detail_menu_id = LAST_INSERT_ID();

-- 显示插入结果
SELECT 
    menu_id,
    menu_name,
    parent_id,
    path,
    component,
    visible,
    perms
FROM sys_menu
WHERE menu_id = @detail_menu_id;

-- ==========================================
-- 步骤3：为各角色分配权限
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
-- 步骤4：验证配置结果
-- ==========================================

-- 查看知识点相关菜单
SELECT 
    menu_id,
    menu_name,
    parent_id,
    order_num,
    path,
    component,
    visible,
    menu_type,
    perms
FROM sys_menu
WHERE menu_name LIKE '%知识点%' OR path LIKE '%knowledge%'
ORDER BY parent_id, order_num;

-- 查看各角色的知识点权限统计
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

-- 查看详细的角色权限明细
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
-- 配置说明
-- ==========================================

/*
配置内容：
-----------
1. 菜单名称：知识点详情
2. 父菜单ID：0（独立一级菜单）
3. 排序号：99（靠后排序）
4. 路由路径：knowledgepoint/detail/:id（完整路径，动态参数）
5. 组件路径：knowledgepoint/detail
6. 可见性：隐藏（visible='1'）
7. 权限标识：knowledgepoint:knowledgepoint:query

权限分配：
-----------
- 超级管理员（role_id=1）：拥有所有知识点相关权限
- 普通角色（role_id=2）：拥有所有知识点相关权限
- 教师（role_id=100）：拥有所有知识点相关权限
- 学生（role_id=101）：拥有所有知识点相关权限

生成的路由结构：
-----------------
{
  path: "/knowledgepoint/detail/:id",
  component: Layout,
  hidden: true,  // 自动从 visible='1' 转换
  children: [
    {
      path: '',
      component: () => import('@/views/knowledgepoint/detail'),
      name: 'KnowledgePointDetail',
      meta: { 
        title: "知识点详情",
        noCache: true  // is_cache=0 转换
      }
    }
  ]
}

测试步骤：
-----------
1. ✅ 执行本SQL脚本
2. ✅ 检查验证查询结果是否正确
3. ✅ 重启后端服务（加载新的菜单数据）
4. ✅ 清除浏览器缓存，重新登录
5. ✅ 打开知识点管理页面
6. ✅ 点击"查看"按钮
7. ✅ 验证跳转到 /knowledgepoint/detail/:id 页面
8. ✅ 验证页面保留左侧菜单和顶部导航
9. ✅ 验证知识点管理菜单保持高亮状态

常见问题：
-----------
Q: 为什么要设置为独立一级菜单（parent_id=0）？
A: 设置为独立菜单可以避免依赖其他菜单。虽然是一级菜单，但通过 visible='1' 隐藏，
   不会在侧边栏显示，只能通过代码跳转访问。

Q: 如何确认权限分配成功？
A: 查看上面的验证查询结果，确保每个角色都有知识点详情权限

Q: 点击查看后显示404怎么办？
A: 1. 确认SQL执行成功
   2. 确认后端已重启
   3. 清除浏览器缓存重新登录
   4. 检查后端日志

回滚脚本：
-----------
如需删除配置，执行以下语句：

-- 删除权限分配
DELETE rm FROM sys_role_menu rm
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE m.menu_name = '知识点详情';

-- 删除菜单
DELETE FROM sys_menu WHERE menu_name = '知识点详情' AND path = 'detail/:id';
*/

-- ==========================================
-- 执行完成提示
-- ==========================================
SELECT '✅ 知识点详情页配置完成！请重启后端服务并清除浏览器缓存。' AS status;
