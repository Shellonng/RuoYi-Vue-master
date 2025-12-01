

USE education_platform;

-- 基础用户（教师 + 学生）
INSERT INTO user (id, username, password, email, real_name, role, status, create_time, update_time)
VALUES (20001, 'teacher_zhang', 'pwd', 'zhang@example.com', '张老师', 'TEACHER', 'ACTIVE', NOW(), NOW()),
       (20002, 'student_li', 'pwd', 'li@example.com', '李雷', 'STUDENT', 'ACTIVE', NOW(), NOW()),
       (20003, 'student_han', 'pwd', 'han@example.com', '韩梅梅', 'STUDENT', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);

INSERT INTO teacher (id, user_id, department, title, status, create_time, update_time)
VALUES (10001, 20001, '计算机学院', '副教授', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);

INSERT INTO student (id, user_id, enrollment_status, create_time, update_time)
VALUES (30001, 20002, 'ENROLLED', NOW(), NOW()),
       (30002, 20003, 'ENROLLED', NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);

-- 课程及班级
INSERT INTO course (id, title, description, teacher_user_id, status, term, student_count, create_time, update_time)
VALUES (40001, '人工智能导论', '基础 AI 课程', 20001, '进行中', '2025春', 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);

INSERT INTO course_class (id, course_id, teacher_user_id, name, is_default, create_time)
VALUES (50001, 40001, 20001, 'AI-2025班', 1, NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO class_student (id, class_id, student_user_id, join_time)
VALUES (51001, 50001, 20002, NOW()),
       (51002, 50001, 20003, NOW())
ON DUPLICATE KEY UPDATE join_time = VALUES(join_time);

INSERT INTO course_student (id, course_id, student_user_id, enroll_time)
VALUES (52001, 40001, 20002, NOW()),
       (52002, 40001, 20003, NOW())
ON DUPLICATE KEY UPDATE enroll_time = VALUES(enroll_time);

-- 知识点
INSERT INTO knowledge_point (id, course_id, title, description, level, creator_user_id, create_time, update_time)
VALUES (60001, 40001, '神经网络基础', '感知机、前馈网络概念', 'BASIC', 20001, NOW(), NOW()),
       (60002, 40001, '梯度下降', '优化算法与反向传播', 'INTERMEDIATE', 20001, NOW(), NOW()),
       (60003, 40001, '正则化技术', 'Dropout、L2/L1', 'INTERMEDIATE', 20001, NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);

-- 作业与知识点关联
INSERT INTO assignment (id, title, course_id, publisher_user_id, type, description, start_time, end_time, status, mode, total_score)
VALUES (70001, '第一章作业', 40001, 20001, 'homework', '简答题 + 报告', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, 'question', 100)
ON DUPLICATE KEY UPDATE end_time = VALUES(end_time);

-- 章节（question 需要关联 chapter）
INSERT INTO chapter (id, course_id, title, description, sort_order, create_time, update_time)
VALUES (72001, 40001, '第1章 神经网络入门', '章节示例', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);

INSERT INTO assignment_kp (id, assignment_id, kp_id, sequence)
VALUES (71001, 70001, 60001, 1),
       (71002, 70001, 60002, 2)
ON DUPLICATE KEY UPDATE sequence = VALUES(sequence);

INSERT INTO question (id, title, question_type, difficulty, correct_answer, explanation, knowledge_point, course_id, chapter_id, created_by)
VALUES (80001, '请解释感知机模型的基本结构', 'short', 2, '描述输入层、权重、激活函数', '考察基础概念', '60001', 40001, 72001, 20001),
       (80002, '梯度下降的关键步骤有哪些？', 'short', 3, '计算梯度、更新参数', '考察优化算法', '60002', 40001, 72001, 20001)
ON DUPLICATE KEY UPDATE update_time = NOW();

INSERT INTO assignment_question (id, assignment_id, question_id, score, sequence)
VALUES (82001, 70001, 80001, 50, 1),
       (82002, 70001, 80002, 50, 2)
ON DUPLICATE KEY UPDATE score = VALUES(score);

-- 学生提交与答题
INSERT INTO assignment_submission (id, assignment_id, student_user_id, status, score, submit_time, grade_time, graded_by, content, file_name, file_path)
VALUES (83001, 70001, 20002, 2, 86, NOW(), NOW(), 20001, '文本答案', NULL, NULL),
       (83002, 70001, 20003, 2, 72, NOW(), NOW(), 20001, '文本答案', NULL, NULL)
ON DUPLICATE KEY UPDATE score = VALUES(score);

INSERT INTO student_answer (id, student_user_id, assignment_id, question_id, answer_content, is_correct, score, answer_time)
VALUES (84001, 20002, 70001, 80001, '阐述结构', 1, 45, NOW()),
       (84002, 20002, 70001, 80002, '列出步骤', 1, 41, NOW()),
       (84003, 20003, 70001, 80001, '描述不完整', 0, 35, NOW()),
       (84004, 20003, 70001, 80002, '部分步骤', 0, 37, NOW())
ON DUPLICATE KEY UPDATE score = VALUES(score);

-- 学习行为（示例）
INSERT INTO student_learning_behavior (id, student_user_id, course_id, behavior_type, target_id, target_type, duration, count, detail, create_time)
VALUES (85001, 20002, 40001, 'video_view', 1, 'section', 3600, 1, JSON_OBJECT('progress', '100%'), NOW()),
       (85002, 20003, 40001, 'video_view', 1, 'section', 2200, 1, JSON_OBJECT('progress', '65%'), NOW())
ON DUPLICATE KEY UPDATE duration = VALUES(duration);

-- 学生知识点掌握度（示例值，可由计算逻辑覆盖）
INSERT INTO student_kp_mastery (id, student_user_id, course_id, kp_id, mastery_score, mastery_status, trend, weight_factors, update_time)
VALUES (86001, 20002, 40001, 60001, 0.85, 'learning', 'improving',
        JSON_OBJECT('exam_score',0.4,'homework_score',0.3,'video_behavior',0.2,'resource_behavior',0.1), NOW()),
       (86002, 20002, 40001, 60002, 0.78, 'learning', 'stable',
        JSON_OBJECT('exam_score',0.5,'homework_score',0.3,'video_behavior',0.1,'resource_behavior',0.1), NOW()),
       (86003, 20003, 40001, 60001, 0.62, 'weak', 'declining',
        JSON_OBJECT('exam_score',0.4,'homework_score',0.2,'video_behavior',0.2,'resource_behavior',0.2), NOW()),
       (86004, 20003, 40001, 60002, 0.55, 'weak', 'stable',
        JSON_OBJECT('exam_score',0.4,'homework_score',0.3,'video_behavior',0.1,'resource_behavior',0.2), NOW())
ON DUPLICATE KEY UPDATE mastery_score = VALUES(mastery_score), update_time = VALUES(update_time);
