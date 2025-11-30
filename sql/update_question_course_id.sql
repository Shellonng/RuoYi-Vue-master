-- 将 question 表中所有记录的 course_id 更新为 4004
-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

UPDATE question 
SET course_id = 4004;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 查看更新结果
SELECT course_id, COUNT(*) as count 
FROM question 
GROUP BY course_id;
