/*
SQLyog Ultimate v8.71 
MySQL - 8.0.13 : Database - education_platform_v1
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`education_platform_v1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */;

USE `education_platform_v1`;

/*Table structure for table `ai_grading` */

DROP TABLE IF EXISTS `ai_grading`;

CREATE TABLE `ai_grading` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '批改ID',
  `assignment_submission_id` bigint(20) NOT NULL COMMENT '作业提交表id',
  `content_score` decimal(5,2) DEFAULT NULL COMMENT '内容相关性得分（0-100）',
  `logic_score` decimal(5,2) DEFAULT NULL COMMENT '逻辑结构得分（0-100）',
  `knowledge_score` decimal(5,2) DEFAULT NULL COMMENT '知识点覆盖得分（0-100）',
  `innovation_score` decimal(5,2) DEFAULT NULL COMMENT '创新性得分（0-100）',
  `total_score` decimal(5,2) DEFAULT NULL COMMENT '综合得分',
  `ai_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'AI评语',
  `improvement_suggestions` json DEFAULT NULL COMMENT '改进建议',
  `covered_knowledge_points` json DEFAULT NULL COMMENT '覆盖的知识点',
  `missing_knowledge_points` json DEFAULT NULL COMMENT '缺失的知识点',
  `llm_model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '使用的LLM模型',
  `llm_tokens` int(11) DEFAULT NULL COMMENT '消耗的token数',
  `processing_time` int(11) DEFAULT NULL COMMENT '处理时长（毫秒）',
  `status` enum('pending','processing','completed','failed') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'pending',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '错误信息（如果失败）',
  `teacher_confirmed` tinyint(4) DEFAULT '0' COMMENT '教师是否确认：1-是 0-否',
  `teacher_modified_score` decimal(5,2) DEFAULT NULL COMMENT '教师修改后的分数',
  `teacher_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '教师补充评语',
  `confirmed_by` bigint(20) DEFAULT NULL COMMENT '确认教师ID',
  `confirmed_at` timestamp NULL DEFAULT NULL COMMENT '确认时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='智能批改记录表';

/*Data for the table `ai_grading` */

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
  `attachments` json DEFAULT NULL COMMENT '任务附件列表，支持多文件，格式：[{"name":"文件名.pdf","url":"https://xxx.com/file.pdf"}]',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `course_id` (`course_id`) USING BTREE,
  KEY `fk_assignment_user` (`publisher_user_id`) USING BTREE,
  KEY `idx_deleted` (`is_deleted`) USING BTREE,
  CONSTRAINT `assignment_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_assignment_user` FOREIGN KEY (`publisher_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='作业或考试表';

/*Data for the table `assignment` */

/*Table structure for table `assignment_kp` */

DROP TABLE IF EXISTS `assignment_kp`;

CREATE TABLE `assignment_kp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint(20) DEFAULT NULL COMMENT '作业/考试ID（允许为空，兼容旧数据）',
  `kp_id` bigint(20) DEFAULT NULL COMMENT '知识点ID',
  `sequence` int(11) DEFAULT '1' COMMENT '在任务中的顺序',
  `is_required` tinyint(1) DEFAULT '1' COMMENT '是否必修知识点',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_assignment_kp` (`assignment_id`,`kp_id`) USING BTREE,
  KEY `idx_kp` (`kp_id`) USING BTREE,
  KEY `idx_assignment` (`assignment_id`) USING BTREE,
  CONSTRAINT `fk_ak_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ak_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='任务与知识点关联表（非强制）';

/*Data for the table `assignment_kp` */

/*Table structure for table `assignment_question` */

DROP TABLE IF EXISTS `assignment_question`;

CREATE TABLE `assignment_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint(20) NOT NULL,
  `question_id` bigint(20) NOT NULL,
  `score` int(11) DEFAULT '5' COMMENT '该题满分',
  `sequence` int(11) DEFAULT '1' COMMENT '题目顺序',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `assignment_id` (`assignment_id`) USING BTREE,
  KEY `question_id` (`question_id`) USING BTREE,
  CONSTRAINT `assignment_question_ibfk_1` FOREIGN KEY (`assignment_id`) REFERENCES `assignment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `assignment_question_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='作业题目关联表';

/*Data for the table `assignment_question` */

/*Table structure for table `assignment_submission` */

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
  KEY `idx_deleted` (`is_deleted`) USING BTREE,
  CONSTRAINT `fk_submission_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_submission_student` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='作业提交记录表';

/*Data for the table `assignment_submission` */

/*Table structure for table `chapter` */

DROP TABLE IF EXISTS `chapter`;

CREATE TABLE `chapter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '章节ID，主键',
  `course_id` bigint(20) NOT NULL COMMENT '所属课程ID，外键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '章节名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `sort_order` int(11) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_course_id` (`course_id`) USING BTREE,
  KEY `idx_deleted` (`is_deleted`) USING BTREE,
  CONSTRAINT `fk_chapter_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='课程章节表';

/*Data for the table `chapter` */

insert  into `chapter`(`id`,`course_id`,`title`,`description`,`sort_order`,`create_time`,`update_time`,`is_deleted`,`delete_time`) values (43,30,'2','',1,'2025-11-21 14:48:00','2025-11-21 15:10:50',1,'2025-11-21 15:10:50'),(44,30,'1','',3,'2025-11-21 14:48:22','2025-11-21 14:50:33',0,NULL),(45,30,'3','',2,'2025-11-21 14:50:33','2025-11-21 15:10:50',1,'2025-11-21 15:10:50');

/*Table structure for table `class_student` */

DROP TABLE IF EXISTS `class_student`;

CREATE TABLE `class_student` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL,
  `student_user_id` bigint(20) NOT NULL,
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_class_student` (`class_id`,`student_user_id`) USING BTREE,
  KEY `student_id` (`student_user_id`) USING BTREE,
  CONSTRAINT `fk_class_student_user` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='班级学生关联表';

/*Data for the table `class_student` */

/*Table structure for table `competency_kp_relation` */

DROP TABLE IF EXISTS `competency_kp_relation`;

CREATE TABLE `competency_kp_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `competency_id` bigint(20) NOT NULL COMMENT '能力点ID（关联course_competency表）',
  `kp_id` bigint(20) NOT NULL COMMENT '知识点ID（关联knowledge_point表）',
  `contribution_rate` decimal(3,2) DEFAULT '1.00' COMMENT '知识点对能力点的贡献度（0-1，用于加权计算能力得分）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_competency_kp` (`competency_id`,`kp_id`) USING BTREE COMMENT '同一能力点-知识点关联唯一',
  KEY `idx_kp` (`kp_id`) USING BTREE,
  CONSTRAINT `fk_ckr_competency` FOREIGN KEY (`competency_id`) REFERENCES `course_competency` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ckr_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='能力点-知识点关联表（支撑能力模型与知识点的映射）';

/*Data for the table `competency_kp_relation` */

/*Table structure for table `course` */

DROP TABLE IF EXISTS `course`;

CREATE TABLE `course` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '课程描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课程封面图片',
  `credit` decimal(3,1) DEFAULT '3.0' COMMENT '学分',
  `course_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '必修课' COMMENT '课程类型',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `teacher_user_id` bigint(20) NOT NULL COMMENT '任课教师 user.id',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '未开始' COMMENT '课程状态',
  `term` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学期',
  `student_count` int(11) DEFAULT '0' COMMENT '学生数量',
  `average_score` decimal(5,2) DEFAULT NULL COMMENT '平均分数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记: 0=未删除, 1=已删除',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_term` (`term`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_teacher_user_id` (`teacher_user_id`) USING BTREE,
  KEY `idx_deleted` (`is_deleted`) USING BTREE,
  CONSTRAINT `fk_course_teacher_user` FOREIGN KEY (`teacher_user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='课程表';

/*Data for the table `course` */

insert  into `course`(`id`,`title`,`description`,`cover_image`,`credit`,`course_type`,`start_time`,`end_time`,`teacher_user_id`,`status`,`term`,`student_count`,`average_score`,`create_time`,`update_time`,`is_deleted`,`delete_time`) values (30,'测试课程','123','/profile/upload/2025/11/21/j2cjlogo_20251121152823A001.png','3.0','必修课','2025-11-20 19:06:00','2025-11-27 00:00:00',37,'未开始',NULL,0,NULL,'2025-11-20 19:06:08','2025-11-21 15:28:25',0,NULL);

/*Table structure for table `course_class` */

DROP TABLE IF EXISTS `course_class`;

CREATE TABLE `course_class` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `course_id` bigint(20) DEFAULT NULL,
  `teacher_user_id` bigint(20) NOT NULL COMMENT '教师 user.id',
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
  CONSTRAINT `fk_course_class_teacher_user` FOREIGN KEY (`teacher_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='课程班级表';

/*Data for the table `course_class` */

/*Table structure for table `course_competency` */

DROP TABLE IF EXISTS `course_competency`;

CREATE TABLE `course_competency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '能力点ID',
  `course_id` bigint(20) NOT NULL COMMENT '所属课程ID（关联course表）',
  `competency_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '能力点名称（如“数据结构理解能力”“算法优化能力”）',
  `competency_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '能力点描述',
  `sort_order` int(11) DEFAULT '1' COMMENT '展示顺序（雷达图维度排序）',
  `weight` decimal(3,2) DEFAULT '1.00' COMMENT '能力点权重（用于综合能力计算）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_course_competency` (`course_id`,`competency_name`) USING BTREE COMMENT '同一课程能力点名称唯一',
  KEY `idx_course` (`course_id`) USING BTREE,
  CONSTRAINT `fk_cc_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='课程能力点表（定义能力模型维度）';

/*Data for the table `course_competency` */

/*Table structure for table `course_enrollment_request` */

DROP TABLE IF EXISTS `course_enrollment_request`;

CREATE TABLE `course_enrollment_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `student_user_id` bigint(20) NOT NULL COMMENT '学生 user.id',
  `course_id` bigint(20) NOT NULL COMMENT '申请加入的课程ID',
  `status` tinyint(4) DEFAULT '0' COMMENT '申请状态：0=待审核 1=已通过 2=已拒绝',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '学生申请理由',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '教师审核意见',
  `submit_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_student_course` (`student_user_id`,`course_id`) USING BTREE,
  KEY `course_id` (`course_id`) USING BTREE,
  CONSTRAINT `course_enrollment_request_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_enrollment_student_user` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学生选课申请表';

/*Data for the table `course_enrollment_request` */

/*Table structure for table `course_resource` */

DROP TABLE IF EXISTS `course_resource`;

CREATE TABLE `course_resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `course_id` bigint(20) NOT NULL COMMENT '所属课程ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资源名称',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件类型，如pdf、doc、ppt等',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小(字节)',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件URL',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '资源描述',
  `download_count` int(11) DEFAULT '0' COMMENT '下载次数',
  `upload_user_id` bigint(20) NOT NULL COMMENT '上传用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_course_id` (`course_id`) USING BTREE COMMENT '课程ID索引',
  KEY `idx_upload_user_id` (`upload_user_id`) USING BTREE COMMENT '上传用户ID索引'
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='课程资源表';

/*Data for the table `course_resource` */

/*Table structure for table `course_resource_kp` */

DROP TABLE IF EXISTS `course_resource_kp`;

CREATE TABLE `course_resource_kp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_id` bigint(20) DEFAULT NULL COMMENT '资源ID（允许为空）',
  `kp_id` bigint(20) DEFAULT NULL COMMENT '知识点ID',
  `is_confirmed` tinyint(1) DEFAULT '0' COMMENT '教师是否确认',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_resource_kp` (`resource_id`,`kp_id`) USING BTREE,
  KEY `idx_kp` (`kp_id`) USING BTREE,
  KEY `idx_confirmed` (`is_confirmed`) USING BTREE,
  CONSTRAINT `fk_crk_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_crk_resource` FOREIGN KEY (`resource_id`) REFERENCES `course_resource` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='课程资源与知识点智能关联表（非强制）';

/*Data for the table `course_resource_kp` */

/*Table structure for table `course_student` */

DROP TABLE IF EXISTS `course_student`;

CREATE TABLE `course_student` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(20) NOT NULL,
  `student_user_id` bigint(20) NOT NULL COMMENT '学生 user.id',
  `enroll_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `collected` int(11) DEFAULT '0' COMMENT '课程是否被该学生收藏，1为被收藏，0为未被收藏',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_course_student` (`course_id`,`student_user_id`) USING BTREE,
  KEY `student_id` (`student_user_id`) USING BTREE,
  CONSTRAINT `course_student_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_course_student_user` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学生选课表';

/*Data for the table `course_student` */

/*Table structure for table `gen_table` */

DROP TABLE IF EXISTS `gen_table`;

CREATE TABLE `gen_table` (
  `table_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='代码生成业务表';

/*Data for the table `gen_table` */

/*Table structure for table `gen_table_column` */

DROP TABLE IF EXISTS `gen_table_column`;

CREATE TABLE `gen_table_column` (
  `column_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint(20) DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '字典类型',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='代码生成业务表字段';

/*Data for the table `gen_table_column` */

/*Table structure for table `knowledge_graph` */

DROP TABLE IF EXISTS `knowledge_graph`;

CREATE TABLE `knowledge_graph` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '知识图谱ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图谱标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '图谱描述',
  `course_id` bigint(20) NOT NULL COMMENT '关联课程ID',
  `graph_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'COURSE' COMMENT '图谱类型：COURSE-课程图谱，CHAPTER-章节图谱',
  `graph_data` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '图谱数据（JSON格式）',
  `creator_id` bigint(20) NOT NULL COMMENT '创建者ID',
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

/*Data for the table `knowledge_graph` */

/*Table structure for table `knowledge_point` */

DROP TABLE IF EXISTS `knowledge_point`;

CREATE TABLE `knowledge_point` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `course_id` bigint(20) NOT NULL COMMENT '所属课程',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识点名称（如“二分查找”）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '详细解释（AI生成）',
  `level` enum('BASIC','INTERMEDIATE','ADVANCED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'BASIC' COMMENT '难度',
  `creator_user_id` bigint(20) NOT NULL COMMENT '创建者 user.id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_course` (`course_id`) USING BTREE,
  KEY `idx_creator` (`creator_user_id`) USING BTREE,
  KEY `idx_deleted` (`is_deleted`) USING BTREE,
  CONSTRAINT `fk_kp_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_kp_creator` FOREIGN KEY (`creator_user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='知识点表';

/*Data for the table `knowledge_point` */

/*Table structure for table `kp_relation` */

DROP TABLE IF EXISTS `kp_relation`;

CREATE TABLE `kp_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_kp_id` bigint(20) NOT NULL COMMENT '源知识点',
  `to_kp_id` bigint(20) NOT NULL COMMENT '目标知识点',
  `relation_type` enum('PREREQUISITE','BELONGS_TO','EXAMPLE','EXTENSION','SIMILAR') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ai_generated` tinyint(1) DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_relation` (`from_kp_id`,`to_kp_id`,`relation_type`) USING BTREE,
  KEY `idx_to` (`to_kp_id`) USING BTREE,
  CONSTRAINT `fk_rel_from` FOREIGN KEY (`from_kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_rel_to` FOREIGN KEY (`to_kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='知识点关系表';

/*Data for the table `kp_relation` */

/*Table structure for table `learning_performance_relevance` */

DROP TABLE IF EXISTS `learning_performance_relevance`;

CREATE TABLE `learning_performance_relevance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `behavior_id` bigint(20) NOT NULL COMMENT '行为ID',
  `avg_score` decimal(5,2) DEFAULT NULL COMMENT '平均成绩',
  `exam_count` int(11) DEFAULT '0' COMMENT '考试次数',
  `homework_count` int(11) DEFAULT '0' COMMENT '作业次数',
  `correlation_coefficient` decimal(5,4) DEFAULT NULL COMMENT '相关系数（-1到1）',
  `behavior_grade` enum('excellent','good','average','poor') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '行为评级',
  `stat_period` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '统计周期（如：2024-11）',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_student_course_period` (`student_id`,`course_id`,`stat_period`) USING BTREE,
  KEY `idx_student_id` (`student_id`) USING BTREE,
  KEY `idx_course_id` (`course_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学习行为与成绩关联表';

/*Data for the table `learning_performance_relevance` */

/*Table structure for table `personalized_recommendation` */

DROP TABLE IF EXISTS `personalized_recommendation`;

CREATE TABLE `personalized_recommendation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '推荐ID',
  `student_user_id` bigint(20) NOT NULL COMMENT '学生user.id（关联user表）',
  `course_id` bigint(20) NOT NULL COMMENT '所属课程ID（关联course表）',
  `recommend_type` enum('video','exercise','resource','kp_review') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '推荐类型：视频/专项练习/资源/知识点复习',
  `target_id` bigint(20) NOT NULL COMMENT '推荐目标ID：视频→section.id，练习→assignment.id，资源→course_resource.id，知识点→kp_id',
  `recommend_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '推荐理由（AI生成，如“您在图神经网络知识点上表现较弱，建议重新学习第3.2节视频”）',
  `related_kp_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '关联的知识点ID（多个用逗号分隔，如“101,102”）',
  `status` enum('pending','viewed','completed','expired') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '推荐状态：待查看/已查看/已完成/已过期',
  `priority` tinyint(4) DEFAULT '3' COMMENT '推荐优先级（1-5，5最高）',
  `ai_model_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成推荐的AI模型版本（用于追溯）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '推荐生成时间',
  `expire_time` datetime DEFAULT NULL COMMENT '推荐过期时间（如7天后过期）',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_student_course` (`student_user_id`,`course_id`) USING BTREE,
  KEY `idx_recommend_type_status` (`recommend_type`,`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `fk_pr_course` (`course_id`) USING BTREE,
  CONSTRAINT `fk_pr_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_pr_student` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI个性化推荐记录表（存储学习建议）';

/*Data for the table `personalized_recommendation` */

/*Table structure for table `qrtz_blob_triggers` */

DROP TABLE IF EXISTS `qrtz_blob_triggers`;

CREATE TABLE `qrtz_blob_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `blob_data` blob COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='Blob类型的触发器表';

/*Data for the table `qrtz_blob_triggers` */

/*Table structure for table `qrtz_calendars` */

DROP TABLE IF EXISTS `qrtz_calendars`;

CREATE TABLE `qrtz_calendars` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`,`calendar_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='日历信息表';

/*Data for the table `qrtz_calendars` */

/*Table structure for table `qrtz_cron_triggers` */

DROP TABLE IF EXISTS `qrtz_cron_triggers`;

CREATE TABLE `qrtz_cron_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='Cron类型的触发器表';

/*Data for the table `qrtz_cron_triggers` */

/*Table structure for table `qrtz_fired_triggers` */

DROP TABLE IF EXISTS `qrtz_fired_triggers`;

CREATE TABLE `qrtz_fired_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint(20) NOT NULL COMMENT '触发的时间',
  `sched_time` bigint(20) NOT NULL COMMENT '定时器制定的时间',
  `priority` int(11) NOT NULL COMMENT '优先级',
  `state` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`,`entry_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='已触发的触发器表';

/*Data for the table `qrtz_fired_triggers` */

/*Table structure for table `qrtz_job_details` */

DROP TABLE IF EXISTS `qrtz_job_details`;

CREATE TABLE `qrtz_job_details` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='任务详细信息表';

/*Data for the table `qrtz_job_details` */

/*Table structure for table `qrtz_locks` */

DROP TABLE IF EXISTS `qrtz_locks`;

CREATE TABLE `qrtz_locks` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`,`lock_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='存储的悲观锁信息表';

/*Data for the table `qrtz_locks` */

/*Table structure for table `qrtz_paused_trigger_grps` */

DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;

CREATE TABLE `qrtz_paused_trigger_grps` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`,`trigger_group`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='暂停的触发器表';

/*Data for the table `qrtz_paused_trigger_grps` */

/*Table structure for table `qrtz_scheduler_state` */

DROP TABLE IF EXISTS `qrtz_scheduler_state`;

CREATE TABLE `qrtz_scheduler_state` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint(20) NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint(20) NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`,`instance_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='调度器状态表';

/*Data for the table `qrtz_scheduler_state` */

/*Table structure for table `qrtz_simple_triggers` */

DROP TABLE IF EXISTS `qrtz_simple_triggers`;

CREATE TABLE `qrtz_simple_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint(20) NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint(20) NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint(20) NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='简单触发器的信息表';

/*Data for the table `qrtz_simple_triggers` */

/*Table structure for table `qrtz_simprop_triggers` */

DROP TABLE IF EXISTS `qrtz_simprop_triggers`;

CREATE TABLE `qrtz_simprop_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int(11) DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int(11) DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint(20) DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint(20) DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='同步机制的行锁表';

/*Data for the table `qrtz_simprop_triggers` */

/*Table structure for table `qrtz_triggers` */

DROP TABLE IF EXISTS `qrtz_triggers`;

CREATE TABLE `qrtz_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint(20) DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint(20) DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint(20) NOT NULL COMMENT '开始时间',
  `end_time` bigint(20) DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint(6) DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  KEY `sched_name` (`sched_name`,`job_name`,`job_group`) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='触发器详细信息表';

/*Data for the table `qrtz_triggers` */

/*Table structure for table `question` */

DROP TABLE IF EXISTS `question`;

CREATE TABLE `question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干内容',
  `question_type` enum('single','multiple','true_false','blank','short','code') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题型',
  `difficulty` tinyint(4) NOT NULL DEFAULT '3' COMMENT '难度等级，1~5整数',
  `correct_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '标准答案',
  `explanation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '答案解析',
  `knowledge_point` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '知识点',
  `course_id` bigint(20) NOT NULL,
  `chapter_id` bigint(20) NOT NULL,
  `created_by` bigint(20) NOT NULL COMMENT '出题教师ID',
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
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题目表';

/*Data for the table `question` */

/*Table structure for table `question_image` */

DROP TABLE IF EXISTS `question_image`;

CREATE TABLE `question_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `question_id` bigint(20) NOT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL或路径',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图片说明',
  `sequence` int(11) DEFAULT '1' COMMENT '图片显示顺序',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `question_id` (`question_id`) USING BTREE,
  CONSTRAINT `question_image_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题目图片表';

/*Data for the table `question_image` */

/*Table structure for table `question_option` */

DROP TABLE IF EXISTS `question_option`;

CREATE TABLE `question_option` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `question_id` bigint(20) NOT NULL,
  `option_label` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项标识 A/B/C/D/T/F',
  `option_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选项内容',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `question_id` (`question_id`) USING BTREE,
  CONSTRAINT `question_option_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题目选项表';

/*Data for the table `question_option` */

/*Table structure for table `question_stats` */

DROP TABLE IF EXISTS `question_stats`;

CREATE TABLE `question_stats` (
  `question_id` bigint(20) NOT NULL,
  `answer_count` int(11) DEFAULT '0' COMMENT '答题总数',
  `correct_count` int(11) DEFAULT '0' COMMENT '正确人数',
  `accuracy` decimal(5,2) DEFAULT '0.00' COMMENT '正确率（百分比）',
  PRIMARY KEY (`question_id`) USING BTREE,
  CONSTRAINT `question_stats_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='题目统计信息表';

/*Data for the table `question_stats` */

/*Table structure for table `section` */

DROP TABLE IF EXISTS `section`;

CREATE TABLE `section` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '小节ID，主键',
  `chapter_id` bigint(20) NOT NULL COMMENT '所属章节ID，外键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '小节名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '小节简介',
  `video_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '视频播放地址，可对接OSS',
  `duration` int(11) DEFAULT '0' COMMENT '视频时长(秒)',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '小节顺序，用于排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_chapter_id` (`chapter_id`) USING BTREE,
  KEY `idx_sort_order` (`sort_order`) USING BTREE,
  KEY `idx_deleted` (`is_deleted`) USING BTREE,
  CONSTRAINT `fk_section_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='课程小节表';

/*Data for the table `section` */

/*Table structure for table `section_comment` */

DROP TABLE IF EXISTS `section_comment`;

CREATE TABLE `section_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID，主键',
  `section_id` bigint(20) NOT NULL COMMENT '所属小节ID，外键',
  `user_id` bigint(20) NOT NULL COMMENT '评论人ID，外键',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID，为NULL表示一级评论',
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

/*Data for the table `section_comment` */

/*Table structure for table `section_kp` */

DROP TABLE IF EXISTS `section_kp`;

CREATE TABLE `section_kp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `section_id` bigint(20) NOT NULL COMMENT '小节ID',
  `kp_id` bigint(20) NOT NULL COMMENT '知识点ID',
  `sequence` int(11) DEFAULT '1' COMMENT '在小节中的顺序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_section_kp` (`section_id`,`kp_id`) USING BTREE,
  KEY `idx_kp` (`kp_id`) USING BTREE,
  CONSTRAINT `fk_sk_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_sk_section` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='小节与知识点关联表';

/*Data for the table `section_kp` */

/*Table structure for table `student` */

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

/*Data for the table `student` */

/*Table structure for table `student_answer` */

DROP TABLE IF EXISTS `student_answer`;

CREATE TABLE `student_answer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_user_id` bigint(20) NOT NULL COMMENT '学生 user.id',
  `assignment_id` bigint(20) NOT NULL,
  `question_id` bigint(20) NOT NULL,
  `answer_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '学生答案',
  `is_correct` tinyint(1) DEFAULT NULL COMMENT '是否正确',
  `score` int(11) DEFAULT '0',
  `answer_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `delete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `student_id` (`student_user_id`) USING BTREE,
  KEY `assignment_id` (`assignment_id`) USING BTREE,
  KEY `question_id` (`question_id`) USING BTREE,
  CONSTRAINT `fk_student_answer_user` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `student_answer_ibfk_2` FOREIGN KEY (`assignment_id`) REFERENCES `assignment` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `student_answer_ibfk_3` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学生答题记录表';

/*Data for the table `student_answer` */

/*Table structure for table `student_kp_mastery` */

DROP TABLE IF EXISTS `student_kp_mastery`;

CREATE TABLE `student_kp_mastery` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_user_id` bigint(20) NOT NULL COMMENT '学生user.id（关联user表）',
  `course_id` bigint(20) NOT NULL COMMENT '所属课程ID（关联course表）',
  `kp_id` bigint(20) NOT NULL COMMENT '知识点ID（关联knowledge_point表）',
  `correct_count` int(11) DEFAULT '0' COMMENT '答对次数',
  `total_count` int(11) DEFAULT '0' COMMENT '总答题次数',
  `accuracy` decimal(5,2) DEFAULT NULL COMMENT '正确率（自动计算）',
  `last_test_score` decimal(5,2) DEFAULT NULL COMMENT '最近一次测试得分',
  `last_test_time` timestamp NULL DEFAULT NULL COMMENT '最近测试时间',
  `trend` enum('improving','stable','declining') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '趋势',
  `mastery_score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '掌握指标（0-100分）：按权重计算的综合得分',
  `mastery_status` enum('unlearned','learning','mastered','weak') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'unlearned' COMMENT '掌握状态：未学习/学习中/已掌握/薄弱点',
  `weight_factors` json DEFAULT NULL COMMENT '权重因子明细：{"exam_score":0.4,"video_behavior":0.3,"resource_behavior":0.2,"homework_score":0.1}（可配置）',
  `last_updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'system' COMMENT '最后更新来源：system/ai/job',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后计算更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_student_kp` (`student_user_id`,`kp_id`) USING BTREE COMMENT '确保每个学生-知识点记录唯一',
  KEY `idx_course_kp` (`course_id`,`kp_id`) USING BTREE,
  KEY `idx_mastery_status` (`mastery_status`) USING BTREE,
  KEY `fk_skm_kp` (`kp_id`) USING BTREE,
  CONSTRAINT `fk_skm_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_skm_kp` FOREIGN KEY (`kp_id`) REFERENCES `knowledge_point` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_skm_student` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学生知识点掌握情况表（支撑知识图谱状态标识）';

/*Data for the table `student_kp_mastery` */

/*Table structure for table `student_learning_behavior` */

DROP TABLE IF EXISTS `student_learning_behavior`;

CREATE TABLE `student_learning_behavior` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '行为ID',
  `student_user_id` bigint(20) NOT NULL COMMENT '学生user.id（关联user表）',
  `course_id` bigint(20) NOT NULL COMMENT '所属课程ID（关联course表）',
  `behavior_type` enum('video_view','resource_view','resource_download','comment') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '行为类型：视频观看/资源查看/资源下载/评论',
  `target_id` bigint(20) NOT NULL COMMENT '行为目标ID：视频→section.id，资源→course_resource.id，评论→section_comment.id',
  `target_type` enum('section','course_resource','section_comment') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标类型（与target_id对应）',
  `duration` int(11) DEFAULT '0' COMMENT '行为时长（秒）：视频观看/资源查看时长',
  `count` int(11) DEFAULT '1' COMMENT '行为次数：视频重复观看次数/资源重复查看次数',
  `detail` json DEFAULT NULL COMMENT '行为详情：视频→{"start_time":120,"end_time":300,"is_replay":1}（开始秒/结束秒/是否重复）；资源→{"view_pages":"1-5","is_bookmark":0}（查看页数/是否收藏）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '行为发生时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_student_course` (`student_user_id`,`course_id`) USING BTREE,
  KEY `idx_target` (`target_type`,`target_id`) USING BTREE,
  KEY `idx_behavior_type` (`behavior_type`) USING BTREE,
  KEY `fk_sl_behavior_course` (`course_id`) USING BTREE,
  CONSTRAINT `fk_sl_behavior_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_sl_behavior_student` FOREIGN KEY (`student_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学生学习行为记录表（视频/资源/互动行为）';

/*Data for the table `student_learning_behavior` */

/*Table structure for table `sys_config` */

DROP TABLE IF EXISTS `sys_config`;

CREATE TABLE `sys_config` (
  `config_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='参数配置表';

/*Data for the table `sys_config` */

insert  into `sys_config`(`config_id`,`config_name`,`config_key`,`config_value`,`config_type`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values (1,'主框架页-默认皮肤样式名称','sys.index.skinName','skin-blue','Y','admin','2025-11-18 16:54:30','',NULL,'蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow'),(2,'用户管理-账号初始密码','sys.user.initPassword','123456','Y','admin','2025-11-18 16:54:30','',NULL,'初始化密码 123456'),(3,'主框架页-侧边栏主题','sys.index.sideTheme','theme-dark','Y','admin','2025-11-18 16:54:30','',NULL,'深色主题theme-dark，浅色主题theme-light'),(4,'账号自助-验证码开关','sys.account.captchaEnabled','true','Y','admin','2025-11-18 16:54:30','',NULL,'是否开启验证码功能（true开启，false关闭）'),(5,'账号自助-是否开启用户注册功能','sys.account.registerUser','true','Y','admin','2025-11-18 16:54:30','',NULL,'是否开启注册用户功能（true开启，false关闭）'),(6,'用户登录-黑名单列表','sys.login.blackIPList','','Y','admin','2025-11-18 16:54:30','',NULL,'设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）'),(7,'用户管理-初始密码修改策略','sys.account.initPasswordModify','1','Y','admin','2025-11-18 16:54:30','',NULL,'0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框'),(8,'用户管理-账号密码更新周期','sys.account.passwordValidateDays','0','Y','admin','2025-11-18 16:54:30','',NULL,'密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');

/*Table structure for table `sys_dept` */

DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE `sys_dept` (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '部门名称',
  `order_num` int(11) DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='部门表';

/*Data for the table `sys_dept` */

insert  into `sys_dept`(`dept_id`,`parent_id`,`ancestors`,`dept_name`,`order_num`,`leader`,`phone`,`email`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`) values (100,0,'0','若依科技',0,'若依','15888888888','ry@qq.com','0','0','admin','2025-11-18 16:54:29','',NULL),(101,100,'0,100','深圳总公司',1,'若依','15888888888','ry@qq.com','0','0','admin','2025-11-18 16:54:29','',NULL),(102,100,'0,100','长沙分公司',2,'若依','15888888888','ry@qq.com','0','0','admin','2025-11-18 16:54:29','',NULL),(103,101,'0,100,101','研发部门',1,'若依','15888888888','ry@qq.com','0','0','admin','2025-11-18 16:54:29','',NULL),(104,101,'0,100,101','市场部门',2,'若依','15888888888','ry@qq.com','0','0','admin','2025-11-18 16:54:29','',NULL),(105,101,'0,100,101','测试部门',3,'若依','15888888888','ry@qq.com','0','0','admin','2025-11-18 16:54:29','',NULL),(106,101,'0,100,101','财务部门',4,'若依','15888888888','ry@qq.com','0','0','admin','2025-11-18 16:54:29','',NULL),(107,101,'0,100,101','运维部门',5,'若依','15888888888','ry@qq.com','0','0','admin','2025-11-18 16:54:29','',NULL),(108,102,'0,100,102','市场部门',1,'若依','15888888888','ry@qq.com','0','0','admin','2025-11-18 16:54:29','',NULL),(109,102,'0,100,102','财务部门',2,'若依','15888888888','ry@qq.com','0','0','admin','2025-11-18 16:54:29','',NULL);

/*Table structure for table `sys_dict_data` */

DROP TABLE IF EXISTS `sys_dict_data`;

CREATE TABLE `sys_dict_data` (
  `dict_code` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int(11) DEFAULT '0' COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='字典数据表';

/*Data for the table `sys_dict_data` */

insert  into `sys_dict_data`(`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values (1,1,'男','0','sys_user_sex','','','Y','0','admin','2025-11-18 16:54:30','',NULL,'性别男'),(2,2,'女','1','sys_user_sex','','','N','0','admin','2025-11-18 16:54:30','',NULL,'性别女'),(3,3,'未知','2','sys_user_sex','','','N','0','admin','2025-11-18 16:54:30','',NULL,'性别未知'),(4,1,'显示','0','sys_show_hide','','primary','Y','0','admin','2025-11-18 16:54:30','',NULL,'显示菜单'),(5,2,'隐藏','1','sys_show_hide','','danger','N','0','admin','2025-11-18 16:54:30','',NULL,'隐藏菜单'),(6,1,'正常','0','sys_normal_disable','','primary','Y','0','admin','2025-11-18 16:54:30','',NULL,'正常状态'),(7,2,'停用','1','sys_normal_disable','','danger','N','0','admin','2025-11-18 16:54:30','',NULL,'停用状态'),(8,1,'正常','0','sys_job_status','','primary','Y','0','admin','2025-11-18 16:54:30','',NULL,'正常状态'),(9,2,'暂停','1','sys_job_status','','danger','N','0','admin','2025-11-18 16:54:30','',NULL,'停用状态'),(10,1,'默认','DEFAULT','sys_job_group','','','Y','0','admin','2025-11-18 16:54:30','',NULL,'默认分组'),(11,2,'系统','SYSTEM','sys_job_group','','','N','0','admin','2025-11-18 16:54:30','',NULL,'系统分组'),(12,1,'是','Y','sys_yes_no','','primary','Y','0','admin','2025-11-18 16:54:30','',NULL,'系统默认是'),(13,2,'否','N','sys_yes_no','','danger','N','0','admin','2025-11-18 16:54:30','',NULL,'系统默认否'),(14,1,'通知','1','sys_notice_type','','warning','Y','0','admin','2025-11-18 16:54:30','',NULL,'通知'),(15,2,'公告','2','sys_notice_type','','success','N','0','admin','2025-11-18 16:54:30','',NULL,'公告'),(16,1,'正常','0','sys_notice_status','','primary','Y','0','admin','2025-11-18 16:54:30','',NULL,'正常状态'),(17,2,'关闭','1','sys_notice_status','','danger','N','0','admin','2025-11-18 16:54:30','',NULL,'关闭状态'),(18,99,'其他','0','sys_oper_type','','info','N','0','admin','2025-11-18 16:54:30','',NULL,'其他操作'),(19,1,'新增','1','sys_oper_type','','info','N','0','admin','2025-11-18 16:54:30','',NULL,'新增操作'),(20,2,'修改','2','sys_oper_type','','info','N','0','admin','2025-11-18 16:54:30','',NULL,'修改操作'),(21,3,'删除','3','sys_oper_type','','danger','N','0','admin','2025-11-18 16:54:30','',NULL,'删除操作'),(22,4,'授权','4','sys_oper_type','','primary','N','0','admin','2025-11-18 16:54:30','',NULL,'授权操作'),(23,5,'导出','5','sys_oper_type','','warning','N','0','admin','2025-11-18 16:54:30','',NULL,'导出操作'),(24,6,'导入','6','sys_oper_type','','warning','N','0','admin','2025-11-18 16:54:30','',NULL,'导入操作'),(25,7,'强退','7','sys_oper_type','','danger','N','0','admin','2025-11-18 16:54:30','',NULL,'强退操作'),(26,8,'生成代码','8','sys_oper_type','','warning','N','0','admin','2025-11-18 16:54:30','',NULL,'生成操作'),(27,9,'清空数据','9','sys_oper_type','','danger','N','0','admin','2025-11-18 16:54:30','',NULL,'清空操作'),(28,1,'成功','0','sys_common_status','','primary','N','0','admin','2025-11-18 16:54:30','',NULL,'正常状态'),(29,2,'失败','1','sys_common_status','','danger','N','0','admin','2025-11-18 16:54:30','',NULL,'停用状态');

/*Table structure for table `sys_dict_type` */

DROP TABLE IF EXISTS `sys_dict_type`;

CREATE TABLE `sys_dict_type` (
  `dict_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE KEY `dict_type` (`dict_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='字典类型表';

/*Data for the table `sys_dict_type` */

insert  into `sys_dict_type`(`dict_id`,`dict_name`,`dict_type`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values (1,'用户性别','sys_user_sex','0','admin','2025-11-18 16:54:30','',NULL,'用户性别列表'),(2,'菜单状态','sys_show_hide','0','admin','2025-11-18 16:54:30','',NULL,'菜单状态列表'),(3,'系统开关','sys_normal_disable','0','admin','2025-11-18 16:54:30','',NULL,'系统开关列表'),(4,'任务状态','sys_job_status','0','admin','2025-11-18 16:54:30','',NULL,'任务状态列表'),(5,'任务分组','sys_job_group','0','admin','2025-11-18 16:54:30','',NULL,'任务分组列表'),(6,'系统是否','sys_yes_no','0','admin','2025-11-18 16:54:30','',NULL,'系统是否列表'),(7,'通知类型','sys_notice_type','0','admin','2025-11-18 16:54:30','',NULL,'通知类型列表'),(8,'通知状态','sys_notice_status','0','admin','2025-11-18 16:54:30','',NULL,'通知状态列表'),(9,'操作类型','sys_oper_type','0','admin','2025-11-18 16:54:30','',NULL,'操作类型列表'),(10,'系统状态','sys_common_status','0','admin','2025-11-18 16:54:30','',NULL,'登录状态列表');

/*Table structure for table `sys_job` */

DROP TABLE IF EXISTS `sys_job`;

CREATE TABLE `sys_job` (
  `job_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='定时任务调度表';

/*Data for the table `sys_job` */

insert  into `sys_job`(`job_id`,`job_name`,`job_group`,`invoke_target`,`cron_expression`,`misfire_policy`,`concurrent`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values (1,'系统默认（无参）','DEFAULT','ryTask.ryNoParams','0/10 * * * * ?','3','1','1','admin','2025-11-18 16:54:30','',NULL,''),(2,'系统默认（有参）','DEFAULT','ryTask.ryParams(\'ry\')','0/15 * * * * ?','3','1','1','admin','2025-11-18 16:54:30','',NULL,''),(3,'系统默认（多参）','DEFAULT','ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)','0/20 * * * * ?','3','1','1','admin','2025-11-18 16:54:30','',NULL,'');

/*Table structure for table `sys_job_log` */

DROP TABLE IF EXISTS `sys_job_log`;

CREATE TABLE `sys_job_log` (
  `job_log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='定时任务调度日志表';

/*Data for the table `sys_job_log` */

/*Table structure for table `sys_logininfor` */

DROP TABLE IF EXISTS `sys_logininfor`;

CREATE TABLE `sys_logininfor` (
  `info_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  KEY `idx_sys_logininfor_s` (`status`) USING BTREE,
  KEY `idx_sys_logininfor_lt` (`login_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=197 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='系统访问记录';

/*Data for the table `sys_logininfor` */

insert  into `sys_logininfor`(`info_id`,`user_name`,`ipaddr`,`login_location`,`browser`,`os`,`status`,`msg`,`login_time`) values (100,'admin','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-18 17:18:12'),(101,'admin','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-18 17:20:27'),(102,'wangwu','127.0.0.1','内网IP','Chrome 14','Windows 10','1','用户不存在/密码错误','2025-11-18 17:20:37'),(103,'wangwu','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-18 17:21:23'),(104,'wangwu','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-18 17:21:46'),(105,'wangwu','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-18 17:21:59'),(106,'wangwu','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-18 17:22:54'),(107,'wangwu','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-18 17:23:05'),(108,'wangwu','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-18 17:23:58'),(109,'admin','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-18 17:24:02'),(110,'admin','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-18 17:24:33'),(111,'wangwu','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-18 17:24:45'),(112,'wangwu','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-18 17:50:46'),(113,'admin','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-18 17:50:50'),(114,'admin','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-18 17:54:34'),(115,'test-student','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-18 17:54:50'),(116,'test-student','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-18 18:06:36'),(117,'admin','127.0.0.1','内网IP','Chrome 12','Windows 10','1','验证码错误','2025-11-18 18:16:36'),(118,'admin','127.0.0.1','内网IP','Chrome 12','Windows 10','1','验证码错误','2025-11-18 18:16:39'),(119,'admin','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 18:16:44'),(120,'admin','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 18:17:55'),(121,'admin','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 18:24:37'),(122,'admin','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 18:24:50'),(123,'admin','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 18:27:15'),(124,'admin','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 18:27:56'),(125,'1234','127.0.0.1','内网IP','Chrome 12','Windows 10','0','注册成功','2025-11-18 18:48:02'),(126,'1234','127.0.0.1','内网IP','Chrome 12','Windows 10','1','用户不存在/密码错误','2025-11-18 18:48:18'),(127,'1234','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 18:48:27'),(128,'1234','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 18:50:21'),(129,'1234','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 18:50:33'),(130,'1234','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 18:55:02'),(131,'ljy','127.0.0.1','内网IP','Chrome 12','Windows 10','0','注册成功','2025-11-18 18:55:17'),(132,'ljy','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 18:55:34'),(133,'admin','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 20:55:27'),(134,'admin','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 21:05:26'),(135,'123','127.0.0.1','内网IP','Chrome 12','Windows 10','0','注册成功','2025-11-18 21:05:42'),(136,'123aa','127.0.0.1','内网IP','Chrome 12','Windows 10','0','注册成功','2025-11-18 21:06:13'),(137,'123aa','127.0.0.1','内网IP','Chrome 12','Windows 10','0','注册成功','2025-11-18 21:07:49'),(138,'123aa','127.0.0.1','内网IP','Chrome 12','Windows 10','0','注册成功','2025-11-18 21:08:35'),(139,'wma','127.0.0.1','内网IP','Chrome 12','Windows 10','0','注册成功','2025-11-18 21:10:26'),(140,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','注册成功','2025-11-18 21:17:37'),(141,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 21:17:56'),(142,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 22:41:53'),(143,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 22:42:01'),(144,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','1','验证码已失效','2025-11-18 22:46:38'),(145,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 22:46:42'),(146,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 22:46:47'),(147,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 22:46:54'),(148,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 22:53:27'),(149,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 22:53:32'),(150,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 22:53:38'),(151,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','1','身份验证失败：您选择的是教师登录，但您的账号角色是学生','2025-11-18 22:54:11'),(152,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 22:54:16'),(153,'qwhrei','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 22:54:40'),(154,'wangwu','127.0.0.1','内网IP','Chrome 12','Windows 10','1','身份验证失败：您选择的是学生登录，但您的账号角色是教师','2025-11-18 22:54:49'),(155,'wangwu','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 22:54:54'),(156,'wangwu','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-18 22:55:01'),(157,'we','127.0.0.1','内网IP','Chrome 12','Windows 10','0','注册成功','2025-11-18 22:55:20'),(158,'we','127.0.0.1','内网IP','Chrome 12','Windows 10','1','身份验证失败：您选择的是教师登录，但您的账号角色是学生','2025-11-18 22:55:34'),(159,'we','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-18 22:55:39'),(160,'wangwu','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-19 16:39:01'),(161,'wangwu','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-19 16:39:06'),(162,'wangwu','127.0.0.1','内网IP','Chrome 12','Windows 10','1','身份验证失败：您选择的是学生登录，但您的账号角色是教师','2025-11-19 16:39:16'),(163,'we','127.0.0.1','内网IP','Chrome 12','Windows 10','1','身份验证失败：您选择的是教师登录，但您的账号角色是学生','2025-11-19 16:39:24'),(164,'we','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-19 16:39:30'),(165,'we','127.0.0.1','内网IP','Chrome 12','Windows 10','0','退出成功','2025-11-19 16:40:14'),(166,'123456','127.0.0.1','内网IP','Chrome 12','Windows 10','0','注册成功','2025-11-19 16:40:36'),(167,'123456','127.0.0.1','内网IP','Chrome 12','Windows 10','0','登录成功','2025-11-19 16:40:47'),(168,'testtc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','注册成功','2025-11-19 17:09:36'),(169,'testtc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-19 17:10:02'),(170,'testtc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-19 17:13:58'),(171,'testtc','127.0.0.1','内网IP','Chrome 14','Windows 10','1','用户不存在/密码错误','2025-11-19 17:14:04'),(172,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','1','验证码错误','2025-11-19 17:14:07'),(173,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','1','身份验证失败：您选择的是学生登录，但您的账号角色是教师','2025-11-19 17:14:11'),(174,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-19 17:14:16'),(175,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-19 18:25:10'),(176,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-19 18:37:32'),(177,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-19 18:37:36'),(178,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-19 18:37:51'),(179,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-19 18:37:57'),(180,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-19 18:38:57'),(181,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-19 18:39:00'),(182,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-19 18:44:33'),(183,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-19 18:44:40'),(184,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-20 12:55:07'),(185,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-20 13:03:13'),(186,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-20 14:29:37'),(187,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-20 15:35:42'),(188,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-20 15:48:39'),(189,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-20 15:48:42'),(190,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-20 17:50:11'),(191,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-20 18:20:48'),(192,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','退出成功','2025-11-20 20:50:22'),(193,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-20 20:50:26'),(194,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-20 23:13:34'),(195,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-21 12:43:32'),(196,'tc','127.0.0.1','内网IP','Chrome 14','Windows 10','0','登录成功','2025-11-21 14:35:22');

/*Table structure for table `sys_menu` */

DROP TABLE IF EXISTS `sys_menu`;

CREATE TABLE `sys_menu` (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int(11) DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '路由名称',
  `is_frame` int(11) DEFAULT '1' COMMENT '是否为外链（0是 1否）',
  `is_cache` int(11) DEFAULT '0' COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5010 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='菜单权限表';

/*Data for the table `sys_menu` */

insert  into `sys_menu`(`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values (1,'系统管理',0,1,'system',NULL,'','',1,0,'M','0','0','','system','admin','2025-11-18 16:54:30','tc','2025-11-20 13:32:43','系统管理目录'),(2,'系统监控',0,2,'monitor',NULL,'','',1,0,'M','0','0','','monitor','admin','2025-11-18 16:54:30','',NULL,'系统监控目录'),(3,'系统工具',0,3,'tool',NULL,'','',1,0,'M','0','0','','tool','admin','2025-11-18 16:54:30','',NULL,'系统工具目录'),(4,'若依官网',0,4,'http://ruoyi.vip',NULL,'','',0,0,'M','0','0','','guide','admin','2025-11-18 16:54:30','',NULL,'若依官网地址'),(100,'用户管理',1,1,'user','system/user/index','','',1,0,'C','0','0','system:user:list','user','admin','2025-11-18 16:54:30','',NULL,'用户管理菜单'),(101,'角色管理',1,2,'role','system/role/index','','',1,0,'C','0','0','system:role:list','peoples','admin','2025-11-18 16:54:30','',NULL,'角色管理菜单'),(102,'菜单管理',1,3,'menu','system/menu/index','','',1,0,'C','0','0','system:menu:list','tree-table','admin','2025-11-18 16:54:30','',NULL,'菜单管理菜单'),(103,'部门管理',1,4,'dept','system/dept/index','','',1,0,'C','0','0','system:dept:list','tree','admin','2025-11-18 16:54:30','',NULL,'部门管理菜单'),(104,'岗位管理',1,5,'post','system/post/index','','',1,0,'C','0','0','system:post:list','post','admin','2025-11-18 16:54:30','',NULL,'岗位管理菜单'),(105,'字典管理',1,6,'dict','system/dict/index','','',1,0,'C','0','0','system:dict:list','dict','admin','2025-11-18 16:54:30','',NULL,'字典管理菜单'),(106,'参数设置',1,7,'config','system/config/index','','',1,0,'C','0','0','system:config:list','edit','admin','2025-11-18 16:54:30','',NULL,'参数设置菜单'),(107,'通知公告',1,8,'notice','system/notice/index','','',1,0,'C','0','0','system:notice:list','message','admin','2025-11-18 16:54:30','',NULL,'通知公告菜单'),(108,'日志管理',1,9,'log','','','',1,0,'M','0','0','','log','admin','2025-11-18 16:54:30','',NULL,'日志管理菜单'),(109,'在线用户',2,1,'online','monitor/online/index','','',1,0,'C','0','0','monitor:online:list','online','admin','2025-11-18 16:54:30','',NULL,'在线用户菜单'),(110,'定时任务',2,2,'job','monitor/job/index','','',1,0,'C','0','0','monitor:job:list','job','admin','2025-11-18 16:54:30','',NULL,'定时任务菜单'),(111,'数据监控',2,3,'druid','monitor/druid/index','','',1,0,'C','0','0','monitor:druid:list','druid','admin','2025-11-18 16:54:30','',NULL,'数据监控菜单'),(112,'服务监控',2,4,'server','monitor/server/index','','',1,0,'C','0','0','monitor:server:list','server','admin','2025-11-18 16:54:30','',NULL,'服务监控菜单'),(113,'缓存监控',2,5,'cache','monitor/cache/index','','',1,0,'C','0','0','monitor:cache:list','redis','admin','2025-11-18 16:54:30','',NULL,'缓存监控菜单'),(114,'缓存列表',2,6,'cacheList','monitor/cache/list','','',1,0,'C','0','0','monitor:cache:list','redis-list','admin','2025-11-18 16:54:30','',NULL,'缓存列表菜单'),(115,'表单构建',3,1,'build','tool/build/index','','',1,0,'C','0','0','tool:build:list','build','admin','2025-11-18 16:54:30','',NULL,'表单构建菜单'),(116,'代码生成',3,2,'gen','tool/gen/index','','',1,0,'C','0','0','tool:gen:list','code','admin','2025-11-18 16:54:30','',NULL,'代码生成菜单'),(117,'系统接口',3,3,'swagger','tool/swagger/index','','',1,0,'C','0','0','tool:swagger:list','swagger','admin','2025-11-18 16:54:30','',NULL,'系统接口菜单'),(500,'操作日志',108,1,'operlog','monitor/operlog/index','','',1,0,'C','0','0','monitor:operlog:list','form','admin','2025-11-18 16:54:30','',NULL,'操作日志菜单'),(501,'登录日志',108,2,'logininfor','monitor/logininfor/index','','',1,0,'C','0','0','monitor:logininfor:list','logininfor','admin','2025-11-18 16:54:30','',NULL,'登录日志菜单'),(1000,'用户查询',100,1,'','','','',1,0,'F','0','0','system:user:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1001,'用户新增',100,2,'','','','',1,0,'F','0','0','system:user:add','#','admin','2025-11-18 16:54:30','',NULL,''),(1002,'用户修改',100,3,'','','','',1,0,'F','0','0','system:user:edit','#','admin','2025-11-18 16:54:30','',NULL,''),(1003,'用户删除',100,4,'','','','',1,0,'F','0','0','system:user:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1004,'用户导出',100,5,'','','','',1,0,'F','0','0','system:user:export','#','admin','2025-11-18 16:54:30','',NULL,''),(1005,'用户导入',100,6,'','','','',1,0,'F','0','0','system:user:import','#','admin','2025-11-18 16:54:30','',NULL,''),(1006,'重置密码',100,7,'','','','',1,0,'F','0','0','system:user:resetPwd','#','admin','2025-11-18 16:54:30','',NULL,''),(1007,'角色查询',101,1,'','','','',1,0,'F','0','0','system:role:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1008,'角色新增',101,2,'','','','',1,0,'F','0','0','system:role:add','#','admin','2025-11-18 16:54:30','',NULL,''),(1009,'角色修改',101,3,'','','','',1,0,'F','0','0','system:role:edit','#','admin','2025-11-18 16:54:30','',NULL,''),(1010,'角色删除',101,4,'','','','',1,0,'F','0','0','system:role:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1011,'角色导出',101,5,'','','','',1,0,'F','0','0','system:role:export','#','admin','2025-11-18 16:54:30','',NULL,''),(1012,'菜单查询',102,1,'','','','',1,0,'F','0','0','system:menu:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1013,'菜单新增',102,2,'','','','',1,0,'F','0','0','system:menu:add','#','admin','2025-11-18 16:54:30','',NULL,''),(1014,'菜单修改',102,3,'','','','',1,0,'F','0','0','system:menu:edit','#','admin','2025-11-18 16:54:30','',NULL,''),(1015,'菜单删除',102,4,'','','','',1,0,'F','0','0','system:menu:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1016,'部门查询',103,1,'','','','',1,0,'F','0','0','system:dept:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1017,'部门新增',103,2,'','','','',1,0,'F','0','0','system:dept:add','#','admin','2025-11-18 16:54:30','',NULL,''),(1018,'部门修改',103,3,'','','','',1,0,'F','0','0','system:dept:edit','#','admin','2025-11-18 16:54:30','',NULL,''),(1019,'部门删除',103,4,'','','','',1,0,'F','0','0','system:dept:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1020,'岗位查询',104,1,'','','','',1,0,'F','0','0','system:post:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1021,'岗位新增',104,2,'','','','',1,0,'F','0','0','system:post:add','#','admin','2025-11-18 16:54:30','',NULL,''),(1022,'岗位修改',104,3,'','','','',1,0,'F','0','0','system:post:edit','#','admin','2025-11-18 16:54:30','',NULL,''),(1023,'岗位删除',104,4,'','','','',1,0,'F','0','0','system:post:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1024,'岗位导出',104,5,'','','','',1,0,'F','0','0','system:post:export','#','admin','2025-11-18 16:54:30','',NULL,''),(1025,'字典查询',105,1,'#','','','',1,0,'F','0','0','system:dict:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1026,'字典新增',105,2,'#','','','',1,0,'F','0','0','system:dict:add','#','admin','2025-11-18 16:54:30','',NULL,''),(1027,'字典修改',105,3,'#','','','',1,0,'F','0','0','system:dict:edit','#','admin','2025-11-18 16:54:30','',NULL,''),(1028,'字典删除',105,4,'#','','','',1,0,'F','0','0','system:dict:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1029,'字典导出',105,5,'#','','','',1,0,'F','0','0','system:dict:export','#','admin','2025-11-18 16:54:30','',NULL,''),(1030,'参数查询',106,1,'#','','','',1,0,'F','0','0','system:config:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1031,'参数新增',106,2,'#','','','',1,0,'F','0','0','system:config:add','#','admin','2025-11-18 16:54:30','',NULL,''),(1032,'参数修改',106,3,'#','','','',1,0,'F','0','0','system:config:edit','#','admin','2025-11-18 16:54:30','',NULL,''),(1033,'参数删除',106,4,'#','','','',1,0,'F','0','0','system:config:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1034,'参数导出',106,5,'#','','','',1,0,'F','0','0','system:config:export','#','admin','2025-11-18 16:54:30','',NULL,''),(1035,'公告查询',107,1,'#','','','',1,0,'F','0','0','system:notice:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1036,'公告新增',107,2,'#','','','',1,0,'F','0','0','system:notice:add','#','admin','2025-11-18 16:54:30','',NULL,''),(1037,'公告修改',107,3,'#','','','',1,0,'F','0','0','system:notice:edit','#','admin','2025-11-18 16:54:30','',NULL,''),(1038,'公告删除',107,4,'#','','','',1,0,'F','0','0','system:notice:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1039,'操作查询',500,1,'#','','','',1,0,'F','0','0','monitor:operlog:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1040,'操作删除',500,2,'#','','','',1,0,'F','0','0','monitor:operlog:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1041,'日志导出',500,3,'#','','','',1,0,'F','0','0','monitor:operlog:export','#','admin','2025-11-18 16:54:30','',NULL,''),(1042,'登录查询',501,1,'#','','','',1,0,'F','0','0','monitor:logininfor:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1043,'登录删除',501,2,'#','','','',1,0,'F','0','0','monitor:logininfor:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1044,'日志导出',501,3,'#','','','',1,0,'F','0','0','monitor:logininfor:export','#','admin','2025-11-18 16:54:30','',NULL,''),(1045,'账户解锁',501,4,'#','','','',1,0,'F','0','0','monitor:logininfor:unlock','#','admin','2025-11-18 16:54:30','',NULL,''),(1046,'在线查询',109,1,'#','','','',1,0,'F','0','0','monitor:online:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1047,'批量强退',109,2,'#','','','',1,0,'F','0','0','monitor:online:batchLogout','#','admin','2025-11-18 16:54:30','',NULL,''),(1048,'单条强退',109,3,'#','','','',1,0,'F','0','0','monitor:online:forceLogout','#','admin','2025-11-18 16:54:30','',NULL,''),(1049,'任务查询',110,1,'#','','','',1,0,'F','0','0','monitor:job:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1050,'任务新增',110,2,'#','','','',1,0,'F','0','0','monitor:job:add','#','admin','2025-11-18 16:54:30','',NULL,''),(1051,'任务修改',110,3,'#','','','',1,0,'F','0','0','monitor:job:edit','#','admin','2025-11-18 16:54:30','',NULL,''),(1052,'任务删除',110,4,'#','','','',1,0,'F','0','0','monitor:job:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1053,'状态修改',110,5,'#','','','',1,0,'F','0','0','monitor:job:changeStatus','#','admin','2025-11-18 16:54:30','',NULL,''),(1054,'任务导出',110,6,'#','','','',1,0,'F','0','0','monitor:job:export','#','admin','2025-11-18 16:54:30','',NULL,''),(1055,'生成查询',116,1,'#','','','',1,0,'F','0','0','tool:gen:query','#','admin','2025-11-18 16:54:30','',NULL,''),(1056,'生成修改',116,2,'#','','','',1,0,'F','0','0','tool:gen:edit','#','admin','2025-11-18 16:54:30','',NULL,''),(1057,'生成删除',116,3,'#','','','',1,0,'F','0','0','tool:gen:remove','#','admin','2025-11-18 16:54:30','',NULL,''),(1058,'导入代码',116,4,'#','','','',1,0,'F','0','0','tool:gen:import','#','admin','2025-11-18 16:54:30','',NULL,''),(1059,'预览代码',116,5,'#','','','',1,0,'F','0','0','tool:gen:preview','#','admin','2025-11-18 16:54:30','',NULL,''),(1060,'生成代码',116,6,'#','','','',1,0,'F','0','0','tool:gen:code','#','admin','2025-11-18 16:54:30','',NULL,''),(2000,'课程管理',0,1,'course','course/index',NULL,'',1,0,'C','0','0','','education','admin','2025-11-20 12:54:45','tc','2025-11-20 13:30:13','课程管理目录'),(5009,'课程详情',0,2,'detail/:id','course/detail',NULL,'',1,0,'C','1','0','','','admin','2025-11-20 20:22:47','tc','2025-11-20 21:30:22','课程详情页面');

/*Table structure for table `sys_notice` */

DROP TABLE IF EXISTS `sys_notice`;

CREATE TABLE `sys_notice` (
  `notice_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='通知公告表';

/*Data for the table `sys_notice` */

insert  into `sys_notice`(`notice_id`,`notice_title`,`notice_type`,`notice_content`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values (1,'温馨提醒：2018-07-01 若依新版本发布啦','2','新版本内容','0','admin','2025-11-18 16:54:30','',NULL,'管理员'),(2,'维护通知：2018-07-01 若依系统凌晨维护','1','维护内容','0','admin','2025-11-18 16:54:30','',NULL,'管理员');

/*Table structure for table `sys_oper_log` */

DROP TABLE IF EXISTS `sys_oper_log`;

CREATE TABLE `sys_oper_log` (
  `oper_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '模块标题',
  `business_type` int(11) DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '请求方式',
  `operator_type` int(11) DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '返回参数',
  `status` int(11) DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint(20) DEFAULT '0' COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  KEY `idx_sys_oper_log_bt` (`business_type`) USING BTREE,
  KEY `idx_sys_oper_log_s` (`status`) USING BTREE,
  KEY `idx_sys_oper_log_ot` (`oper_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=215 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='操作日志记录';

/*Data for the table `sys_oper_log` */

insert  into `sys_oper_log`(`oper_id`,`title`,`business_type`,`method`,`request_method`,`operator_type`,`oper_name`,`dept_name`,`oper_url`,`oper_ip`,`oper_location`,`oper_param`,`json_result`,`status`,`error_msg`,`oper_time`,`cost_time`) values (100,'个人信息',2,'com.ruoyi.web.controller.system.SysProfileController.updatePwd()','PUT',1,'wangwu','研发部门','/system/user/profile/updatePwd','127.0.0.1','内网IP','{}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 17:21:36',388),(101,'个人信息',2,'com.ruoyi.web.controller.system.SysProfileController.updatePwd()','PUT',1,'wangwu','研发部门','/system/user/profile/updatePwd','127.0.0.1','内网IP','{}','{\"msg\":\"修改密码失败，旧密码错误\",\"code\":500}',0,NULL,'2025-11-18 17:21:39',132),(102,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 16:54:30\",\"dataScope\":\"2\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"remark\":\"普通角色\",\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":2,\"status\":\"0\",\"updateBy\":\"admin\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 17:24:29',128),(103,'用户管理',1,'com.ruoyi.web.controller.system.SysUserController.add()','POST',1,'admin','研发部门','/system/user','127.0.0.1','内网IP','{\"admin\":false,\"createBy\":\"admin\",\"nickName\":\"测试学生\",\"params\":{},\"postIds\":[],\"roleIds\":[100],\"status\":\"0\",\"userId\":206,\"userName\":\"test-student\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 17:52:39',356),(104,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 17:47:44\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[],\"params\":{},\"roleId\":101,\"roleKey\":\"teacher\",\"roleName\":\"教师角色\",\"roleSort\":9,\"status\":\"0\",\"updateBy\":\"admin\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 17:54:18',42),(105,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 17:47:44\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":101,\"roleKey\":\"teacher\",\"roleName\":\"教师角色\",\"roleSort\":9,\"status\":\"0\",\"updateBy\":\"admin\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 17:54:24',55),(106,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 17:47:44\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":100,\"roleKey\":\"student\",\"roleName\":\"测试学生角色\",\"roleSort\":10,\"status\":\"0\",\"updateBy\":\"admin\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 17:54:30',47),(107,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 16:54:30\",\"dataScope\":\"2\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"remark\":\"普通角色\",\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":2,\"status\":\"0\",\"updateBy\":\"admin\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 18:27:30',162),(108,'个人信息',2,'com.ruoyi.web.controller.system.SysProfileController.updateProfile()','PUT',1,'1234',NULL,'/system/user/profile','127.0.0.1','内网IP','{\"admin\":false,\"email\":\"qwkhrkhe@qq.com\",\"nickName\":\"lll\",\"params\":{},\"phonenumber\":\"19823452345\",\"sex\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 18:49:46',72),(109,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 17:47:44\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":100,\"roleKey\":\"student\",\"roleName\":\"学生角色\",\"roleSort\":10,\"status\":\"0\",\"updateBy\":\"admin\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 20:57:21',97),(110,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 17:47:44\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":100,\"roleKey\":\"student\",\"roleName\":\"学生角色\",\"roleSort\":10,\"status\":\"0\",\"updateBy\":\"admin\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 20:57:31',65),(111,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 17:47:44\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":100,\"roleKey\":\"student\",\"roleName\":\"学生角色\",\"roleSort\":3,\"status\":\"0\",\"updateBy\":\"admin\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 20:59:20',53),(112,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.edit()','PUT',1,'admin','研发部门','/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 17:47:44\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":100,\"roleKey\":\"student\",\"roleName\":\"学生角色\",\"roleSort\":3,\"status\":\"0\",\"updateBy\":\"admin\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-18 20:59:30',44),(113,'个人信息',2,'com.ruoyi.web.controller.system.SysProfileController.updateProfile()','PUT',1,'123456',NULL,'/system/user/profile','127.0.0.1','内网IP','{\"admin\":false,\"email\":\"2138642@qq.con\",\"nickName\":\"123456\",\"params\":{},\"phonenumber\":\"15425356782\",\"sex\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-19 16:42:16',49),(114,'个人信息',2,'com.ruoyi.web.controller.system.SysProfileController.updateProfile()','PUT',1,'123456',NULL,'/system/user/profile','127.0.0.1','内网IP','{\"admin\":false,\"email\":\"2138642@qq.con\",\"nickName\":\"123456\",\"params\":{},\"phonenumber\":\"15425356782\",\"sex\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-19 16:42:18',36),(115,'菜单管理',1,'com.ruoyi.web.controller.system.SysMenuController.add()','POST',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createBy\":\"tc\",\"icon\":\"education\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"课程管理\",\"menuType\":\"M\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"user\",\"status\":\"0\",\"visible\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-19 18:26:59',140),(116,'菜单管理',1,'com.ruoyi.web.controller.system.SysMenuController.add()','POST',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"icon\":\"education\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"课程管理\",\"menuType\":\"M\",\"orderNum\":2,\"params\":{},\"parentId\":0,\"path\":\"user\",\"status\":\"0\",\"visible\":\"0\"}','{\"msg\":\"新增菜单\'课程管理\'失败，菜单名称已存在\",\"code\":500}',0,NULL,'2025-11-19 18:28:44',12),(117,'菜单管理',1,'com.ruoyi.web.controller.system.SysMenuController.add()','POST',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"icon\":\"education\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"课程管理\",\"menuType\":\"M\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"user\",\"status\":\"0\",\"visible\":\"0\"}','{\"msg\":\"新增菜单\'课程管理\'失败，菜单名称已存在\",\"code\":500}',0,NULL,'2025-11-19 18:30:59',4),(118,'菜单管理',1,'com.ruoyi.web.controller.system.SysMenuController.add()','POST',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createBy\":\"tc\",\"icon\":\"education\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"课程管理1\",\"menuType\":\"M\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"user\",\"status\":\"0\",\"visible\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-19 18:31:05',27),(119,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2025-11-20 12:54:45\",\"icon\":\"education\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2000,\"menuName\":\"课程管理\",\"menuType\":\"M\",\"orderNum\":2,\"params\":{},\"parentId\":0,\"path\":\"course\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"tc\",\"visible\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 13:29:52',110),(120,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2025-11-20 12:54:45\",\"icon\":\"education\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2000,\"menuName\":\"课程管理\",\"menuType\":\"M\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"course\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"tc\",\"visible\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 13:30:13',120),(121,'菜单管理',3,'com.ruoyi.web.controller.system.SysMenuController.remove()','DELETE',1,'tc',NULL,'/system/menu/2001','127.0.0.1','内网IP','2001','{\"msg\":\"菜单已分配,不允许删除\",\"code\":601}',0,NULL,'2025-11-20 13:31:54',13),(122,'菜单管理',3,'com.ruoyi.web.controller.system.SysMenuController.remove()','DELETE',1,'tc',NULL,'/system/menu/2001','127.0.0.1','内网IP','2001','{\"msg\":\"菜单已分配,不允许删除\",\"code\":601}',0,NULL,'2025-11-20 13:32:03',4),(123,'菜单管理',3,'com.ruoyi.web.controller.system.SysMenuController.remove()','DELETE',1,'tc',NULL,'/system/menu/2000','127.0.0.1','内网IP','2000','{\"msg\":\"存在子菜单,不允许删除\",\"code\":601}',0,NULL,'2025-11-20 13:32:09',3),(124,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"course/index\",\"createTime\":\"2025-11-20 12:54:45\",\"icon\":\"list\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2001,\"menuName\":\"课程列表\",\"menuType\":\"M\",\"orderNum\":1,\"params\":{},\"parentId\":2000,\"path\":\"list\",\"perms\":\"course:course:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"tc\",\"visible\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 13:32:19',57),(125,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"createTime\":\"2025-11-18 16:54:30\",\"icon\":\"system\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":1,\"menuName\":\"系统管理\",\"menuType\":\"M\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"system\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"tc\",\"visible\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 13:32:43',72),(126,'菜单管理',3,'com.ruoyi.web.controller.system.SysMenuController.remove()','DELETE',1,'tc',NULL,'/system/menu/2001','127.0.0.1','内网IP','2001','{\"msg\":\"菜单已分配,不允许删除\",\"code\":601}',0,NULL,'2025-11-20 13:33:06',8),(127,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"course/index\",\"createTime\":\"2025-11-20 12:54:45\",\"icon\":\"list\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2001,\"menuName\":\"课程列表\",\"menuType\":\"M\",\"orderNum\":1,\"params\":{},\"parentId\":0,\"path\":\"list\",\"perms\":\"course:course:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"tc\",\"visible\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 13:33:10',18),(128,'菜单管理',3,'com.ruoyi.web.controller.system.SysMenuController.remove()','DELETE',1,'tc',NULL,'/system/menu/2001','127.0.0.1','内网IP','2001','{\"msg\":\"菜单已分配,不允许删除\",\"code\":601}',0,NULL,'2025-11-20 13:33:31',6),(129,'课程',1,'com.ruoyi.web.controller.system.CourseController.add()','POST',1,'tc',NULL,'/course','127.0.0.1','内网IP','{\"courseType\":\"必修课\",\"coverImage\":\"\",\"credit\":3,\"description\":\"123\",\"endTime\":\"2025-11-27 00:00:00\",\"id\":30,\"params\":{},\"startTime\":\"2025-11-20 19:06:00\",\"status\":\"未开始\",\"studentCount\":0,\"teacherUserId\":37,\"title\":\"测试课程\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 19:06:08',214),(130,'课程',2,'com.ruoyi.web.controller.system.CourseController.edit()','PUT',1,'tc',NULL,'/course','127.0.0.1','内网IP','{\"courseType\":\"必修课\",\"coverImage\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAPgAAADhCAYAAADlCHhYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAALvCSURBVHhe7P15kGVJdt4H/tz93vvW2LfMyD0rs/bqqq7u6h2NBhpobAQxoChRHIGaIUVKNM3Qxmaj2dDGRjQbmkRyhmPSiJyhKAMowigNF3ATQexNAGwAvXdX9VL7mpV77BFvuZv7mT/c73svXkTkVplV1Vnxpd2M9+67i1+/5/Nz/Pjx4+pn/+aqcIhDHOK+hB7fcYhDHOL+wSHBD3GI+xiHBD/EIe5jHBL8EIe4j3FI8EMc4j7GIcEPcYj7GIcEP8Qh7mMcEvwQh7iPcUjwQxziPsYhwQ9xiPsYhwQ/xCHuYxwS/BCHuI9xSPBDHOI+xiHBD3GI+xiHBD/EIe5jHBL8EIe4j3FI8EO8N3hHaUZUEF01/sMhxnBI8EO8+5BATbnJhvObcshgUwgaQYM6FN+b4bCGDvEeQCGoEQ1sQBlQChSIAtSIipfRrw6lHAoH4gAJ/w6xH9RhTrZDvHdwKMlRWKBAkQMlitL/LAowCBEiEaJqiIpBR+MXQtC72oRDeBwS/BDvEjQgKAqgALFEqk+dHepqhUa8TSPq0Ywz6rHFiZCXil5Ro5u3yMo2vXKKjAWsaXmi4xuAYZ/8EOM4JPgh3h2IoKVD7K4wFV/ixFzGkemChQnL3GTJTBsasUXrjNg4lDaIiilsxOaOY7snXN8UVjs1Lq1HXFxL6HOCgqM4PRn644dOt3EcEvwQ9xAOpMRID+NWmDCX+cg5y0dOdznWvkpbr5EkJVo7tBLQgijfqwaFOG96e+IKShT9PGK93+Jqb5mvvVLjexcadOUEpZ7HqQmEZLwQH2gcEvwQ9wgORZ/YrTEfv87TJ9/mhx51LLa2iFWf2GRoXYL2vjXPaYXD4RAQg5MaaQqRUSRRgaFECZQqonQJ/axFt5zht7/Z59mrZ1kpzpPrJSAaOPGUfLC1+iHBD3EPIBhZYVq9wmPHVvnI6W0eOrLDVLRCpDNvTWsN2oycE4ioBIeiKBJ+9d+8zB/84SvML0zxUz/5BE89sYhyOyhVIjgoFc4mbJdzPH9tiq+/2uS7lxbp8BjOTIehNOv75x9QopuHfvwv/9XxnYc4xB1BQJETyTVm9fN84dHr/MTjVzg3+zatuIMxDmX0kNyiB15yMCjlHWYKQz81/NL/8BW+83zOGxdzvv3cq5x94CRLSy2UduF2Dq0cNdNjeXKH0zMpsxMRF67ukNsWohJQ7gPthDsk+CHuGpQUJG6FE7Uv8ec+l/LMiRWma6tEUYHWGqUNaIPSgXBKUzrN1jZ0+hodxRijUUCiExaXjvHVb75Av2jR6dd44YXXePLpB5iYjFCqRCuNUgqlHEZKmknJ8mzBUmuHq2s9ukUbp2qgNKIU6gNI8kOCvysYDeq4G7ib17pbEGrubR6a/g5/4mMdzs9epGm20JFFGRW0dnCYKd/ftlLn2efW+MVf/iP+53/9HS5e6nL27Gma9QilhKUjM0xOT/Gd775BUcTsdAuuXLnKxz56hkZSokSFmlCgFJqSRPrMtYWFacPmzhYbvTqOFpCM1Nv7sf7uDQ4JfhfhI7BGxGfg3ag8SWNQVYiWRUmJJke5EPghcoOhHz+m/L6BFESywtH6c/yHn1jl/OxVmkkXZcSb5EaF56yeRwOafh7xj//pc/zel66wutnmtdfX2Vzf4KMffYLIWLROOXnyKHnqeOml1xA0Vy6t8tlPn+XIQuQjWZWPihtQV0FkChbbHZZmIi6vOzb7TZyaCO+g2t5H9XcPcUjwuwU1JJ2PqKocO/sR1KEoUZKjydGuR40tWmwwU99hIikoyxwrifcGD0K0qmvJUD73u/y7AKkaMnEYt86J2lf5',NULL,1,'\r\n### Error updating database.  Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column \'cover_image\' at row 1\r\n### The error may exist in file [E:\\JProject\\RuoYi-Vue-master\\ruoyi-system\\target\\classes\\mapper\\system\\CourseMapper.xml]\r\n### The error may involve com.ruoyi.system.mapper.CourseMapper.updateCourse-Inline\r\n### The error occurred while setting parameters\r\n### SQL: update course          SET title = ?,              description = ?,              cover_image = ?,              credit = ?,              course_type = ?,              start_time = ?,              end_time = ?,              status = ?,                           student_count = ?,                           update_time = sysdate()          where id = ? and is_deleted = 0\r\n### Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column \'cover_image\' at row 1\n; Data truncation: Data too long for column \'cover_image\' at row 1; nested exception is com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column \'cover_image\' at row 1','2025-11-20 19:08:05',161),(131,'课程',2,'com.ruoyi.web.controller.system.CourseController.edit()','PUT',1,'tc',NULL,'/course','127.0.0.1','内网IP','{\"courseType\":\"必修课\",\"coverImage\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAPgAAADhCAYAAADlCHhYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAALvCSURBVHhe7P15kGVJdt4H/tz93vvW2LfMyD0rs/bqqq7u6h2NBhpobAQxoChRHIGaIUVKNM3Qxmaj2dDGRjQbmkRyhmPSiJyhKAMowigNF3ATQexNAGwAvXdX9VL7mpV77BFvuZv7mT/c73svXkTkVplV1Vnxpd2M9+67i1+/5/Nz/Pjx4+pn/+aqcIhDHOK+hB7fcYhDHOL+wSHBD3GI+xiHBD/EIe5jHBL8EIe4j3FI8EMc4j7GIcEPcYj7GIcEP8Qh7mMcEvwQh7iPcUjwQxziPsYhwQ9xiPsYhwQ/xCHuYxwS/BCHuI9xSPBDHOI+xiHBD3GI+xiHBD/EIe5jHBL8EIe4j3FI8EO8N3hHaUZUEF01/sMhxnBI8EO8+5BATbnJhvObcshgUwgaQYM6FN+b4bCGDvEeQCGoEQ1sQBlQChSIAtSIipfRrw6lHAoH4gAJ/w6xH9RhTrZDvHdwKMlRWKBAkQMlitL/LAowCBEiEaJqiIpBR+MXQtC72oRDeBwS/BDvEjQgKAqgALFEqk+dHepqhUa8TSPq0Ywz6rHFiZCXil5Ro5u3yMo2vXKKjAWsaXmi4xuAYZ/8EOM4JPgh3h2IoKVD7K4wFV/ixFzGkemChQnL3GTJTBsasUXrjNg4lDaIiilsxOaOY7snXN8UVjs1Lq1HXFxL6HOCgqM4PRn644dOt3EcEvwQ9xAOpMRID+NWmDCX+cg5y0dOdznWvkpbr5EkJVo7tBLQgijfqwaFOG96e+IKShT9PGK93+Jqb5mvvVLjexcadOUEpZ7HqQmEZLwQH2gcEvwQ9wgORZ/YrTEfv87TJ9/mhx51LLa2iFWf2GRoXYL2vjXPaYXD4RAQg5MaaQqRUSRRgaFECZQqonQJ/axFt5zht7/Z59mrZ1kpzpPrJSAaOPGUfLC1+iHBD3EPIBhZYVq9wmPHVvnI6W0eOrLDVLRCpDNvTWsN2oycE4ioBIeiKBJ+9d+8zB/84SvML0zxUz/5BE89sYhyOyhVIjgoFc4mbJdzPH9tiq+/2uS7lxbp8BjOTIehNOv75x9QopuHfvwv/9XxnYc4xB1BQJETyTVm9fN84dHr/MTjVzg3+zatuIMxDmX0kNyiB15yMCjlHWYKQz81/NL/8BW+83zOGxdzvv3cq5x94CRLSy2UduF2Dq0cNdNjeXKH0zMpsxMRF67ukNsWohJQ7gPthDsk+CHuGpQUJG6FE7Uv8ec+l/LMiRWma6tEUYHWGqUNaIPSgXBKUzrN1jZ0+hodxRijUUCiExaXjvHVb75Av2jR6dd44YXXePLpB5iYjFCqRCuNUgqlHEZKmknJ8mzBUmuHq2s9ukUbp2qgNKIU6gNI8kOCvysYDeq4G7ib17pbEGrubR6a/g5/4mMdzs9epGm20JFFGRW0dnCYKd/ftlLn2efW+MVf/iP+53/9HS5e6nL27Gma9QilhKUjM0xOT/Gd775BUcTsdAuuXLnKxz56hkZSokSFmlCgFJqSRPrMtYWFacPmzhYbvTqOFpCM1Nv7sf7uDQ4JfhfhI7BGxGfg3ag8SWNQVYiWRUmJJke5EPghcoOhHz+m/L6BFESywtH6c/yHn1jl/OxVmkkXZcSb5EaF56yeRwOafh7xj//pc/zel66wutnmtdfX2Vzf4KMffYLIWLROOXnyKHnqeOml1xA0Vy6t8tlPn+XIQuQjWZWPihtQV0FkChbbHZZmIi6vOzb7TZyaCO+g2t5H9XcPcUjwuwU1JJ2PqKocO/sR1KEoUZKjydGuR40tWmwwU99hIikoyxwrifcGD0K0qmvJUD73u/y7AKkaMnEYt86J2lf5',NULL,1,'\r\n### Error updating database.  Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column \'cover_image\' at row 1\r\n### The error may exist in file [E:\\JProject\\RuoYi-Vue-master\\ruoyi-system\\target\\classes\\mapper\\system\\CourseMapper.xml]\r\n### The error may involve com.ruoyi.system.mapper.CourseMapper.updateCourse-Inline\r\n### The error occurred while setting parameters\r\n### SQL: update course          SET title = ?,              description = ?,              cover_image = ?,              credit = ?,              course_type = ?,              start_time = ?,              end_time = ?,              status = ?,                           student_count = ?,                           update_time = sysdate()          where id = ? and is_deleted = 0\r\n### Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column \'cover_image\' at row 1\n; Data truncation: Data too long for column \'cover_image\' at row 1; nested exception is com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column \'cover_image\' at row 1','2025-11-20 19:09:05',13),(132,'课程',2,'com.ruoyi.web.controller.system.CourseController.edit()','PUT',1,'tc',NULL,'/course','127.0.0.1','内网IP','{\"courseType\":\"必修课\",\"coverImage\":\"/profile/upload/2025/11/20/3_20251120191302A001.png\",\"createTime\":\"2025-11-20 19:06:08\",\"credit\":3,\"description\":\"123\",\"endTime\":\"2025-11-27 00:00:00\",\"id\":30,\"isDeleted\":0,\"params\":{},\"progress\":0,\"startTime\":\"2025-11-20 19:06:00\",\"status\":\"未开始\",\"studentCount\":0,\"teacherUserId\":37,\"title\":\"测试课程\",\"updateTime\":\"2025-11-20 19:06:08\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 19:13:03',26),(133,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.dataScope()','PUT',1,'tc',NULL,'/system/role/dataScope','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 17:47:44\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"params\":{},\"roleId\":100,\"roleKey\":\"student\",\"roleName\":\"学生角色\",\"roleSort\":3,\"status\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 20:32:26',117),(134,'角色管理',2,'com.ruoyi.web.controller.system.SysRoleController.edit()','PUT',1,'tc',NULL,'/system/role','127.0.0.1','内网IP','{\"admin\":false,\"createTime\":\"2025-11-18 17:47:44\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2000,1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117,4],\"params\":{},\"roleId\":100,\"roleKey\":\"student\",\"roleName\":\"学生角色\",\"roleSort\":3,\"status\":\"0\",\"updateBy\":\"tc\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 20:43:26',157),(135,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"course/detail\",\"createTime\":\"2025-11-20 20:22:47\",\"icon\":\"\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":5009,\"menuName\":\"课程详情\",\"menuType\":\"C\",\"orderNum\":2,\"params\":{},\"parentId\":2000,\"path\":\"detail/:id\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"tc\",\"visible\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 20:53:13',125),(136,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"course/detail\",\"createTime\":\"2025-11-20 20:22:47\",\"icon\":\"\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":5009,\"menuName\":\"课程详情\",\"menuType\":\"C\",\"orderNum\":2,\"params\":{},\"parentId\":0,\"path\":\"detail/:id\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"tc\",\"visible\":\"0\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 20:53:37',24),(137,'菜单管理',2,'com.ruoyi.web.controller.system.SysMenuController.edit()','PUT',1,'tc',NULL,'/system/menu','127.0.0.1','内网IP','{\"children\":[],\"component\":\"course/detail\",\"createTime\":\"2025-11-20 20:22:47\",\"icon\":\"\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":5009,\"menuName\":\"课程详情\",\"menuType\":\"C\",\"orderNum\":2,\"params\":{},\"parentId\":0,\"path\":\"detail/:id\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"tc\",\"visible\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:30:22',87),(138,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"1\",\"id\":28,\"params\":{},\"sortOrder\":0,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:49:05',232),(139,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":28,\"duration\":60,\"id\":73,\"params\":{},\"sortOrder\":0,\"title\":\"1\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:49:14',37),(140,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":28,\"duration\":0,\"id\":74,\"params\":{},\"sortOrder\":1,\"title\":\"2\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:50:51',101),(141,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":28,\"duration\":180,\"id\":75,\"params\":{},\"sortOrder\":2,\"title\":\"3\",\"type\":\"document\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:51:01',8),(142,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"2\",\"id\":29,\"params\":{},\"sortOrder\":1,\"title\":\"2\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:52:23',39),(143,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":29,\"duration\":738720,\"id\":76,\"params\":{},\"sortOrder\":0,\"title\":\"231\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:52:34',39),(144,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":29,\"duration\":0,\"id\":77,\"params\":{},\"sortOrder\":1,\"title\":\"qwe\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:52:39',37),(145,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":29,\"duration\":180,\"id\":78,\"params\":{},\"sortOrder\":2,\"title\":\"3\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:53:13',85),(146,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":28,\"duration\":120,\"id\":79,\"params\":{},\"sortOrder\":3,\"title\":\"我\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:53:21',36),(147,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":29,\"duration\":1380,\"id\":80,\"params\":{},\"sortOrder\":3,\"title\":\"2\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:53:26',21),(148,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"123\",\"id\":30,\"params\":{},\"sortOrder\":2,\"title\":\"321\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:53:52',120),(149,'课程小节',3,'com.ruoyi.web.controller.system.SectionController.remove()','DELETE',1,'tc',NULL,'/section/73','127.0.0.1','内网IP','[73]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:57:17',31),(150,'课程小节',3,'com.ruoyi.web.controller.system.SectionController.remove()','DELETE',1,'tc',NULL,'/section/74','127.0.0.1','内网IP','[74]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:57:20',23),(151,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":28,\"duration\":60,\"id\":81,\"params\":{},\"sortOrder\":2,\"title\":\"测试\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:57:23',9),(152,'课程小节',3,'com.ruoyi.web.controller.system.SectionController.remove()','DELETE',1,'tc',NULL,'/section/75','127.0.0.1','内网IP','[75]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:57:27',12),(153,'课程小节',3,'com.ruoyi.web.controller.system.SectionController.remove()','DELETE',1,'tc',NULL,'/section/81','127.0.0.1','内网IP','[81]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:57:29',11),(154,'课程章节',3,'com.ruoyi.web.controller.system.ChapterController.remove()','DELETE',1,'tc',NULL,'/chapter/28','127.0.0.1','内网IP','[28]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:57:34',15),(155,'课程章节',3,'com.ruoyi.web.controller.system.ChapterController.remove()','DELETE',1,'tc',NULL,'/chapter/29','127.0.0.1','内网IP','[29]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:57:45',11),(156,'课程章节',3,'com.ruoyi.web.controller.system.ChapterController.remove()','DELETE',1,'tc',NULL,'/chapter/30','127.0.0.1','内网IP','[30]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:57:48',13),(157,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"21\",\"id\":31,\"params\":{},\"sortOrder\":0,\"title\":\"12\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:58:17',94),(158,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":31,\"duration\":720,\"id\":82,\"params\":{},\"sortOrder\":0,\"title\":\"12\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:58:23',31),(159,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":31,\"duration\":720,\"id\":83,\"params\":{},\"sortOrder\":1,\"title\":\"12\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:58:27',22),(160,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":31,\"duration\":720,\"id\":84,\"params\":{},\"sortOrder\":2,\"title\":\"12\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:58:39',23),(161,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"12\",\"id\":32,\"params\":{},\"sortOrder\":1,\"title\":\"21\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:58:45',9),(162,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":32,\"duration\":1260,\"id\":85,\"params\":{},\"sortOrder\":0,\"title\":\"21\",\"type\":\"video\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:58:51',30),(163,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"12\",\"id\":33,\"params\":{},\"sortOrder\":2,\"title\":\"12\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 21:58:54',24),(164,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"312\",\"id\":34,\"params\":{},\"sortOrder\":3,\"title\":\"123\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"description\":\"312\",\"id\":34,\"params\":{},\"sortOrder\":3,\"title\":\"123\"}}',0,NULL,'2025-11-20 22:02:18',263),(165,'课程章节',3,'com.ruoyi.web.controller.system.ChapterController.remove()','DELETE',1,'tc',NULL,'/chapter/34','127.0.0.1','内网IP','[34]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 22:02:22',23),(166,'课程章节',3,'com.ruoyi.web.controller.system.ChapterController.remove()','DELETE',1,'tc',NULL,'/chapter/33','127.0.0.1','内网IP','[33]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 22:02:24',20),(167,'课程小节',2,'com.ruoyi.web.controller.system.SectionController.edit()','PUT',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":31,\"duration\":0,\"id\":83,\"params\":{},\"sortOrder\":1,\"title\":\"12\",\"type\":\"document\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 22:04:22',129),(168,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":31,\"duration\":12,\"id\":86,\"params\":{},\"sortOrder\":3,\"title\":\"2\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":31,\"duration\":12,\"id\":86,\"params\":{},\"sortOrder\":3,\"title\":\"2\"}}',0,NULL,'2025-11-20 22:21:11',119),(169,'课程小节',2,'com.ruoyi.web.controller.system.SectionController.edit()','PUT',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":31,\"duration\":0,\"id\":82,\"params\":{},\"sortOrder\":0,\"title\":\"12\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 22:34:31',80),(170,'课程小节',2,'com.ruoyi.web.controller.system.SectionController.edit()','PUT',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":31,\"duration\":0,\"id\":83,\"params\":{},\"sortOrder\":1,\"title\":\"12\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 22:34:47',51),(171,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"1\",\"id\":35,\"params\":{},\"sortOrder\":0,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"description\":\"1\",\"id\":35,\"params\":{},\"sortOrder\":0,\"title\":\"1\"}}',0,NULL,'2025-11-20 22:37:11',49),(172,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"2\",\"id\":36,\"params\":{},\"sortOrder\":1,\"title\":\"2\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"description\":\"2\",\"id\":36,\"params\":{},\"sortOrder\":1,\"title\":\"2\"}}',0,NULL,'2025-11-20 22:37:14',11),(173,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":35,\"duration\":0,\"id\":87,\"params\":{},\"sortOrder\":0,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":35,\"duration\":0,\"id\":87,\"params\":{},\"sortOrder\":0,\"title\":\"1\"}}',0,NULL,'2025-11-20 22:37:23',14),(174,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":35,\"duration\":0,\"id\":88,\"params\":{},\"sortOrder\":1,\"title\":\"2\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":35,\"duration\":0,\"id\":88,\"params\":{},\"sortOrder\":1,\"title\":\"2\"}}',0,NULL,'2025-11-20 22:37:32',9),(175,'课程小节',2,'com.ruoyi.web.controller.system.SectionController.edit()','PUT',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":35,\"duration\":0,\"id\":87,\"params\":{},\"sortOrder\":0,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 22:46:54',51),(176,'课程小节',2,'com.ruoyi.web.controller.system.SectionController.edit()','PUT',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":35,\"duration\":0,\"id\":87,\"params\":{},\"sortOrder\":0,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 23:16:28',29),(177,'课程小节',2,'com.ruoyi.web.controller.system.SectionController.edit()','PUT',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":35,\"description\":\"1\",\"id\":87,\"params\":{},\"sortOrder\":0,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-20 23:18:05',22),(178,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":35,\"description\":\"2\",\"id\":89,\"params\":{},\"sortOrder\":2,\"title\":\"3\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":35,\"description\":\"2\",\"id\":89,\"params\":{},\"sortOrder\":2,\"title\":\"3\"}}',0,NULL,'2025-11-21 13:55:33',375),(179,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":35,\"description\":\"4\",\"id\":90,\"params\":{},\"sortOrder\":3,\"title\":\"4\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":35,\"description\":\"4\",\"id\":90,\"params\":{},\"sortOrder\":3,\"title\":\"4\"}}',0,NULL,'2025-11-21 13:55:52',27),(180,'课程小节',3,'com.ruoyi.web.controller.system.SectionController.remove()','DELETE',1,'tc',NULL,'/section/90','127.0.0.1','内网IP','[90]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 13:55:58',20),(181,'课程小节',3,'com.ruoyi.web.controller.system.SectionController.remove()','DELETE',1,'tc',NULL,'/section/89','127.0.0.1','内网IP','[89]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 13:56:02',18),(182,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"1\",\"id\":37,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"description\":\"1\",\"id\":37,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}}',0,NULL,'2025-11-21 13:58:35',124),(183,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":37,\"description\":\"1\",\"id\":91,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":37,\"description\":\"1\",\"id\":91,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}}',0,NULL,'2025-11-21 13:58:45',55),(184,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":37,\"description\":\"2\",\"id\":92,\"params\":{},\"sortOrder\":2,\"title\":\"2\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":37,\"description\":\"2\",\"id\":92,\"params\":{},\"sortOrder\":2,\"title\":\"2\"}}',0,NULL,'2025-11-21 13:58:55',87),(185,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":37,\"description\":\"3\",\"id\":93,\"params\":{},\"sortOrder\":3,\"title\":\"3\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":37,\"description\":\"3\",\"id\":93,\"params\":{},\"sortOrder\":3,\"title\":\"3\"}}',0,NULL,'2025-11-21 13:59:07',54),(186,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"2\",\"id\":38,\"params\":{},\"sortOrder\":2,\"title\":\"2\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"description\":\"2\",\"id\":38,\"params\":{},\"sortOrder\":2,\"title\":\"2\"}}',0,NULL,'2025-11-21 13:59:15',40),(187,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":37,\"description\":\"4\",\"id\":94,\"params\":{},\"sortOrder\":4,\"title\":\"4\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":37,\"description\":\"4\",\"id\":94,\"params\":{},\"sortOrder\":4,\"title\":\"4\"}}',0,NULL,'2025-11-21 14:00:43',100),(188,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":37,\"description\":\"\",\"id\":95,\"params\":{},\"sortOrder\":5,\"title\":\"5\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":37,\"id\":95,\"params\":{},\"sortOrder\":5,\"title\":\"5\"}}',0,NULL,'2025-11-21 14:01:02',41),(189,'课程小节',1,'com.ruoyi.web.controller.system.SectionController.add()','POST',1,'tc',NULL,'/section','127.0.0.1','内网IP','{\"chapterId\":37,\"description\":\"\",\"id\":96,\"params\":{},\"sortOrder\":6,\"title\":\"6\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"chapterId\":37,\"id\":96,\"params\":{},\"sortOrder\":6,\"title\":\"6\"}}',0,NULL,'2025-11-21 14:01:09',51),(190,'课程章节',3,'com.ruoyi.web.controller.system.ChapterController.remove()','DELETE',1,'tc',NULL,'/chapter/38','127.0.0.1','内网IP','[38]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:39:15',20),(191,'课程章节',3,'com.ruoyi.web.controller.system.ChapterController.remove()','DELETE',1,'tc',NULL,'/chapter/37','127.0.0.1','内网IP','[37]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:39:15',11),(192,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"\",\"id\":39,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"id\":39,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}}',0,NULL,'2025-11-21 14:39:23',19),(193,'课程章节',3,'com.ruoyi.web.controller.system.ChapterController.remove()','DELETE',1,'tc',NULL,'/chapter/39','127.0.0.1','内网IP','[39]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:39:30',55),(194,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"1\",\"id\":40,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"description\":\"1\",\"id\":40,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}}',0,NULL,'2025-11-21 14:39:43',57),(195,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"\",\"id\":41,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"id\":41,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}}',0,NULL,'2025-11-21 14:43:29',96),(196,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"\",\"id\":42,\"params\":{},\"sortOrder\":1,\"title\":\"2\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"id\":42,\"params\":{},\"sortOrder\":1,\"title\":\"2\"}}',0,NULL,'2025-11-21 14:43:44',42),(197,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"\",\"id\":43,\"params\":{},\"sortOrder\":1,\"title\":\"2\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"id\":43,\"params\":{},\"sortOrder\":1,\"title\":\"2\"}}',0,NULL,'2025-11-21 14:48:00',14),(198,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:00\",\"description\":\"\",\"id\":43,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":2,\"title\":\"2\",\"updateTime\":\"2025-11-21 14:48:00\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:48:22',86),(199,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"\",\"id\":44,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"id\":44,\"params\":{},\"sortOrder\":1,\"title\":\"1\"}}',0,NULL,'2025-11-21 14:48:22',10),(200,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:00\",\"description\":\"\",\"id\":43,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":1,\"title\":\"2\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:16',16),(201,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:22\",\"description\":\"\",\"id\":44,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":2,\"title\":\"1\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:16',13),(202,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:22\",\"description\":\"\",\"id\":44,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":1,\"title\":\"1\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:18',17),(203,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:00\",\"description\":\"\",\"id\":43,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":2,\"title\":\"2\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:18',21),(204,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:22\",\"description\":\"\",\"id\":44,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":2,\"title\":\"1\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:21',15),(205,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:00\",\"description\":\"\",\"id\":43,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":1,\"title\":\"2\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:21',13),(206,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:22\",\"description\":\"\",\"id\":44,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":1,\"title\":\"1\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:21',7),(207,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:00\",\"description\":\"\",\"id\":43,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":2,\"title\":\"2\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:22',20),(208,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:00\",\"description\":\"\",\"id\":43,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":1,\"title\":\"2\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:24',7),(209,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:22\",\"description\":\"\",\"id\":44,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":2,\"title\":\"1\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:24',6),(210,'课程章节',2,'com.ruoyi.web.controller.system.ChapterController.edit()','PUT',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"createTime\":\"2025-11-21 14:48:22\",\"description\":\"\",\"id\":44,\"isDeleted\":0,\"params\":{},\"sections\":[],\"sortOrder\":3,\"title\":\"1\",\"updateTime\":\"2025-11-21 14:48:22\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 14:50:33',25),(211,'课程章节',1,'com.ruoyi.web.controller.system.ChapterController.add()','POST',1,'tc',NULL,'/chapter','127.0.0.1','内网IP','{\"courseId\":30,\"description\":\"\",\"id\":45,\"params\":{},\"sortOrder\":2,\"title\":\"3\"}','{\"msg\":\"操作成功\",\"code\":200,\"data\":{\"courseId\":30,\"id\":45,\"params\":{},\"sortOrder\":2,\"title\":\"3\"}}',0,NULL,'2025-11-21 14:50:33',11),(212,'课程章节',3,'com.ruoyi.web.controller.system.ChapterController.remove()','DELETE',1,'tc',NULL,'/chapter/43','127.0.0.1','内网IP','[43]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 15:10:50',69),(213,'课程章节',3,'com.ruoyi.web.controller.system.ChapterController.remove()','DELETE',1,'tc',NULL,'/chapter/45','127.0.0.1','内网IP','[45]','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 15:10:50',7),(214,'课程',2,'com.ruoyi.web.controller.system.CourseController.edit()','PUT',1,'tc',NULL,'/course','127.0.0.1','内网IP','{\"courseType\":\"必修课\",\"coverImage\":\"/profile/upload/2025/11/21/j2cjlogo_20251121152823A001.png\",\"createTime\":\"2025-11-20 19:06:08\",\"credit\":3,\"description\":\"123\",\"endTime\":\"2025-11-27 00:00:00\",\"id\":30,\"isDeleted\":0,\"params\":{},\"progress\":13,\"startTime\":\"2025-11-20 19:06:00\",\"status\":\"未开始\",\"studentCount\":0,\"teacherName\":\"tc\",\"teacherUserId\":37,\"title\":\"测试课程\",\"updateTime\":\"2025-11-20 19:13:03\"}','{\"msg\":\"操作成功\",\"code\":200}',0,NULL,'2025-11-21 15:28:25',127);

/*Table structure for table `sys_post` */

DROP TABLE IF EXISTS `sys_post`;

CREATE TABLE `sys_post` (
  `post_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int(11) NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='岗位信息表';

/*Data for the table `sys_post` */

insert  into `sys_post`(`post_id`,`post_code`,`post_name`,`post_sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values (1,'ceo','董事长',1,'0','admin','2025-11-18 16:54:30','',NULL,''),(2,'se','项目经理',2,'0','admin','2025-11-18 16:54:30','',NULL,''),(3,'hr','人力资源',3,'0','admin','2025-11-18 16:54:30','',NULL,''),(4,'user','普通员工',4,'0','admin','2025-11-18 16:54:30','',NULL,'');

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(11) NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='角色信息表';

/*Data for the table `sys_role` */

insert  into `sys_role`(`role_id`,`role_name`,`role_key`,`role_sort`,`data_scope`,`menu_check_strictly`,`dept_check_strictly`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values (1,'超级管理员','admin',1,'1',1,1,'0','0','admin','2025-11-18 16:54:30','',NULL,'超级管理员'),(2,'普通角色','common',2,'2',1,1,'0','0','admin','2025-11-18 16:54:30','admin','2025-11-18 18:27:30','普通角色'),(100,'学生角色','student',3,'1',1,1,'0','0','','2025-11-18 17:47:44','tc','2025-11-20 20:43:26',NULL),(101,'教师角色','teacher',9,'1',1,1,'0','0','','2025-11-18 17:47:44','admin','2025-11-18 17:54:24',NULL);

/*Table structure for table `sys_role_dept` */

DROP TABLE IF EXISTS `sys_role_dept`;

CREATE TABLE `sys_role_dept` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='角色和部门关联表';

/*Data for the table `sys_role_dept` */

insert  into `sys_role_dept`(`role_id`,`dept_id`) values (2,100),(2,101),(2,105);

/*Table structure for table `sys_role_menu` */

DROP TABLE IF EXISTS `sys_role_menu`;

CREATE TABLE `sys_role_menu` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='角色和菜单关联表';

/*Data for the table `sys_role_menu` */

insert  into `sys_role_menu`(`role_id`,`menu_id`) values (1,2000),(1,2001),(1,5000),(1,5001),(1,5002),(1,5003),(1,5004),(1,5005),(1,5006),(1,5007),(1,5009),(2,1),(2,2),(2,3),(2,4),(2,100),(2,101),(2,102),(2,103),(2,104),(2,105),(2,106),(2,107),(2,108),(2,109),(2,110),(2,111),(2,112),(2,113),(2,114),(2,115),(2,116),(2,117),(2,500),(2,501),(2,1000),(2,1001),(2,1002),(2,1003),(2,1004),(2,1005),(2,1006),(2,1007),(2,1008),(2,1009),(2,1010),(2,1011),(2,1012),(2,1013),(2,1014),(2,1015),(2,1016),(2,1017),(2,1018),(2,1019),(2,1020),(2,1021),(2,1022),(2,1023),(2,1024),(2,1025),(2,1026),(2,1027),(2,1028),(2,1029),(2,1030),(2,1031),(2,1032),(2,1033),(2,1034),(2,1035),(2,1036),(2,1037),(2,1038),(2,1039),(2,1040),(2,1041),(2,1042),(2,1043),(2,1044),(2,1045),(2,1046),(2,1047),(2,1048),(2,1049),(2,1050),(2,1051),(2,1052),(2,1053),(2,1054),(2,1055),(2,1056),(2,1057),(2,1058),(2,1059),(2,1060),(2,2000),(2,2001),(2,5009),(100,1),(100,2),(100,3),(100,4),(100,100),(100,101),(100,102),(100,103),(100,104),(100,105),(100,106),(100,107),(100,108),(100,109),(100,110),(100,111),(100,112),(100,113),(100,114),(100,115),(100,116),(100,117),(100,500),(100,501),(100,1000),(100,1001),(100,1002),(100,1003),(100,1004),(100,1005),(100,1006),(100,1007),(100,1008),(100,1009),(100,1010),(100,1011),(100,1012),(100,1013),(100,1014),(100,1015),(100,1016),(100,1017),(100,1018),(100,1019),(100,1020),(100,1021),(100,1022),(100,1023),(100,1024),(100,1025),(100,1026),(100,1027),(100,1028),(100,1029),(100,1030),(100,1031),(100,1032),(100,1033),(100,1034),(100,1035),(100,1036),(100,1037),(100,1038),(100,1039),(100,1040),(100,1041),(100,1042),(100,1043),(100,1044),(100,1045),(100,1046),(100,1047),(100,1048),(100,1049),(100,1050),(100,1051),(100,1052),(100,1053),(100,1054),(100,1055),(100,1056),(100,1057),(100,1058),(100,1059),(100,1060),(100,2000),(100,5009),(101,1),(101,2),(101,3),(101,4),(101,100),(101,101),(101,102),(101,103),(101,104),(101,105),(101,106),(101,107),(101,108),(101,109),(101,110),(101,111),(101,112),(101,113),(101,114),(101,115),(101,116),(101,117),(101,500),(101,501),(101,1000),(101,1001),(101,1002),(101,1003),(101,1004),(101,1005),(101,1006),(101,1007),(101,1008),(101,1009),(101,1010),(101,1011),(101,1012),(101,1013),(101,1014),(101,1015),(101,1016),(101,1017),(101,1018),(101,1019),(101,1020),(101,1021),(101,1022),(101,1023),(101,1024),(101,1025),(101,1026),(101,1027),(101,1028),(101,1029),(101,1030),(101,1031),(101,1032),(101,1033),(101,1034),(101,1035),(101,1036),(101,1037),(101,1038),(101,1039),(101,1040),(101,1041),(101,1042),(101,1043),(101,1044),(101,1045),(101,1046),(101,1047),(101,1048),(101,1049),(101,1050),(101,1051),(101,1052),(101,1053),(101,1054),(101,1055),(101,1056),(101,1057),(101,1058),(101,1059),(101,1060),(101,2000),(101,2001),(101,5009);

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=220 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='用户信息表';

/*Data for the table `sys_user` */

insert  into `sys_user`(`user_id`,`dept_id`,`user_name`,`nick_name`,`user_type`,`email`,`phonenumber`,`sex`,`avatar`,`password`,`status`,`del_flag`,`login_ip`,`login_date`,`pwd_update_date`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values (1,103,'admin','若依','00','ry@163.com','15888888888','1','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','127.0.0.1','2025-11-18 20:55:27','2025-11-18 16:54:30','admin','2025-11-18 16:54:30','','2025-11-18 20:55:27','管理员'),(2,105,'ry','若依','00','ry@qq.com','15666666666','1','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','0','0','127.0.0.1','2025-11-18 16:54:30','2025-11-18 16:54:30','admin','2025-11-18 16:54:30','',NULL,'测试员'),(201,103,'wangwu','王五','00','wangwu@ry.com','15888888888','1','','$2a$10$3FOi3w6sPI7DrG9/XG3YvOzfzItqfGpZ1/2QAjVFlY534mOzb/VR6','0','0','127.0.0.1','2025-11-19 16:39:02','2025-11-18 17:21:36','admin','2025-11-18 17:19:58','','2025-11-19 16:39:01','测试学生用户'),(204,NULL,'test_student_001','测试学生001','00','student001@test.com','13800000001','0','','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/sttKGf6ItRm','0','0','',NULL,NULL,'','2025-11-18 17:49:25','',NULL,NULL),(206,NULL,'test-student','测试学生','00','','','0','','$2a$10$FvcQHDYly7TI0.dG9bmVueQcXttEc1IhMQZW1F5RMg/t2KYOtjHMC','0','0','127.0.0.1','2025-11-18 17:54:50',NULL,'admin','2025-11-18 17:52:39','','2025-11-18 17:54:50',NULL),(209,NULL,'1234','lll','00','qwkhrkhe@qq.com','19823452345','0','','$2a$10$f6hZLNe7jsmX5XSKSB2GreY7ztliJfdrXHfmNaUu8FVkBjXE4AWaS','0','0','127.0.0.1','2025-11-18 18:50:33','2025-11-18 18:48:02','','2025-11-18 18:48:02','','2025-11-18 18:50:33',NULL),(210,NULL,'ljy','ljy','00','','','0','','$2a$10$OAkliFSH3o5ScnsZLwMMluPYQLx36rlzFOu116OAoHAN04Yyi9Za6','0','0','127.0.0.1','2025-11-18 18:55:34','2025-11-18 18:55:17','','2025-11-18 18:55:17','','2025-11-18 18:55:34',NULL),(211,NULL,'123','123','00','','','0','','$2a$10$k89FTghIgYMXQe2mjtV9/eUBJ9etMhZRc2iznjKrXfzlfqNY1BiG6','0','0','',NULL,'2025-11-18 21:05:42','','2025-11-18 21:05:42','',NULL,NULL),(215,NULL,'wma','wma','00','','','0','','$2a$10$A6OJkbRsVDScEkEYCLjx/Otj6aYTxS2N4bcCJfJqSaFv7dktY931.','0','0','',NULL,'2025-11-18 21:10:26','','2025-11-18 21:10:25','',NULL,NULL),(216,NULL,'qwhrei','qwhrei','00','','','0','','$2a$10$FYz5jHUXHmepAMHcrc1Un.H1MXz9LeryaECc5qoTDSJmiTBV1usDK','0','0','127.0.0.1','2025-11-18 22:54:16','2025-11-18 21:17:37','','2025-11-18 21:17:37','','2025-11-18 22:54:16',NULL),(217,NULL,'we','we','00','','','0','','$2a$10$jILpJWswxfthwF8AjwPw6ufZTVW/eH4/dx393PrejDFUUrZd8X.oe','0','0','127.0.0.1','2025-11-19 16:39:30','2025-11-18 22:55:20','','2025-11-18 22:55:20','','2025-11-19 16:39:30',NULL),(218,NULL,'123456','123456','00','2138642@qq.con','15425356782','1','','$2a$10$ADUK1SwsO3YovM5yjHCFbOy4PdJ4LhTuNQU.doqVOg03i0OnU18bq','0','0','127.0.0.1','2025-11-19 16:40:47','2025-11-19 16:40:37','','2025-11-19 16:40:36','','2025-11-19 16:42:18',NULL),(219,NULL,'tc','tc','00','','','0','','$2a$10$.K54jcsSs5L6m9uGX0uIse5xXxyZcbubn.3243p4DKKqEq1Yy41Vy','0','0','127.0.0.1','2025-11-21 14:35:23','2025-11-19 17:09:36','','2025-11-19 17:09:36','','2025-11-21 14:35:22',NULL);

/*Table structure for table `sys_user_post` */

DROP TABLE IF EXISTS `sys_user_post`;

CREATE TABLE `sys_user_post` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `post_id` bigint(20) NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='用户与岗位关联表';

/*Data for the table `sys_user_post` */

insert  into `sys_user_post`(`user_id`,`post_id`) values (1,1),(2,2);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='用户和角色关联表';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`user_id`,`role_id`) values (1,1),(2,2),(216,100),(217,100),(218,100),(219,100);

/*Table structure for table `teacher` */

DROP TABLE IF EXISTS `teacher`;

CREATE TABLE `teacher` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '教师ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
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

/*Data for the table `teacher` */

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `sys_user_id` bigint(20) DEFAULT NULL COMMENT '关联sys_user表的user_id',
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
  UNIQUE KEY `uk_email` (`email`) USING BTREE,
  UNIQUE KEY `uk_sys_user_id` (`sys_user_id`) USING BTREE,
  CONSTRAINT `fk_user_sys_user` FOREIGN KEY (`sys_user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';

/*Data for the table `user` */

insert  into `user`(`id`,`sys_user_id`,`username`,`password`,`email`,`real_name`,`avatar`,`role`,`status`,`create_time`,`update_time`) values (19,201,'wangwu','$2a$10$3FOi3w6sPI7DrG9/XG3YvOzfzItqfGpZ1/2QAjVFlY534mOzb/VR6','wangwu@ry.com','王五','','TEACHER','ACTIVE','2025-11-18 17:19:58','2025-11-19 16:39:01'),(22,204,'test_student_001','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/sttKGf6ItRm','student001@test.com','测试学生001','','STUDENT','ACTIVE','2025-11-18 17:49:25',NULL),(24,206,'test-student','$2a$10$FvcQHDYly7TI0.dG9bmVueQcXttEc1IhMQZW1F5RMg/t2KYOtjHMC',NULL,'测试学生','','STUDENT','ACTIVE','2025-11-18 17:52:39','2025-11-18 18:45:35'),(27,209,'1234','$2a$10$f6hZLNe7jsmX5XSKSB2GreY7ztliJfdrXHfmNaUu8FVkBjXE4AWaS','qwkhrkhe@qq.com','lll','','STUDENT','ACTIVE','2025-11-18 18:48:02','2025-11-18 18:50:33'),(28,210,'ljy','$2a$10$OAkliFSH3o5ScnsZLwMMluPYQLx36rlzFOu116OAoHAN04Yyi9Za6',NULL,'ljy','','STUDENT','ACTIVE','2025-11-18 18:55:17','2025-11-18 18:55:34'),(29,211,'123','$2a$10$k89FTghIgYMXQe2mjtV9/eUBJ9etMhZRc2iznjKrXfzlfqNY1BiG6',NULL,'123','','STUDENT','ACTIVE','2025-11-18 21:05:42',NULL),(33,215,'wma','$2a$10$A6OJkbRsVDScEkEYCLjx/Otj6aYTxS2N4bcCJfJqSaFv7dktY931.',NULL,'wma','','STUDENT','ACTIVE','2025-11-18 21:10:25',NULL),(34,216,'qwhrei','$2a$10$FYz5jHUXHmepAMHcrc1Un.H1MXz9LeryaECc5qoTDSJmiTBV1usDK',NULL,'qwhrei','','STUDENT','ACTIVE','2025-11-18 21:17:37','2025-11-18 22:54:16'),(35,217,'we','$2a$10$jILpJWswxfthwF8AjwPw6ufZTVW/eH4/dx393PrejDFUUrZd8X.oe',NULL,'we','','STUDENT','ACTIVE','2025-11-18 22:55:20','2025-11-19 16:39:30'),(36,218,'123456','$2a$10$ADUK1SwsO3YovM5yjHCFbOy4PdJ4LhTuNQU.doqVOg03i0OnU18bq','2138642@qq.con','123456','','STUDENT','ACTIVE','2025-11-19 16:40:36','2025-11-19 16:42:18'),(37,219,'tc','$2a$10$.K54jcsSs5L6m9uGX0uIse5xXxyZcbubn.3243p4DKKqEq1Yy41Vy',NULL,'tc','','STUDENT','ACTIVE','2025-11-19 17:09:36','2025-11-21 14:35:22');

/*Table structure for table `video` */

DROP TABLE IF EXISTS `video`;

CREATE TABLE `video` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '视频ID',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '视频描述',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频文件路径',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小（字节）',
  `duration` int(11) DEFAULT NULL COMMENT '时长（秒）',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '封面图片路径',
  `resolution` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分辨率（如：1080p）',
  `knowledge_point_ids` json DEFAULT NULL COMMENT '关联的知识点ID列表',
  `status` enum('uploading','processing','published','offline') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'uploading' COMMENT '状态',
  `view_count` int(11) DEFAULT '0' COMMENT '观看次数',
  `uploaded_by` bigint(20) DEFAULT NULL COMMENT '上传者ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_course_id` (`course_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_uploaded_by` (`uploaded_by`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='视频表';

/*Data for the table `video` */

/*Table structure for table `video_learning_behavior` */

DROP TABLE IF EXISTS `video_learning_behavior`;

CREATE TABLE `video_learning_behavior` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '行为ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `video_id` bigint(20) NOT NULL COMMENT '视频ID',
  `watch_duration` int(11) DEFAULT '0' COMMENT '观看时长（秒）',
  `video_duration` int(11) DEFAULT NULL COMMENT '视频总时长（秒）',
  `completion_rate` decimal(5,2) DEFAULT NULL COMMENT '完成率（%）',
  `watch_count` int(11) DEFAULT '1' COMMENT '观看次数',
  `heatmap_data` json DEFAULT NULL COMMENT '热力图数据',
  `is_completed` tinyint(4) DEFAULT '0' COMMENT '是否看完：1-是 0-否',
  `fast_forward_count` int(11) DEFAULT '0' COMMENT '快进次数',
  `pause_count` int(11) DEFAULT '0' COMMENT '暂停次数',
  `playback_speed` decimal(2,1) DEFAULT '1.0' COMMENT '播放倍速',
  `first_watch_at` timestamp NULL DEFAULT NULL COMMENT '首次观看时间',
  `last_watch_at` timestamp NULL DEFAULT NULL COMMENT '最近观看时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_student_video` (`student_id`,`video_id`) USING BTREE,
  KEY `idx_student_id` (`student_id`) USING BTREE,
  KEY `idx_video_id` (`video_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='视频学习行为表';

/*Data for the table `video_learning_behavior` */

/* Trigger structure for table `sys_user` */

DELIMITER $$

/*!50003 DROP TRIGGER*//*!50032 IF EXISTS */ /*!50003 `trg_sys_user_insert` */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'root'@'localhost' */ /*!50003 TRIGGER `trg_sys_user_insert` AFTER INSERT ON `sys_user` FOR EACH ROW BEGIN
    INSERT INTO `user` (
        `sys_user_id`,
        `username`,
        `password`,
        `email`,
        `real_name`,
        `avatar`,
        `role`,
        `status`,
        `create_time`,
        `update_time`
    ) VALUES (
        NEW.user_id,
        NEW.user_name,
        NEW.password,
        NULLIF(NEW.email, ''),
        NEW.nick_name,
        NEW.avatar,
        CASE
            WHEN NEW.user_type = 'STUDENT' THEN 'STUDENT'
            WHEN NEW.user_type = 'TEACHER' THEN 'TEACHER'
            ELSE 'STUDENT'
        END,
        CASE
            WHEN NEW.status = '0' THEN 'ACTIVE'
            WHEN NEW.status = '1' THEN 'INACTIVE'
            ELSE 'ACTIVE'
        END,
        NEW.create_time,
        NEW.update_time
    );
END */$$


DELIMITER ;

/* Trigger structure for table `sys_user` */

DELIMITER $$

/*!50003 DROP TRIGGER*//*!50032 IF EXISTS */ /*!50003 `trg_sys_user_delete` */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'root'@'localhost' */ /*!50003 TRIGGER `trg_sys_user_delete` AFTER UPDATE ON `sys_user` FOR EACH ROW BEGIN
    -- 若依使用软删除（del_flag = '2'）
    IF NEW.del_flag = '2' THEN
        UPDATE `user` SET
            `status` = 'DELETED',
            `update_time` = NOW()
        WHERE `sys_user_id` = NEW.user_id;
    END IF;
END */$$


DELIMITER ;

/* Trigger structure for table `sys_user` */

DELIMITER $$

/*!50003 DROP TRIGGER*//*!50032 IF EXISTS */ /*!50003 `trg_sys_user_update` */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'root'@'localhost' */ /*!50003 TRIGGER `trg_sys_user_update` AFTER UPDATE ON `sys_user` FOR EACH ROW BEGIN
    UPDATE `user` SET
        `username` = NEW.user_name,
        `password` = NEW.password,
        `email` = NULLIF(NEW.email, ''),
        `real_name` = NEW.nick_name,
        `avatar` = NEW.avatar,
        `status` = CASE
            WHEN NEW.status = '0' THEN 'ACTIVE'
            WHEN NEW.status = '1' THEN 'INACTIVE'
            ELSE 'ACTIVE'
        END,
        `update_time` = NEW.update_time
    WHERE `sys_user_id` = NEW.user_id;
END */$$


DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
