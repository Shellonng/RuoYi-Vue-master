
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资源智能打标', 0, 4, 'resourceTagging', NULL, 1, 0, 'M', '0', '0', NULL, 'education', 'admin', NOW(), '', NULL, '任务3：课程资源智能打标模块');

SET @parent_menu_id = LAST_INSERT_ID();


INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('资源打标', @parent_menu_id, 1, 'resourceTaggingRenwu3', 'system/resourceTaggingRenwu3', 1, 0, 'C', '0', '0', 'resource:tagging:view', 'upload', 'admin', NOW(), '', NULL, '资源智能打标页面');


SET @resource_tagging_menu_id = LAST_INSERT_ID();


INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('上传资源', @resource_tagging_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'resource:tagging:upload', '#', 'admin', NOW(), '', NULL, '');


INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('分析资源', @resource_tagging_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'resource:tagging:analyze', '#', 'admin', NOW(), '', NULL, '');


INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('确认打标', @resource_tagging_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'resource:tagging:confirm', '#', 'admin', NOW(), '', NULL, '');


INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('AI对话', @resource_tagging_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'resource:tagging:chat', '#', 'admin', NOW(), '', NULL, '');


INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('查询资源', @resource_tagging_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'resource:tagging:query', '#', 'admin', NOW(), '', NULL, '');


INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('删除资源', @resource_tagging_menu_id, 6, '#', '', 1, 0, 'F', '0', '0', 'resource:tagging:remove', '#', 'admin', NOW(), '', NULL, '');


INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, @parent_menu_id);


INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (1, @resource_tagging_menu_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu 
WHERE parent_id = @resource_tagging_menu_id AND menu_type = 'F';


SELECT m.menu_id, m.menu_name, m.perms, m.menu_type, m.parent_id
FROM sys_menu m
WHERE m.menu_name IN ('资源智能打标', '资源打标', '上传资源', '分析资源', '确认打标', 'AI对话', '查询资源', '删除资源')
   OR m.menu_id = @parent_menu_id
ORDER BY m.menu_id;


SELECT rm.role_id, r.role_name, m.menu_name, m.perms
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.role_id
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE m.menu_name IN ('资源智能打标', '资源打标', '上传资源', '分析资源', '确认打标', 'AI对话', '查询资源', '删除资源')
ORDER BY rm.role_id, m.menu_id;

COMMIT;
