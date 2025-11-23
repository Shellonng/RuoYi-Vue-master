-- ==========================================
-- 知识点详情页菜单配置脚本
-- 说明：创建知识点详情页菜单（独立一级菜单，隐藏）
-- 使用：执行本脚本后，重启后端服务，清除浏览器缓存重新登录
-- ==========================================

-- 第一步：查询现有知识点相关菜单（可选）
SELECT menu_id, menu_name, path, parent_id, visible
FROM sys_menu 
WHERE menu_name LIKE '%知识点%' OR path LIKE '%knowledge%'
ORDER BY parent_id, order_num;

-- ==========================================
-- 第二步：插入知识点详情页菜单
-- 注意：parent_id=0 表示独立的一级菜单
-- ==========================================

-- 插入知识点详情页菜单（独立一级菜单，隐藏）
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

-- ==========================================
-- 第三步：为各角色分配知识点详情页权限
-- ==========================================

-- 为 role_id=1 (超级管理员) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_name = '知识点详情'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

-- 为 role_id=2 (普通角色) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, m.menu_id
FROM sys_menu m
WHERE m.menu_name = '知识点详情'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 2 AND rm.menu_id = m.menu_id
  );

-- 为 role_id=100 (教师) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 100, m.menu_id
FROM sys_menu m
WHERE m.menu_name = '知识点详情'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 100 AND rm.menu_id = m.menu_id
  );

-- 为 role_id=101 (学生) 分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 101, m.menu_id
FROM sys_menu m
WHERE m.menu_name = '知识点详情'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm 
    WHERE rm.role_id = 101 AND rm.menu_id = m.menu_id
  );

-- ==========================================
-- 验证配置
-- ==========================================

-- 查看插入的菜单记录
SELECT
    m.menu_id,
    m.menu_name,
    m.parent_id,
    m.path,
    m.component,
    m.visible,
    m.status,
    m.perms
FROM sys_menu m
WHERE m.menu_name = '知识点详情';

-- 查看所有知识点相关菜单
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
ORDER BY parent_id, order_num;-- 验证权限分配结果
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
  AND m.menu_name LIKE '%知识点%'
GROUP BY r.role_id, r.role_name, r.role_key
ORDER BY r.role_id;

-- ==========================================
-- 配置说明
-- ==========================================

/*
字段说明：
-----------
parent_id       : 0 - 独立的一级菜单
order_num       : 99 - 排序靠后，因为是隐藏菜单
path            : 'knowledgepoint/detail/:id' - 完整路径
component       : 'knowledgepoint/detail' - 对应 @/views/knowledgepoint/detail.vue
visible         : '1' - 隐藏菜单，不在侧边栏显示，但可以通过代码跳转访问
menu_type       : 'C' - 菜单（M目录/C菜单/F按钮）
is_cache        : 0 - 不缓存，每次访问重新加载数据
perms           : 'knowledgepoint:knowledgepoint:query' - 需要查询权限

路由效果：
-----------
前端跳转代码：
  this.$router.push('/knowledgepoint/detail/1')

生成的完整路由：
  {
    path: "/knowledgepoint/detail/:id",
    component: Layout,
    hidden: true,  // 自动从 visible='1' 转换
    children: [
      {
        path: '',
        component: () => import('@/views/knowledgepoint/detail'),
        meta: { 
          title: "知识点详情",
          noCache: true  // is_cache=0 转换
        }
      }
    ]
  }
  }

测试步骤：
-----------
1. 执行本SQL脚本
2. 重启后端服务（加载新的菜单数据）
3. 清除浏览器缓存，重新登录
4. 打开知识点管理页面
5. 点击"查看"按钮
6. 应该跳转到 /knowledgepoint/detail/:id 页面
7. 页面保留左侧菜单和顶部导航
8. 知识点管理菜单保持高亮状态

常见问题：
-----------
Q: 为什么要设置 visible='1'？
A: visible='1' 表示隐藏菜单，不在侧边栏显示，但路由仍然可以访问。
   详情页、编辑页等辅助页面都应该设置为隐藏。

Q: 为什么 parent_id 设置为 0？
A: parent_id=0 表示独立的一级菜单。虽然是一级菜单，但设置 visible='1' 隐藏后，
   不会在侧边栏显示，只能通过代码跳转访问。框架会自动将其渲染在 Layout 组件内。

Q: 动态路由参数 :id 如何传递？
A: 跳转时：this.$router.push('/knowledgepoint/detail/' + row.id)
   接收时：this.$route.params.id

Q: 需要删除静态路由配置吗？
A: 是的。数据库配置和静态配置不要混用，否则会产生冲突。使用数据库配置后，
   删除 router/index.js 中的手动配置。
A: 如果之前在 router/index.js 中有手动配置，需要删除，避免冲突。
*/

-- ==========================================
-- 回滚脚本（如需删除配置）
-- ==========================================

-- 删除知识点详情菜单
-- DELETE FROM sys_menu WHERE menu_name = '知识点详情' AND path = 'detail/:id';
