-- 运行此脚本来插入测试数据
-- 请在数据库管理工具（如MySQL Workbench、phpMyAdmin等）中执行此SQL

USE education_platform_v1;

-- 先查看当前的作业提交数据
SELECT * FROM assignment_submission WHERE assignment_id = 38;

-- 插入作业提交测试数据
-- 为作业ID 38 (计算机组成原理实验) 插入提交记录

-- 学生33 (wma) 的提交记录 - 已批改
INSERT INTO assignment_submission (
    assignment_id, 
    student_user_id, 
    status, 
    score, 
    feedback, 
    submit_time, 
    grade_time, 
    graded_by, 
    content, 
    file_name, 
    file_path, 
    is_deleted,
    create_time,
    update_time
) VALUES (
    38, 
    33, 
    2, -- 已批改
    10, 
    '实验报告内容完整，但缺少部分分析，需要加强对实验结果的深入讨论。实验过程描述清晰，结论基本正确。', 
    '2025-11-27 14:30:00', 
    '2025-11-28 09:15:00', 
    37, -- 教师ID
    '本次实验主要学习了计算机组成原理的基本概念。

实验过程：
1. 首先学习了CPU的基本结构
2. 然后分析了指令执行过程
3. 最后完成了相关练习题

实验结果：
通过本次实验，我对计算机硬件有了更深入的理解，特别是对CPU工作原理和指令执行流程有了清晰的认识。',
    '计算机组成原理实验报告_张三.doc',
    '/profile/upload/2025/11/27/computer_arch_report_zhangsan.doc',
    0,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE 
    content = VALUES(content),
    feedback = VALUES(feedback),
    update_time = NOW();

-- 插入一条已提交未批改的记录
INSERT INTO assignment_submission (
    assignment_id, 
    student_user_id, 
    status, 
    submit_time, 
    content, 
    file_name, 
    file_path, 
    is_deleted,
    create_time,
    update_time
) VALUES (
    38, 
    22, -- 学生ID 22
    1, -- 已提交未批改
    '2025-11-28 10:45:00', 
    '实验报告内容：

1. 实验目的
学习计算机组成原理的基本概念和工作原理

2. 实验内容
- CPU结构分析
- 指令系统学习  
- 存储器层次结构

3. 实验过程
本次实验分为三个部分进行...

4. 实验总结
本次实验让我对计算机硬件系统有了全面的认识，理解了各个部件之间的协作关系。',
    '计算机组成实验_李四.pdf',
    '/profile/upload/2025/11/28/computer_experiment_lisi.pdf',
    0,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE 
    content = VALUES(content),
    submit_time = VALUES(submit_time),
    update_time = NOW();

-- 插入第三条记录 - 已提交未批改
INSERT INTO assignment_submission (
    assignment_id, 
    student_user_id, 
    status, 
    submit_time, 
    content, 
    file_name, 
    file_path, 
    is_deleted,
    create_time,
    update_time
) VALUES (
    38, 
    24, -- 学生ID 24
    1, -- 已提交未批改
    '2025-11-28 11:20:00', 
    '计算机组成原理实验报告

实验名称：计算机组成原理基础实验
实验时间：2025年11月28日

一、实验目的
1. 理解计算机硬件组成
2. 学习CPU工作原理
3. 掌握存储器结构

二、实验内容及步骤
1. 观察CPU结构图
2. 分析指令执行过程
3. 学习存储器工作原理

三、实验结果与分析
通过本次实验，加深了对计算机硬件的理解。',
    '实验报告_王五.docx',
    '/profile/upload/2025/11/28/lab_report_wangwu.docx',
    0,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE 
    content = VALUES(content),
    submit_time = VALUES(submit_time),
    update_time = NOW();

-- 查看插入结果
SELECT 
    s.id,
    s.assignment_id,
    s.student_user_id,
    u.real_name as student_name,
    u.username as student_username,
    s.status,
    s.score,
    s.submit_time,
    s.grade_time,
    grader.real_name as grader_name,
    s.file_name,
    CASE 
        WHEN s.status = 0 THEN '未提交'
        WHEN s.status = 1 THEN '已提交未批改'
        WHEN s.status = 2 THEN '已批改'
        ELSE '未知'
    END as status_text
FROM assignment_submission s
LEFT JOIN user u ON s.student_user_id = u.id
LEFT JOIN user grader ON s.graded_by = grader.id
WHERE s.assignment_id = 38 AND s.is_deleted = 0
ORDER BY s.submit_time DESC;