
USE `education_platform`;

/*Table structure for table `assignment` */

DROP TABLE IF EXISTS `assignment`;

CREATE TABLE `assignment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业或考试标题',
  `course_id` bigint(20) NOT NULL,
  `publisher_user_id` bigint(20) NOT NULL COMMENT '发布者 user.id',
  `type` enum('homework','exam') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'homework' COMMENT '任务类型',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '发布状态：0未发布，1已发布',
  `update_time` datetime DEFAULT NULL,
  `mode` enum('question','file') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'question' COMMENT '作业模式：question-答题型，file-上传型',
  `time_limit` int(11) DEFAULT NULL COMMENT '时间限制（分钟）',
  `total_score` int(11) DEFAULT '100' COMMENT '总分',
  `duration` int(11) DEFAULT NULL COMMENT '任务时长（分钟）',
  `allowed_file_types` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '允许的文件类型（JSON格式）',
  `attachments` JSON DEFAULT NULL COMMENT '任务附件列表，支持多文件，格式：[{"name":"文件名.pdf","url":"https://xxx.com/file.pdf"}]',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `course_id` (`course_id`) USING BTREE,
  KEY `fk_assignment_user` (`publisher_user_id`) USING BTREE,
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `assignment_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_assignment_user` FOREIGN KEY (`publisher_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='作业或考试表';



DROP TABLE IF EXISTS `assignment_submission`;

CREATE TABLE `assignment_submission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `assignment_id` bigint(20) NOT NULL COMMENT '作业ID',
  `student_user_id` bigint(20) NOT NULL COMMENT '学生 user.id',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '状态：0-未提交，1-已提交未批改，2-已批改',
  `score` int(11) DEFAULT NULL COMMENT '得分',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '教师反馈',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `grade_time` datetime DEFAULT NULL COMMENT '批改时间',
  `graded_by` bigint(20) DEFAULT NULL COMMENT '批改人ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '提交内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件名称',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件路径',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_assignment_id` (`assignment_id`) USING BTREE,
  KEY `idx_student_id` (`student_user_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `fk_submission_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_submission_student` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='作业提交记录表';




DROP TABLE IF EXISTS `student`;

CREATE TABLE `student` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `enrollment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'ENROLLED' COMMENT '学籍状态',
  `gpa` decimal(3,2) DEFAULT NULL COMMENT 'GPA',
  `gpa_level` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'GPA等级',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_id` (`user_id`) USING BTREE,
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=20250014 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='学生表';




--智能批改记录表 (ai_grading)


CREATE TABLE ai_grading (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '批改ID',
  assignment_submission_id BIGINT NOT NULL COMMENT '作业提交表id',
  content_score DECIMAL(5,2) COMMENT '内容相关性得分（0-100）',
  logic_score DECIMAL(5,2) COMMENT '逻辑结构得分（0-100）',
  knowledge_score DECIMAL(5,2) COMMENT '知识点覆盖得分（0-100）',
  innovation_score DECIMAL(5,2) COMMENT '创新性得分（0-100）',
  total_score DECIMAL(5,2) COMMENT '综合得分',
  ai_comment TEXT COMMENT 'AI评语',
  improvement_suggestions JSON COMMENT '改进建议',
  covered_knowledge_points JSON COMMENT '覆盖的知识点',
  missing_knowledge_points JSON COMMENT '缺失的知识点',
  llm_model VARCHAR(50) COMMENT '使用的LLM模型',
  llm_tokens INT COMMENT '消耗的token数',
  processing_time INT COMMENT '处理时长（毫秒）',
  status ENUM('pending', 'processing', 'completed', 'failed') DEFAULT 'pending',
  error_message TEXT COMMENT '错误信息（如果失败）',
  teacher_confirmed TINYINT DEFAULT 0 COMMENT '教师是否确认：1-是 0-否',
  teacher_modified_score DECIMAL(5,2) COMMENT '教师修改后的分数',
  teacher_comment TEXT COMMENT '教师补充评语',
  confirmed_by BIGINT COMMENT '确认教师ID',
  confirmed_at TIMESTAMP COMMENT '确认时间',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_grading_submission FOREIGN KEY (assignment_submission_id) REFERENCES assignment_submission(id) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT fk_grading_confirmed_by FOREIGN KEY (confirmed_by) REFERENCES `user`(id) ON DELETE SET NULL ON UPDATE RESTRICT,
  INDEX idx_submission (assignment_submission_id),
  INDEX idx_status (status),
  INDEX idx_confirmed_by (confirmed_by),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能批改记录表';


-- 知识点表
DROP TABLE IF EXISTS `knowledge_point`;

CREATE TABLE `knowledge_point` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(20) NOT NULL COMMENT '所属课程',
  `title` varchar(200) NOT NULL COMMENT '知识点名称（如“二分查找”）',
  `description` text COMMENT '详细解释（AI生成）',
  `level` enum('BASIC','INTERMEDIATE','ADVANCED') DEFAULT 'BASIC' COMMENT '难度',
  `creator_user_id` bigint(20) NOT NULL COMMENT '创建者 user.id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_course` (`course_id`),
  KEY `idx_creator` (`creator_user_id`),
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `fk_kp_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_kp_creator` FOREIGN KEY (`creator_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识点表';


-- 任务与知识点关联表（非强制）
DROP TABLE IF EXISTS `assignment_kp`;

CREATE TABLE `assignment_kp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint(20) DEFAULT NULL COMMENT '作业/考试ID（允许为空，兼容旧数据）',
  `kp_id` bigint(20) DEFAULT NULL COMMENT '知识点ID',
  `sequence` int(11) DEFAULT '1' COMMENT '在任务中的顺序',
  `is_required` tinyint(1) DEFAULT '1' COMMENT '是否必修知识点',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assignment_kp` (`assignment_id`,`kp_id`),
  KEY `idx_kp` (`kp_id`),
  KEY `idx_assignment` (`assignment_id`),
  CONSTRAINT `fk_ak_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ak_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务与知识点关联表（非强制）';

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码(加密)',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role` enum('STUDENT','TEACHER','ADMIN') NOT NULL DEFAULT 'STUDENT' COMMENT '用户角色: STUDENT=学生, TEACHER=教师, ADMIN=管理员',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';


