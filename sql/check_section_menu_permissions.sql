-- =============================================
-- 查询小节详情菜单的权限分配情况
-- =============================================

-- 第一步：查询小节详情菜单的基本信息
SELECT 
    menu_id,
    menu_name,
    parent_id,
    path,
    component,
    visible,
    status,
    perms
FROM sys_menu 
WHERE menu_name = '小节详情';

-- 第二步：查询哪些角色有权限访问小节详情菜单
SELECT 
    r.role_id,
    r.role_name,
    r.role_key,
    r.status AS role_status,
    rm.menu_id
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
WHERE rm.menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '小节详情')
ORDER BY r.role_id;

-- 第三步：查询哪些用户有权限访问（通过角色）
SELECT DISTINCT
    u.user_id,
    u.user_name,
    u.nick_name,
    u.status AS user_status,
    r.role_id,
    r.role_name
FROM sys_user u
INNER JOIN sys_user_role ur ON u.user_id = ur.user_id
INNER JOIN sys_role r ON ur.role_id = r.role_id
INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id
WHERE rm.menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '小节详情')
  AND u.del_flag = '0'
  AND u.status = '0'
  AND r.status = '0'
ORDER BY u.user_id;

-- 第四步：检查当前登录用户（假设user_id=1）是否有权限
-- 替换 user_id = 1 为你当前登录的用户ID
SELECT 
    '当前用户权限检查' AS check_type,
    COUNT(*) AS has_permission
FROM sys_user_role ur
INNER JOIN sys_role_menu rm ON ur.role_id = rm.role_id
WHERE ur.user_id = 1  -- ★ 改成你当前登录的用户ID
  AND rm.menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '小节详情');

-- 第五步：查询课程详情菜单的权限（对比参考）
SELECT 
    '课程详情菜单权限' AS menu_type,
    r.role_id,
    r.role_name
FROM sys_role r
INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id
WHERE rm.menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '课程详情')
ORDER BY r.role_id;

-- 第六步：为管理员角色添加小节详情菜单权限
-- 如果上面查询发现没有角色有权限，执行下面的INSERT语句
-- 注意：role_id = 1 通常是超级管理员，根据实际情况修改

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 
    1,  -- ★ 管理员角色ID，根据实际情况修改
    menu_id
FROM sys_menu
WHERE menu_name = '小节详情'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu 
    WHERE role_id = 1 
      AND menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '小节详情')
  );

-- 第七步：批量为所有有课程详情权限的角色添加小节详情权限
-- 如果某个角色有课程详情的权限，应该也有小节详情的权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT
    rm.role_id,
    (SELECT menu_id FROM sys_menu WHERE menu_name = '小节详情')
FROM sys_role_menu rm
WHERE rm.menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '课程详情')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm2
    WHERE rm2.role_id = rm.role_id
      AND rm2.menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '小节详情')
  );

-- 第八步：验证权限添加结果
SELECT 
    '权限分配结果' AS result_type,
    r.role_id,
    r.role_name,
    COUNT(DISTINCT rm.menu_id) AS menu_count
FROM sys_role r
INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id
WHERE rm.menu_id IN (
    SELECT menu_id FROM sys_menu 
    WHERE menu_name IN ('课程详情', '小节详情')
)
GROUP BY r.role_id, r.role_name
ORDER BY r.role_id;

-- =============================================
-- 执行顺序说明
-- =============================================
-- 1. 执行第一步：确认菜单已插入
-- 2. 执行第二步：查看哪些角色有权限（如果结果为空，说明没有角色有权限）
-- 3. 执行第三步：查看哪些用户有权限
-- 4. 执行第四步：检查当前用户是否有权限
-- 5. 如果上面查询结果都是空的，执行第六步或第七步添加权限
-- 6. 执行第八步：验证权限添加成功
-- 7. 重启后端服务
-- 8. 清除浏览器缓存，重新登录
-- =============================================
