-- 在"资源智能打标"目录下添加"管理资源"子菜单
-- 仿照作业管理的页面结构，显示字段：资源名称-所属课程-知识点-文件类型-操作

-- 获取"资源智能打标"的菜单ID（如果已存在）
SET @parent_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '资源智能打标' AND menu_type = 'M' LIMIT 1);

-- 如果不存在父菜单，先创建它
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '资源智能打标', 0, 4, 'resourceTagging', NULL, 1, 0, 'M', '0', '0', NULL, 'education', 'admin', NOW(), '', NULL, '任务3：课程资源智能打标模块'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '资源智能打标' AND menu_type = 'M');

-- 重新获取父菜单ID
SET @parent_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '资源智能打标' AND menu_type = 'M' LIMIT 1);

-- 插入"管理资源"菜单（C类型 - 菜单页面）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('管理资源', @parent_menu_id, 2, 'resourceManagement', 'system/resourceManagement/index', 1, 0, 'C', '0', '0', 'resource:management:list', 'list', 'admin', NOW(), '', NULL, '课程资源管理页面');

-- 获取刚插入的"管理资源"菜单ID
SET @resource_management_menu_id = LAST_INSERT_ID();

-- 插入"管理资源"的功能按钮（F类型 - 功能权限）

-- 1. 新增资源
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('新增资源', @resource_management_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'resource:management:add', '#', 'admin', NOW(), '', NULL, '');

-- 2. 修改资源
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('修改资源', @resource_management_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'resource:management:edit', '#', 'admin', NOW(), '', NULL, '');

-- 3. 删除资源
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('删除资源', @resource_management_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'resource:management:remove', '#', 'admin', NOW(), '', NULL, '');

-- 4. 查询资源
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('查询资源', @resource_management_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'resource:management:query', '#', 'admin', NOW(), '', NULL, '');

-- 5. 导出资源
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('导出资源', @resource_management_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'resource:management:export', '#', 'admin', NOW(), '', NULL, '');

-- 为角色1（管理员）授权
INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, @resource_management_menu_id);

-- 授权管理员访问所有功能按钮
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu 
WHERE parent_id = @resource_management_menu_id AND menu_type = 'F';

-- 为角色101（教师）授权
INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (101, @resource_management_menu_id);

-- 授权教师访问所有功能按钮
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 101, menu_id FROM sys_menu 
WHERE parent_id = @resource_management_menu_id AND menu_type = 'F';

-- 查询验证：显示新创建的菜单
SELECT m.menu_id, m.menu_name, m.parent_id, m.perms, m.menu_type, m.path, m.component
FROM sys_menu m
WHERE m.menu_name IN ('资源智能打标', '管理资源', '新增资源', '修改资源', '删除资源', '查询资源', '导出资源')
   OR m.menu_id = @parent_menu_id
   OR m.menu_id = @resource_management_menu_id
ORDER BY m.parent_id, m.order_num;

-- 查询验证：显示角色权限分配
SELECT rm.role_id, r.role_name, m.menu_name, m.perms, m.menu_type
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.role_id
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE m.menu_name IN ('管理资源', '新增资源', '修改资源', '删除资源', '查询资源', '导出资源')
ORDER BY rm.role_id, m.order_num;

COMMIT;
