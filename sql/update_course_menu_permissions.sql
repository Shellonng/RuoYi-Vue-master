-- 更新课程管理菜单权限：将menu_id从2000改为1999
-- 1. 为角色101分配menu_id=1999的权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 101, 1999
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu WHERE role_id = 101 AND menu_id = 1999
);

-- 2. 删除menu_id=2000相关的所有角色权限
DELETE FROM sys_role_menu WHERE menu_id = 2000;

-- 3. 验证结果
SELECT role_id, menu_id 
FROM sys_role_menu 
WHERE menu_id IN (1999, 2000)
ORDER BY menu_id, role_id;
