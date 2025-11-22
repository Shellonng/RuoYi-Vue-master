-- 第1步：先把旧值安全映射（避免直接删enum导致报错）
UPDATE kp_relation 
SET relation_type = CASE relation_type
    WHEN 'PREREQUISITE' THEN 'prerequisite_of'
    WHEN 'EXTENSION'    THEN 'extension_of'
    WHEN 'SIMILAR'      THEN 'similar_to'
    WHEN 'EXAMPLE'      THEN 'prerequisite_of'      -- 临时随便映射一个，后面会删掉
    WHEN 'BELONGS_TO'   THEN 'prerequisite_of'      -- 临时随便映射一个，后面会删掉
    ELSE relation_type
END;
-- 第2步：彻底删除 BELONGS_TO 和 EXAMPLE 两条记录（数据清理）
DELETE FROM kp_relation 
WHERE relation_type IN ('BELONGS_TO', 'EXAMPLE');
-- 第3步：正式把 enum 改成你最终想要的纯净 5 个值（加上 derived_from 和 counterexample_of）
ALTER TABLE kp_relation 
MODIFY COLUMN relation_type ENUM(
    'prerequisite_of',
    'similar_to',
    'extension_of',
    'derived_from',          -- 新增第1个
    'counterexample_of'      -- 新增第2个
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL 
COMMENT '纯知识点关系：前置/相似/进阶/推导/反例';