-- 为测试资源添加知识点关联
-- 假设资源ID可以通过查询course_resource表获取

-- 查看现有资源（执行这个查询来获取资源ID）
SELECT id, name, course_id FROM course_resource ORDER BY id DESC LIMIT 5;

-- 假设资源ID分别为 35 和 36（根据截图中的资源名称）
-- 知识点ID: 524(测试), 512(异常处理机制) 等

-- 为第一个资源（计算机组成原理数学大纲.pdf，假设ID=35）关联知识点
INSERT INTO course_resource_kp (resource_id, kp_id, confidence_score, is_ai_tagged, create_time)
VALUES 
(35, 524, 0.95, 1, NOW()),  -- 测试知识点
(35, 512, 0.90, 1, NOW());  -- 异常处理机制

-- 为第二个资源（A0801040040-计算...doc，假设ID=36）关联知识点  
INSERT INTO course_resource_kp (resource_id, kp_id, confidence_score, is_ai_tagged, create_time)
VALUES 
(36, 499, 0.92, 1, NOW()),  -- 取指周期
(36, 482, 0.88, 1, NOW()),  -- 单地址指令
(36, 475, 0.85, 1, NOW());  -- 定长与变长指令

-- 验证插入结果
SELECT 
    cr.id AS resource_id,
    cr.name AS resource_name,
    kp.id AS kp_id,
    kp.title AS kp_name,
    crkp.confidence_score
FROM course_resource cr
LEFT JOIN course_resource_kp crkp ON cr.id = crkp.resource_id
LEFT JOIN knowledge_point kp ON crkp.kp_id = kp.id
WHERE cr.id IN (35, 36)
ORDER BY cr.id, kp.id;
