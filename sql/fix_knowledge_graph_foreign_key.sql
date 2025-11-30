-- 修复knowledge_graph表的外键约束问题
-- 删除creator_id的外键约束，因为它过于严格

USE education_platform_v1;

-- 删除creator_id的外键约束
ALTER TABLE `knowledge_graph` DROP FOREIGN KEY `fk_knowledge_graph_creator`;

-- 保留course_id的外键约束是合理的
-- 如果需要的话，可以重新添加creator_id的外键约束，但设置为ON DELETE SET NULL
-- ALTER TABLE `knowledge_graph` 
-- ADD CONSTRAINT `fk_knowledge_graph_creator` 
-- FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) 
-- ON DELETE SET NULL ON UPDATE CASCADE;

SELECT 'knowledge_graph外键约束已修复' AS message;
