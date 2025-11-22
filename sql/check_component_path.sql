-- 检查课程管理和作业考试的组件路径配置
SELECT 
    menu_id,
    menu_name,
    component,
    path,
    visible,
    status
FROM sys_menu 
WHERE menu_name IN ('课程管理', '作业或考试')
ORDER BY menu_id;
