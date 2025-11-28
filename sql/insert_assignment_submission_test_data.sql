-- 插入作业提交测试数据
-- 为作业ID 38 (计算机组成原理实验) 插入提交记录

-- 学生33 (wma) 的提交记录
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
    is_deleted
) VALUES (
    38, 
    33, 
    2, -- 已批改
    10, 
    '实验报告内容完整，但缺少部分分析，需要加强对实验结果的深入讨论。', 
    '2025-11-27 14:30:00', 
    '2025-11-28 09:15:00', 
    37, -- 假设教师ID为37
    '本次实验主要学习了计算机组成原理的基本概念。\n\n实验过程：\n1. 首先学习了CPU的基本结构\n2. 然后分析了指令执行过程\n3. 最后完成了相关练习题\n\n实验结果：\n通过本次实验，我对计算机硬件有了更深入的理解。',
    '计算机组成原理实验报告_张三.doc',
    '/profile/upload/2025/11/27/computer_arch_report_zhangsan.doc',
    0
);

-- 再插入一条已提交未批改的记录
INSERT INTO assignment_submission (
    assignment_id, 
    student_user_id, 
    status, 
    submit_time, 
    content, 
    file_name, 
    file_path, 
    is_deleted
) VALUES (
    38, 
    22, -- 假设另一个学生ID
    1, -- 已提交未批改
    '2025-11-28 10:45:00', 
    '实验报告内容：\n\n1. 实验目的\n学习计算机组成原理的基本概念和工作原理\n\n2. 实验内容\n- CPU结构分析\n- 指令系统学习\n- 存储器层次结构\n\n3. 实验总结\n本次实验让我对计算机硬件系统有了全面的认识。',
    '计算机组成实验_李四.pdf',
    '/profile/upload/2025/11/28/computer_experiment_lisi.pdf',
    0
);

-- 再插入一条未提交的记录（实际上这种记录通常不会在数据库中，但为了演示）
INSERT INTO assignment_submission (
    assignment_id, 
    student_user_id, 
    status, 
    is_deleted
) VALUES (
    38, 
    24, -- 另一个学生ID  
    0, -- 未提交
    0
);