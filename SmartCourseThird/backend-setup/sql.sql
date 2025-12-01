-- MySQL dump 10.13  Distrib 8.0.43, for Linux (x86_64)
--
-- Host: 8.131.109.137    Database: education_platform
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ai_grading`
--

DROP TABLE IF EXISTS `ai_grading`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_grading` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '批改ID',
  `assignment_submission_id` bigint NOT NULL COMMENT '作业提交表id',
  `content_score` decimal(5,2) DEFAULT NULL COMMENT '内容相关性得分（0-100）',
  `logic_score` decimal(5,2) DEFAULT NULL COMMENT '逻辑结构得分（0-100）',
  `knowledge_score` decimal(5,2) DEFAULT NULL COMMENT '知识点覆盖得分（0-100）',
  `innovation_score` decimal(5,2) DEFAULT NULL COMMENT '创新性得分（0-100）',
  `total_score` decimal(5,2) DEFAULT NULL COMMENT '综合得分',
  `ai_comment` text COMMENT 'AI评语',
  `improvement_suggestions` json DEFAULT NULL COMMENT '改进建议',
  `covered_knowledge_points` json DEFAULT NULL COMMENT '覆盖的知识点',
  `missing_knowledge_points` json DEFAULT NULL COMMENT '缺失的知识点',
  `llm_model` varchar(50) DEFAULT NULL COMMENT '使用的LLM模型',
  `llm_tokens` int DEFAULT NULL COMMENT '消耗的token数',
  `processing_time` int DEFAULT NULL COMMENT '处理时长（毫秒）',
  `status` enum('pending','processing','completed','failed') DEFAULT 'pending',
  `error_message` text COMMENT '错误信息（如果失败）',
  `teacher_confirmed` tinyint DEFAULT '0' COMMENT '教师是否确认：1-是 0-否',
  `teacher_modified_score` decimal(5,2) DEFAULT NULL COMMENT '教师修改后的分数',
  `teacher_comment` text COMMENT '教师补充评语',
  `confirmed_by` bigint DEFAULT NULL COMMENT '确认教师ID',
  `confirmed_at` timestamp NULL DEFAULT NULL COMMENT '确认时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能批改记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_grading`
--

/*!40000 ALTER TABLE `ai_grading` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_grading` ENABLE KEYS */;

--
-- Table structure for table `assignment`
--

DROP TABLE IF EXISTS `assignment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业或考试标题',
  `course_id` bigint NOT NULL,
  `publisher_user_id` bigint NOT NULL COMMENT '发布者 user.id',
  `type` enum('homework','exam') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'homework' COMMENT '任务类型',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '发布状态：0未发布，1已发布',
  `update_time` datetime DEFAULT NULL,
  `mode` enum('question','file') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'question' COMMENT '作业模式：question-答题型，file-上传型',
  `time_limit` int DEFAULT NULL COMMENT '时间限制（分钟）',
  `total_score` int DEFAULT '100' COMMENT '总分',
  `duration` int DEFAULT NULL COMMENT '任务时长（分钟）',
  `allowed_file_types` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '允许的文件类型（JSON格式）',
  `attachments` json DEFAULT NULL COMMENT '任务附件列表，支持多文件，格式：[{"name":"文件名.pdf","url":"https://xxx.com/file.pdf"}]',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `course_id` (`course_id`) USING BTREE,
  KEY `fk_assignment_user` (`publisher_user_id`) USING BTREE,
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `assignment_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_assignment_user` FOREIGN KEY (`publisher_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=70002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='作业或考试表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assignment`
--

/*!40000 ALTER TABLE `assignment` DISABLE KEYS */;
INSERT INTO `assignment` VALUES (70001,'第一章作业',40001,20001,'homework','简答题 + 报告','2025-11-19 19:59:04','2025-11-26 20:17:39','2025-11-19 19:59:04',1,NULL,'question',NULL,100,NULL,NULL,NULL,0,NULL);
/*!40000 ALTER TABLE `assignment` ENABLE KEYS */;

--
-- Table structure for table `assignment_kp`
--

DROP TABLE IF EXISTS `assignment_kp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignment_kp` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint DEFAULT NULL COMMENT '作业/考试ID（允许为空，兼容旧数据）',
  `kp_id` bigint DEFAULT NULL COMMENT '知识点ID',
  `sequence` int DEFAULT '1' COMMENT '在任务中的顺序',
  `is_required` tinyint(1) DEFAULT '1' COMMENT '是否必修知识点',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assignment_kp` (`assignment_id`,`kp_id`),
  KEY `idx_kp` (`kp_id`),
  KEY `idx_assignment` (`assignment_id`),
  CONSTRAINT `fk_ak_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ak_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=71003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务与知识点关联表（非强制）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assignment_kp`
--

/*!40000 ALTER TABLE `assignment_kp` DISABLE KEYS */;
INSERT INTO `assignment_kp` VALUES (71001,70001,60001,1,1,'2025-11-19 19:59:04'),(71002,70001,60002,2,1,'2025-11-19 19:59:04');
/*!40000 ALTER TABLE `assignment_kp` ENABLE KEYS */;

--
-- Table structure for table `assignment_question`
--

DROP TABLE IF EXISTS `assignment_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignment_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `score` int DEFAULT '5' COMMENT '该题满分',
  `sequence` int DEFAULT '1' COMMENT '题目顺序',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `assignment_id` (`assignment_id`) USING BTREE,
  KEY `question_id` (`question_id`) USING BTREE,
  CONSTRAINT `assignment_question_ibfk_1` FOREIGN KEY (`assignment_id`) REFERENCES `assignment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `assignment_question_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=82003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='作业题目关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assignment_question`
--

/*!40000 ALTER TABLE `assignment_question` DISABLE KEYS */;
INSERT INTO `assignment_question` VALUES (82001,70001,80001,50,1,0,NULL),(82002,70001,80002,50,2,0,NULL);
/*!40000 ALTER TABLE `assignment_question` ENABLE KEYS */;

--
-- Table structure for table `assignment_submission`
--

DROP TABLE IF EXISTS `assignment_submission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignment_submission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `assignment_id` bigint NOT NULL COMMENT '作业ID',
  `student_user_id` bigint NOT NULL COMMENT '学生 user.id',
  `status` int NOT NULL DEFAULT '0' COMMENT '状态：0-未提交，1-已提交未批改，2-已批改',
  `score` int DEFAULT NULL COMMENT '得分',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '教师反馈',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `grade_time` datetime DEFAULT NULL COMMENT '批改时间',
  `graded_by` bigint DEFAULT NULL COMMENT '批改人ID',
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
) ENGINE=InnoDB AUTO_INCREMENT=83003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='作业提交记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assignment_submission`
--

/*!40000 ALTER TABLE `assignment_submission` DISABLE KEYS */;
INSERT INTO `assignment_submission` VALUES (83001,70001,20002,2,86,NULL,'2025-11-19 19:59:04','2025-11-19 19:59:04',20001,'文本答案','2025-11-19 19:59:04','2025-11-19 19:59:04',NULL,NULL,0,NULL),(83002,70001,20003,2,72,NULL,'2025-11-19 19:59:04','2025-11-19 19:59:04',20001,'文本答案','2025-11-19 19:59:04','2025-11-19 19:59:04',NULL,NULL,0,NULL);
/*!40000 ALTER TABLE `assignment_submission` ENABLE KEYS */;

--
-- Table structure for table `chapter`
--

DROP TABLE IF EXISTS `chapter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chapter` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '章节ID，主键',
  `course_id` bigint NOT NULL COMMENT '所属课程ID，外键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '章节名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `sort_order` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_course_id` (`course_id`) USING BTREE,
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `fk_chapter_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=72002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='课程章节表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chapter`
--

/*!40000 ALTER TABLE `chapter` DISABLE KEYS */;
INSERT INTO `chapter` VALUES (72001,40001,'第1章 神经网络入门','章节示例',1,'2025-11-19 20:06:14','2025-11-19 20:17:39',0,NULL);
/*!40000 ALTER TABLE `chapter` ENABLE KEYS */;

--
-- Table structure for table `class_student`
--

DROP TABLE IF EXISTS `class_student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_student` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `student_user_id` bigint NOT NULL,
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_class_student` (`class_id`,`student_user_id`) USING BTREE,
  KEY `student_id` (`student_user_id`) USING BTREE,
  CONSTRAINT `fk_class_student_user` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='班级学生关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_student`
--

/*!40000 ALTER TABLE `class_student` DISABLE KEYS */;
INSERT INTO `class_student` VALUES (51001,50001,20002,'2025-11-19 20:17:39',0,NULL),(51002,50001,20003,'2025-11-19 20:17:39',0,NULL);
/*!40000 ALTER TABLE `class_student` ENABLE KEYS */;

--
-- Table structure for table `competency_kp_relation`
--

DROP TABLE IF EXISTS `competency_kp_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `competency_kp_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `competency_id` bigint NOT NULL COMMENT '能力点ID（关联course_competency表）',
  `kp_id` bigint NOT NULL COMMENT '知识点ID（关联knowledge_point表）',
  `contribution_rate` decimal(3,2) DEFAULT '1.00' COMMENT '知识点对能力点的贡献度（0-1，用于加权计算能力得分）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_competency_kp` (`competency_id`,`kp_id`) USING BTREE COMMENT '同一能力点-知识点关联唯一',
  KEY `idx_kp` (`kp_id`) USING BTREE,
  CONSTRAINT `fk_ckr_competency` FOREIGN KEY (`competency_id`) REFERENCES `course_competency` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ckr_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='能力点-知识点关联表（支撑能力模型与知识点的映射）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `competency_kp_relation`
--

/*!40000 ALTER TABLE `competency_kp_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `competency_kp_relation` ENABLE KEYS */;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '课程描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课程封面图片',
  `credit` decimal(3,1) DEFAULT '3.0' COMMENT '学分',
  `course_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '必修课' COMMENT '课程类型',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `teacher_user_id` bigint NOT NULL COMMENT '任课教师 user.id',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '未开始' COMMENT '课程状态',
  `term` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学期',
  `student_count` int DEFAULT '0' COMMENT '学生数量',
  `average_score` decimal(5,2) DEFAULT NULL COMMENT '平均分数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记: 0=未删除, 1=已删除',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_term` (`term`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_teacher_user_id` (`teacher_user_id`),
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `fk_course_teacher_user` FOREIGN KEY (`teacher_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='课程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (40001,'人工智能导论','基础 AI 课程',NULL,3.0,'必修课',NULL,NULL,20001,'进行中','2025春',2,NULL,'2025-11-19 19:59:04','2025-11-19 20:17:39',0,NULL);
/*!40000 ALTER TABLE `course` ENABLE KEYS */;

--
-- Table structure for table `course_class`
--

DROP TABLE IF EXISTS `course_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_class` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `course_id` bigint DEFAULT NULL,
  `teacher_user_id` bigint NOT NULL COMMENT '教师 user.id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `is_default` tinyint(1) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `course_class_ibfk_1` (`course_id`) USING BTREE,
  KEY `course_class_ibfk_2` (`teacher_user_id`) USING BTREE,
  CONSTRAINT `course_class_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_course_class_teacher_user` FOREIGN KEY (`teacher_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=50002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='课程班级表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_class`
--

/*!40000 ALTER TABLE `course_class` DISABLE KEYS */;
INSERT INTO `course_class` VALUES (50001,40001,20001,'AI-2025班',NULL,1,'2025-11-19 19:59:04',0,NULL);
/*!40000 ALTER TABLE `course_class` ENABLE KEYS */;

--
-- Table structure for table `course_competency`
--

DROP TABLE IF EXISTS `course_competency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_competency` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '能力点ID',
  `course_id` bigint NOT NULL COMMENT '所属课程ID（关联course表）',
  `competency_name` varchar(100) NOT NULL COMMENT '能力点名称（如“数据结构理解能力”“算法优化能力”）',
  `competency_desc` varchar(500) DEFAULT NULL COMMENT '能力点描述',
  `sort_order` int DEFAULT '1' COMMENT '展示顺序（雷达图维度排序）',
  `weight` decimal(3,2) DEFAULT '1.00' COMMENT '能力点权重（用于综合能力计算）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_course_competency` (`course_id`,`competency_name`) USING BTREE COMMENT '同一课程能力点名称唯一',
  KEY `idx_course` (`course_id`) USING BTREE,
  CONSTRAINT `fk_cc_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程能力点表（定义能力模型维度）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_competency`
--

/*!40000 ALTER TABLE `course_competency` DISABLE KEYS */;
/*!40000 ALTER TABLE `course_competency` ENABLE KEYS */;

--
-- Table structure for table `course_enrollment_request`
--

DROP TABLE IF EXISTS `course_enrollment_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_enrollment_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `student_user_id` bigint NOT NULL COMMENT '学生 user.id',
  `course_id` bigint NOT NULL COMMENT '申请加入的课程ID',
  `status` tinyint DEFAULT '0' COMMENT '申请状态：0=待审核 1=已通过 2=已拒绝',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '学生申请理由',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '教师审核意见',
  `submit_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_student_course` (`student_user_id`,`course_id`) USING BTREE,
  KEY `course_id` (`course_id`) USING BTREE,
  CONSTRAINT `course_enrollment_request_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_enrollment_student_user` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学生选课申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_enrollment_request`
--

/*!40000 ALTER TABLE `course_enrollment_request` DISABLE KEYS */;
/*!40000 ALTER TABLE `course_enrollment_request` ENABLE KEYS */;

--
-- Table structure for table `course_resource`
--

DROP TABLE IF EXISTS `course_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `course_id` bigint NOT NULL COMMENT '所属课程ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源名称',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件类型，如pdf、doc、ppt等',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件URL',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '资源描述',
  `download_count` int DEFAULT '0' COMMENT '下载次数',
  `upload_user_id` bigint NOT NULL COMMENT '上传用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_course_id` (`course_id`) USING BTREE COMMENT '课程ID索引',
  KEY `idx_upload_user_id` (`upload_user_id`) USING BTREE COMMENT '上传用户ID索引'
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='课程资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_resource`
--

/*!40000 ALTER TABLE `course_resource` DISABLE KEYS */;
/*!40000 ALTER TABLE `course_resource` ENABLE KEYS */;

--
-- Table structure for table `course_resource_kp`
--

DROP TABLE IF EXISTS `course_resource_kp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_resource_kp` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint DEFAULT NULL COMMENT '资源ID（允许为空）',
  `kp_id` bigint DEFAULT NULL COMMENT '知识点ID',
  `is_confirmed` tinyint(1) DEFAULT '0' COMMENT '教师是否确认',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_kp` (`resource_id`,`kp_id`),
  KEY `idx_kp` (`kp_id`),
  KEY `idx_confirmed` (`is_confirmed`),
  CONSTRAINT `fk_crk_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_crk_resource` FOREIGN KEY (`resource_id`) REFERENCES `course_resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程资源与知识点智能关联表（非强制）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_resource_kp`
--

/*!40000 ALTER TABLE `course_resource_kp` DISABLE KEYS */;
/*!40000 ALTER TABLE `course_resource_kp` ENABLE KEYS */;

--
-- Table structure for table `course_student`
--

DROP TABLE IF EXISTS `course_student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_student` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `student_user_id` bigint NOT NULL COMMENT '学生 user.id',
  `enroll_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `collected` int DEFAULT '0' COMMENT '课程是否被该学生收藏，1为被收藏，0为未被收藏',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_course_student` (`course_id`,`student_user_id`) USING BTREE,
  KEY `student_id` (`student_user_id`) USING BTREE,
  CONSTRAINT `course_student_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_course_student_user` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=52003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学生选课表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_student`
--

/*!40000 ALTER TABLE `course_student` DISABLE KEYS */;
INSERT INTO `course_student` VALUES (52001,40001,20002,'2025-11-19 20:17:39',0,0,NULL),(52002,40001,20003,'2025-11-19 20:17:39',0,0,NULL);
/*!40000 ALTER TABLE `course_student` ENABLE KEYS */;

--
-- Table structure for table `knowledge_graph`
--

DROP TABLE IF EXISTS `knowledge_graph`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_graph` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识图谱ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图谱标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '图谱描述',
  `course_id` bigint NOT NULL COMMENT '关联课程ID',
  `graph_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'COURSE' COMMENT '图谱类型：COURSE-课程图谱，CHAPTER-章节图谱',
  `graph_data` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '图谱数据（JSON格式）',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'active' COMMENT '状态：active-活跃，archived-归档',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_course_id` (`course_id`) USING BTREE,
  KEY `idx_creator_id` (`creator_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  CONSTRAINT `fk_knowledge_graph_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_knowledge_graph_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='知识图谱表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `knowledge_graph`
--

/*!40000 ALTER TABLE `knowledge_graph` DISABLE KEYS */;
/*!40000 ALTER TABLE `knowledge_graph` ENABLE KEYS */;

--
-- Table structure for table `knowledge_point`
--

DROP TABLE IF EXISTS `knowledge_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_point` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '所属课程',
  `title` varchar(200) NOT NULL COMMENT '知识点名称（如“二分查找”）',
  `description` text COMMENT '详细解释（AI生成）',
  `level` enum('BASIC','INTERMEDIATE','ADVANCED') DEFAULT 'BASIC' COMMENT '难度',
  `creator_user_id` bigint NOT NULL COMMENT '创建者 user.id',
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
) ENGINE=InnoDB AUTO_INCREMENT=60004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `knowledge_point`
--

/*!40000 ALTER TABLE `knowledge_point` DISABLE KEYS */;
INSERT INTO `knowledge_point` VALUES (60001,40001,'神经网络基础','感知机、前馈网络概念','BASIC',20001,'2025-11-19 19:59:04','2025-11-19 20:17:39',0,NULL),(60002,40001,'梯度下降','优化算法与反向传播','INTERMEDIATE',20001,'2025-11-19 19:59:04','2025-11-19 20:17:39',0,NULL),(60003,40001,'正则化技术','Dropout、L2/L1','INTERMEDIATE',20001,'2025-11-19 19:59:04','2025-11-19 20:17:39',0,NULL);
/*!40000 ALTER TABLE `knowledge_point` ENABLE KEYS */;

--
-- Table structure for table `kp_relation`
--

DROP TABLE IF EXISTS `kp_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kp_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_kp_id` bigint NOT NULL COMMENT '源知识点',
  `to_kp_id` bigint NOT NULL COMMENT '目标知识点',
  `relation_type` enum('PREREQUISITE','BELONGS_TO','EXAMPLE','EXTENSION','SIMILAR') NOT NULL,
  `ai_generated` tinyint(1) DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_relation` (`from_kp_id`,`to_kp_id`,`relation_type`),
  KEY `idx_to` (`to_kp_id`),
  CONSTRAINT `fk_rel_from` FOREIGN KEY (`from_kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rel_to` FOREIGN KEY (`to_kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识点关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kp_relation`
--

/*!40000 ALTER TABLE `kp_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `kp_relation` ENABLE KEYS */;

--
-- Table structure for table `learning_performance_relevance`
--

DROP TABLE IF EXISTS `learning_performance_relevance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_performance_relevance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `behavior_id` bigint NOT NULL COMMENT '行为ID',
  `avg_score` decimal(5,2) DEFAULT NULL COMMENT '平均成绩',
  `exam_count` int DEFAULT '0' COMMENT '考试次数',
  `homework_count` int DEFAULT '0' COMMENT '作业次数',
  `correlation_coefficient` decimal(5,4) DEFAULT NULL COMMENT '相关系数（-1到1）',
  `behavior_grade` enum('excellent','good','average','poor') DEFAULT NULL COMMENT '行为评级',
  `stat_period` varchar(50) DEFAULT NULL COMMENT '统计周期（如：2024-11）',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_course_period` (`student_id`,`course_id`,`stat_period`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习行为与成绩关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `learning_performance_relevance`
--

/*!40000 ALTER TABLE `learning_performance_relevance` DISABLE KEYS */;
/*!40000 ALTER TABLE `learning_performance_relevance` ENABLE KEYS */;

--
-- Table structure for table `personalized_recommendation`
--

DROP TABLE IF EXISTS `personalized_recommendation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `personalized_recommendation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '推荐ID',
  `student_user_id` bigint NOT NULL COMMENT '学生user.id（关联user表）',
  `course_id` bigint NOT NULL COMMENT '所属课程ID（关联course表）',
  `recommend_type` enum('video','exercise','resource','kp_review') NOT NULL COMMENT '推荐类型：视频/专项练习/资源/知识点复习',
  `target_id` bigint NOT NULL COMMENT '推荐目标ID：视频→section.id，练习→assignment.id，资源→course_resource.id，知识点→kp_id',
  `recommend_reason` text COMMENT '推荐理由（AI生成，如“您在图神经网络知识点上表现较弱，建议重新学习第3.2节视频”）',
  `related_kp_ids` varchar(255) DEFAULT NULL COMMENT '关联的知识点ID（多个用逗号分隔，如“101,102”）',
  `status` enum('pending','viewed','completed','expired') NOT NULL DEFAULT 'pending' COMMENT '推荐状态：待查看/已查看/已完成/已过期',
  `priority` tinyint DEFAULT '3' COMMENT '推荐优先级（1-5，5最高）',
  `ai_model_version` varchar(50) DEFAULT NULL COMMENT '生成推荐的AI模型版本（用于追溯）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '推荐生成时间',
  `expire_time` datetime DEFAULT NULL COMMENT '推荐过期时间（如7天后过期）',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_student_course` (`student_user_id`,`course_id`) USING BTREE,
  KEY `idx_recommend_type_status` (`recommend_type`,`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `fk_pr_course` (`course_id`),
  CONSTRAINT `fk_pr_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_pr_student` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI个性化推荐记录表（存储学习建议）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `personalized_recommendation`
--

/*!40000 ALTER TABLE `personalized_recommendation` DISABLE KEYS */;
/*!40000 ALTER TABLE `personalized_recommendation` ENABLE KEYS */;

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干内容',
  `question_type` enum('single','multiple','true_false','blank','short','code') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题型',
  `difficulty` tinyint NOT NULL DEFAULT '3' COMMENT '难度等级，1~5整数',
  `correct_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '标准答案',
  `explanation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '答案解析',
  `knowledge_point` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '知识点',
  `course_id` bigint NOT NULL,
  `chapter_id` bigint NOT NULL,
  `created_by` bigint NOT NULL COMMENT '出题教师ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `course_id` (`course_id`) USING BTREE,
  KEY `chapter_id` (`chapter_id`) USING BTREE,
  KEY `question_ibfk_3` (`created_by`) USING BTREE,
  CONSTRAINT `question_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `question_ibfk_2` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `question_ibfk_3` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=80005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question`
--

/*!40000 ALTER TABLE `question` DISABLE KEYS */;
INSERT INTO `question` VALUES (80001,'请解释感知机模型的基本结构','short',2,'描述输入层、权重、激活函数','考察基础概念','60001',40001,72001,20001,'2025-11-19 20:06:14','2025-11-19 20:17:39',0,NULL),(80002,'梯度下降的关键步骤有哪些？','short',3,'计算梯度、更新参数','考察优化算法','60002',40001,72001,20001,'2025-11-19 20:06:14','2025-11-19 20:17:39',0,NULL);
/*!40000 ALTER TABLE `question` ENABLE KEYS */;

--
-- Table structure for table `question_image`
--

DROP TABLE IF EXISTS `question_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL或路径',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图片说明',
  `sequence` int DEFAULT '1' COMMENT '图片显示顺序',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `question_id` (`question_id`) USING BTREE,
  CONSTRAINT `question_image_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题目图片表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_image`
--

/*!40000 ALTER TABLE `question_image` DISABLE KEYS */;
/*!40000 ALTER TABLE `question_image` ENABLE KEYS */;

--
-- Table structure for table `question_option`
--

DROP TABLE IF EXISTS `question_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_option` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_id` bigint NOT NULL,
  `option_label` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项标识 A/B/C/D/T/F',
  `option_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项内容',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `question_id` (`question_id`) USING BTREE,
  CONSTRAINT `question_option_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题目选项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_option`
--

/*!40000 ALTER TABLE `question_option` DISABLE KEYS */;
/*!40000 ALTER TABLE `question_option` ENABLE KEYS */;

--
-- Table structure for table `question_stats`
--

DROP TABLE IF EXISTS `question_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_stats` (
  `question_id` bigint NOT NULL,
  `answer_count` int DEFAULT '0' COMMENT '答题总数',
  `correct_count` int DEFAULT '0' COMMENT '正确人数',
  `accuracy` decimal(5,2) DEFAULT '0.00' COMMENT '正确率（百分比）',
  PRIMARY KEY (`question_id`) USING BTREE,
  CONSTRAINT `question_stats_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题目统计信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_stats`
--

/*!40000 ALTER TABLE `question_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `question_stats` ENABLE KEYS */;

--
-- Table structure for table `section`
--

DROP TABLE IF EXISTS `section`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `section` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '小节ID，主键',
  `chapter_id` bigint NOT NULL COMMENT '所属章节ID，外键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小节名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '小节简介',
  `video_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '视频播放地址，可对接OSS',
  `duration` int DEFAULT '0' COMMENT '视频时长(秒)',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '小节顺序，用于排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_chapter_id` (`chapter_id`) USING BTREE,
  KEY `idx_sort_order` (`sort_order`) USING BTREE,
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `fk_section_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='课程小节表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `section`
--

/*!40000 ALTER TABLE `section` DISABLE KEYS */;
/*!40000 ALTER TABLE `section` ENABLE KEYS */;

--
-- Table structure for table `section_comment`
--

DROP TABLE IF EXISTS `section_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `section_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID，主键',
  `section_id` bigint NOT NULL COMMENT '所属小节ID，外键',
  `user_id` bigint NOT NULL COMMENT '评论人ID，外键',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，为NULL表示一级评论',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_section_id` (`section_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `section_comment` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_section` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='小节评论表(讨论区)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `section_comment`
--

/*!40000 ALTER TABLE `section_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `section_comment` ENABLE KEYS */;

--
-- Table structure for table `section_kp`
--

DROP TABLE IF EXISTS `section_kp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `section_kp` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `section_id` bigint NOT NULL COMMENT '小节ID',
  `kp_id` bigint NOT NULL COMMENT '知识点ID',
  `sequence` int DEFAULT '1' COMMENT '在小节中的顺序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_section_kp` (`section_id`,`kp_id`),
  KEY `idx_kp` (`kp_id`),
  CONSTRAINT `fk_sk_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sk_section` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小节与知识点关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `section_kp`
--

/*!40000 ALTER TABLE `section_kp` DISABLE KEYS */;
/*!40000 ALTER TABLE `section_kp` ENABLE KEYS */;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (30001,20002,'ENROLLED',NULL,NULL,'2025-11-19 19:59:04','2025-11-19 20:17:39',0,NULL),(30002,20003,'ENROLLED',NULL,NULL,'2025-11-19 19:59:04','2025-11-19 20:17:39',0,NULL);
/*!40000 ALTER TABLE `student` ENABLE KEYS */;

--
-- Table structure for table `student_answer`
--

DROP TABLE IF EXISTS `student_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_user_id` bigint NOT NULL COMMENT '学生 user.id',
  `assignment_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `answer_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '学生答案',
  `is_correct` tinyint(1) DEFAULT NULL COMMENT '是否正确',
  `score` int DEFAULT '0',
  `answer_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `student_id` (`student_user_id`) USING BTREE,
  KEY `assignment_id` (`assignment_id`) USING BTREE,
  KEY `question_id` (`question_id`) USING BTREE,
  CONSTRAINT `fk_student_answer_user` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `student_answer_ibfk_2` FOREIGN KEY (`assignment_id`) REFERENCES `assignment` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `student_answer_ibfk_3` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=84005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学生答题记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_answer`
--

/*!40000 ALTER TABLE `student_answer` DISABLE KEYS */;
INSERT INTO `student_answer` VALUES (84001,20002,70001,80001,'阐述结构',1,45,'2025-11-19 20:06:14',0,NULL),(84002,20002,70001,80002,'列出步骤',1,41,'2025-11-19 20:06:14',0,NULL),(84003,20003,70001,80001,'描述不完整',0,35,'2025-11-19 20:06:14',0,NULL),(84004,20003,70001,80002,'部分步骤',0,37,'2025-11-19 20:06:14',0,NULL);
/*!40000 ALTER TABLE `student_answer` ENABLE KEYS */;

--
-- Table structure for table `student_kp_mastery`
--

DROP TABLE IF EXISTS `student_kp_mastery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_kp_mastery` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_user_id` bigint NOT NULL COMMENT '学生user.id（关联user表）',
  `course_id` bigint NOT NULL COMMENT '所属课程ID（关联course表）',
  `kp_id` bigint NOT NULL COMMENT '知识点ID（关联knowledge_point表）',
  `correct_count` int DEFAULT '0' COMMENT '答对次数',
  `total_count` int DEFAULT '0' COMMENT '总答题次数',
  `accuracy` decimal(5,2) DEFAULT NULL COMMENT '正确率（自动计算）',
  `last_test_score` decimal(5,2) DEFAULT NULL COMMENT '最近一次测试得分',
  `last_test_time` timestamp NULL DEFAULT NULL COMMENT '最近测试时间',
  `trend` enum('improving','stable','declining') DEFAULT NULL COMMENT '趋势',
  `mastery_score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '掌握指标（0-100分）：按权重计算的综合得分',
  `mastery_status` enum('unlearned','learning','mastered','weak') NOT NULL DEFAULT 'unlearned' COMMENT '掌握状态：未学习/学习中/已掌握/薄弱点',
  `weight_factors` json DEFAULT NULL COMMENT '权重因子明细：{"exam_score":0.4,"video_behavior":0.3,"resource_behavior":0.2,"homework_score":0.1}（可配置）',
  `last_updated_by` varchar(50) NOT NULL DEFAULT 'system' COMMENT '最后更新来源：system/ai/job',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后计算更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_student_kp` (`student_user_id`,`kp_id`) USING BTREE COMMENT '确保每个学生-知识点记录唯一',
  KEY `idx_course_kp` (`course_id`,`kp_id`) USING BTREE,
  KEY `idx_mastery_status` (`mastery_status`) USING BTREE,
  KEY `fk_skm_kp` (`kp_id`),
  CONSTRAINT `fk_skm_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_skm_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_skm_student` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=86005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生知识点掌握情况表（支撑知识图谱状态标识）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_kp_mastery`
--

/*!40000 ALTER TABLE `student_kp_mastery` DISABLE KEYS */;
INSERT INTO `student_kp_mastery` VALUES (86001,20002,40001,60001,0,0,NULL,NULL,NULL,'improving',0.85,'learning','{\"exam_score\": 0.4, \"homework_score\": 0.3, \"video_behavior\": 0.2, \"resource_behavior\": 0.1}','system','2025-11-19 19:59:04','2025-11-19 20:17:39',0),(86002,20002,40001,60002,0,0,NULL,NULL,NULL,'stable',0.78,'learning','{\"exam_score\": 0.5, \"homework_score\": 0.3, \"video_behavior\": 0.1, \"resource_behavior\": 0.1}','system','2025-11-19 19:59:04','2025-11-19 20:17:39',0),(86003,20003,40001,60001,0,0,NULL,NULL,NULL,'declining',0.62,'weak','{\"exam_score\": 0.4, \"homework_score\": 0.2, \"video_behavior\": 0.2, \"resource_behavior\": 0.2}','system','2025-11-19 19:59:04','2025-11-19 20:17:39',0),(86004,20003,40001,60002,0,0,NULL,NULL,NULL,'stable',0.55,'weak','{\"exam_score\": 0.4, \"homework_score\": 0.3, \"video_behavior\": 0.1, \"resource_behavior\": 0.2}','system','2025-11-19 19:59:04','2025-11-19 20:17:39',0);
/*!40000 ALTER TABLE `student_kp_mastery` ENABLE KEYS */;

--
-- Table structure for table `student_learning_behavior`
--

DROP TABLE IF EXISTS `student_learning_behavior`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_learning_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '行为ID',
  `student_user_id` bigint NOT NULL COMMENT '学生user.id（关联user表）',
  `course_id` bigint NOT NULL COMMENT '所属课程ID（关联course表）',
  `behavior_type` enum('video_view','resource_view','resource_download','comment') NOT NULL COMMENT '行为类型：视频观看/资源查看/资源下载/评论',
  `target_id` bigint NOT NULL COMMENT '行为目标ID：视频→section.id，资源→course_resource.id，评论→section_comment.id',
  `target_type` enum('section','course_resource','section_comment') NOT NULL COMMENT '目标类型（与target_id对应）',
  `duration` int DEFAULT '0' COMMENT '行为时长（秒）：视频观看/资源查看时长',
  `count` int DEFAULT '1' COMMENT '行为次数：视频重复观看次数/资源重复查看次数',
  `detail` json DEFAULT NULL COMMENT '行为详情：视频→{"start_time":120,"end_time":300,"is_replay":1}（开始秒/结束秒/是否重复）；资源→{"view_pages":"1-5","is_bookmark":0}（查看页数/是否收藏）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '行为发生时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_student_course` (`student_user_id`,`course_id`) USING BTREE,
  KEY `idx_target` (`target_type`,`target_id`) USING BTREE,
  KEY `idx_behavior_type` (`behavior_type`) USING BTREE,
  KEY `fk_sl_behavior_course` (`course_id`),
  CONSTRAINT `fk_sl_behavior_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_sl_behavior_student` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=85003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生学习行为记录表（视频/资源/互动行为）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_learning_behavior`
--

/*!40000 ALTER TABLE `student_learning_behavior` DISABLE KEYS */;
INSERT INTO `student_learning_behavior` VALUES (85001,20002,40001,'video_view',1,'section',3600,1,'{\"progress\": \"100%\"}','2025-11-19 19:59:04','2025-11-19 19:59:04',0),(85002,20003,40001,'video_view',1,'section',2200,1,'{\"progress\": \"65%\"}','2025-11-19 19:59:04','2025-11-19 19:59:04',0);
/*!40000 ALTER TABLE `student_learning_behavior` ENABLE KEYS */;

--
-- Table structure for table `teacher`
--

DROP TABLE IF EXISTS `teacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '教师ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属院系',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职称',
  `education` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学历',
  `specialty` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业领域',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '个人简介',
  `office_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '办公地点',
  `office_hours` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '办公时间',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系邮箱',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT '状态',
  `hire_date` datetime DEFAULT NULL COMMENT '入职日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_id` (`user_id`) USING BTREE,
  KEY `idx_department` (`department`) USING BTREE,
  CONSTRAINT `fk_teacher_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2025005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='教师表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher`
--

/*!40000 ALTER TABLE `teacher` DISABLE KEYS */;
INSERT INTO `teacher` VALUES (10001,20001,'计算机学院','副教授',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'ACTIVE',NULL,'2025-11-19 19:59:04','2025-11-19 20:17:39',0,NULL);
/*!40000 ALTER TABLE `teacher` ENABLE KEYS */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(加密)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `role` enum('STUDENT','TEACHER','ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STUDENT' COMMENT '用户角色: STUDENT=学生, TEACHER=教师, ADMIN=管理员',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  UNIQUE KEY `uk_email` (`email`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (20001,'teacher_zhang','pwd','zhang@example.com','张老师',NULL,'TEACHER','ACTIVE','2025-11-19 19:59:04','2025-11-19 20:17:39'),(20002,'student_li','pwd','li@example.com','李雷',NULL,'STUDENT','ACTIVE','2025-11-19 19:59:04','2025-11-19 20:17:39'),(20003,'student_han','pwd','han@example.com','韩梅梅',NULL,'STUDENT','ACTIVE','2025-11-19 19:59:04','2025-11-19 20:17:39');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;

--
-- Table structure for table `video`
--

DROP TABLE IF EXISTS `video`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '视频ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `title` varchar(200) NOT NULL COMMENT '视频标题',
  `description` text COMMENT '视频描述',
  `file_path` varchar(500) NOT NULL COMMENT '视频文件路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `duration` int DEFAULT NULL COMMENT '时长（秒）',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图片路径',
  `resolution` varchar(20) DEFAULT NULL COMMENT '分辨率（如：1080p）',
  `knowledge_point_ids` json DEFAULT NULL COMMENT '关联的知识点ID列表',
  `status` enum('uploading','processing','published','offline') DEFAULT 'uploading' COMMENT '状态',
  `view_count` int DEFAULT '0' COMMENT '观看次数',
  `uploaded_by` bigint DEFAULT NULL COMMENT '上传者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_status` (`status`),
  KEY `idx_uploaded_by` (`uploaded_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `video`
--

/*!40000 ALTER TABLE `video` DISABLE KEYS */;
/*!40000 ALTER TABLE `video` ENABLE KEYS */;

--
-- Table structure for table `video_learning_behavior`
--

DROP TABLE IF EXISTS `video_learning_behavior`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video_learning_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '行为ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `video_id` bigint NOT NULL COMMENT '视频ID',
  `watch_duration` int DEFAULT '0' COMMENT '观看时长（秒）',
  `video_duration` int DEFAULT NULL COMMENT '视频总时长（秒）',
  `completion_rate` decimal(5,2) DEFAULT NULL COMMENT '完成率（%）',
  `watch_count` int DEFAULT '1' COMMENT '观看次数',
  `heatmap_data` json DEFAULT NULL COMMENT '热力图数据',
  `is_completed` tinyint DEFAULT '0' COMMENT '是否看完：1-是 0-否',
  `fast_forward_count` int DEFAULT '0' COMMENT '快进次数',
  `pause_count` int DEFAULT '0' COMMENT '暂停次数',
  `playback_speed` decimal(2,1) DEFAULT '1.0' COMMENT '播放倍速',
  `first_watch_at` timestamp NULL DEFAULT NULL COMMENT '首次观看时间',
  `last_watch_at` timestamp NULL DEFAULT NULL COMMENT '最近观看时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_video` (`student_id`,`video_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频学习行为表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `video_learning_behavior`
--

/*!40000 ALTER TABLE `video_learning_behavior` DISABLE KEYS */;
/*!40000 ALTER TABLE `video_learning_behavior` ENABLE KEYS */;

--
-- Dumping routines for database 'education_platform'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-20 22:34:17
