-- 查找任务管理菜单的真实ID
SELECT menu_id, menu_name, parent_id, path, component, menu_type, order_num
FROM sys_menu 
WHERE menu_name LIKE '%任务%'
ORDER BY menu_id;

-- 查找所有一级菜单
SELECT menu_id, menu_name, parent_id, path, component, menu_type, order_num
FROM sys_menu 
WHERE parent_id = 0
ORDER BY order_num;
