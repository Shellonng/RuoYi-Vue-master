-- 修改 course_resource 表的 description 字段类型，以支持存储较长的 JSON 数据
-- 用于存储教学计划的完整数据

ALTER TABLE `course_resource` 
MODIFY COLUMN `description` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '资源描述（支持存储JSON等长文本）';
