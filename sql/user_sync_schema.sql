-- ================================================================================
-- 用户表同步方案 - 数据库表结构修改
-- ================================================================================

-- 方案说明：
-- 1. sys_user 作为主表（若依框架的用户表）
-- 2. user 表添加 sys_user_id 字段作为外键，关联到 sys_user.user_id
-- 3. 通过数据库触发器实现双向同步

-- ================================================================================
-- 第一步：修改 user 表，添加外键关联
-- ================================================================================

-- 添加 sys_user_id 字段
ALTER TABLE `user` 
ADD COLUMN `sys_user_id` bigint(20) DEFAULT NULL COMMENT '关联sys_user表的user_id' AFTER `id`,
ADD UNIQUE KEY `uk_sys_user_id` (`sys_user_id`);

-- 添加外键约束（可选，如果需要强制引用完整性）
ALTER TABLE `user` 
ADD CONSTRAINT `fk_user_sys_user` 
FOREIGN KEY (`sys_user_id`) REFERENCES `sys_user` (`user_id`) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- ================================================================================
-- 第二步：创建触发器 - sys_user 插入时同步到 user
-- ================================================================================

DROP TRIGGER IF EXISTS `trg_sys_user_insert`;

DELIMITER $$
CREATE TRIGGER `trg_sys_user_insert`
AFTER INSERT ON `sys_user`
FOR EACH ROW
BEGIN
    DECLARE user_role VARCHAR(20);

    -- 查询用户的角色，根据 sys_user_role 和 sys_role 表
    -- 优先级: ADMIN > TEACHER > STUDENT
    SELECT
        CASE
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = NEW.user_id
                AND (r.role_id = 1 OR r.role_key = 'admin' OR r.role_name = '管理员')
            ) THEN 'ADMIN'
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = NEW.user_id
                AND (r.role_key = 'teacher' OR r.role_name LIKE '%教师%')
            ) THEN 'TEACHER'
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = NEW.user_id
                AND (r.role_key = 'student' OR r.role_name LIKE '%学生%')
            ) THEN 'STUDENT'
            ELSE 'STUDENT'
        END INTO user_role;

    -- 插入到 user 表
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
        NEW.email,
        NEW.nick_name,
        NEW.avatar,
        user_role,
        CASE
            WHEN NEW.status = '0' THEN 'ACTIVE'
            WHEN NEW.status = '1' THEN 'INACTIVE'
            ELSE 'ACTIVE'
        END,
        NEW.create_time,
        NEW.update_time
    );
END$$
DELIMITER ;

-- ================================================================================
-- 第三步：创建触发器 - sys_user 更新时同步到 user
-- ================================================================================

DROP TRIGGER IF EXISTS `trg_sys_user_update`;

DELIMITER $$
CREATE TRIGGER `trg_sys_user_update`
AFTER UPDATE ON `sys_user`
FOR EACH ROW
BEGIN
    DECLARE user_role VARCHAR(20);

    -- 查询用户的角色
    SELECT
        CASE
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = NEW.user_id
                AND (r.role_id = 1 OR r.role_key = 'admin' OR r.role_name = '管理员')
            ) THEN 'ADMIN'
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = NEW.user_id
                AND (r.role_key = 'teacher' OR r.role_name LIKE '%教师%')
            ) THEN 'TEACHER'
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = NEW.user_id
                AND (r.role_key = 'student' OR r.role_name LIKE '%学生%')
            ) THEN 'STUDENT'
            ELSE 'STUDENT'
        END INTO user_role;

    -- 更新 user 表
    UPDATE `user` SET
        `username` = NEW.user_name,
        `password` = NEW.password,
        `email` = NEW.email,
        `real_name` = NEW.nick_name,
        `avatar` = NEW.avatar,
        `role` = user_role,
        `status` = CASE
            WHEN NEW.status = '0' THEN 'ACTIVE'
            WHEN NEW.status = '1' THEN 'INACTIVE'
            ELSE 'ACTIVE'
        END,
        `update_time` = NEW.update_time
    WHERE `sys_user_id` = NEW.user_id;
END$$
DELIMITER ;

-- ================================================================================
-- 第四步：创建触发器 - sys_user 删除时同步到 user
-- ================================================================================

DROP TRIGGER IF EXISTS `trg_sys_user_delete`;

DELIMITER $$
CREATE TRIGGER `trg_sys_user_delete`
AFTER UPDATE ON `sys_user`
FOR EACH ROW
BEGIN
    -- 若依使用软删除（del_flag = '2'）
    IF NEW.del_flag = '2' THEN
        UPDATE `user` SET
            `status` = 'DELETED',
            `update_time` = NOW()
        WHERE `sys_user_id` = NEW.user_id;
    END IF;
END$$
DELIMITER ;

-- ================================================================================
-- 第五步：创建触发器 - sys_user_role 变化时同步角色到 user
-- ================================================================================

-- 当用户角色关联插入时，更新 user 表的 role 字段
DROP TRIGGER IF EXISTS `trg_sys_user_role_insert`;

DELIMITER $$
CREATE TRIGGER `trg_sys_user_role_insert`
AFTER INSERT ON `sys_user_role`
FOR EACH ROW
BEGIN
    DECLARE user_role VARCHAR(20);

    -- 查询用户的角色
    SELECT
        CASE
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = NEW.user_id
                AND (r.role_id = 1 OR r.role_key = 'admin' OR r.role_name = '管理员')
            ) THEN 'ADMIN'
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = NEW.user_id
                AND (r.role_key = 'teacher' OR r.role_name LIKE '%教师%')
            ) THEN 'TEACHER'
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = NEW.user_id
                AND (r.role_key = 'student' OR r.role_name LIKE '%学生%')
            ) THEN 'STUDENT'
            ELSE 'STUDENT'
        END INTO user_role;

    -- 更新 user 表的角色
    UPDATE `user` SET
        `role` = user_role,
        `update_time` = NOW()
    WHERE `sys_user_id` = NEW.user_id;
END$$
DELIMITER ;

-- 当用户角色关联删除时，更新 user 表的 role 字段
DROP TRIGGER IF EXISTS `trg_sys_user_role_delete`;

DELIMITER $$
CREATE TRIGGER `trg_sys_user_role_delete`
AFTER DELETE ON `sys_user_role`
FOR EACH ROW
BEGIN
    DECLARE user_role VARCHAR(20);

    -- 查询用户的剩余角色
    SELECT
        CASE
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = OLD.user_id
                AND (r.role_id = 1 OR r.role_key = 'admin' OR r.role_name = '管理员')
            ) THEN 'ADMIN'
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = OLD.user_id
                AND (r.role_key = 'teacher' OR r.role_name LIKE '%教师%')
            ) THEN 'TEACHER'
            WHEN EXISTS (
                SELECT 1 FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.role_id
                WHERE ur.user_id = OLD.user_id
                AND (r.role_key = 'student' OR r.role_name LIKE '%学生%')
            ) THEN 'STUDENT'
            ELSE 'STUDENT'
        END INTO user_role;

    -- 更新 user 表的角色
    UPDATE `user` SET
        `role` = user_role,
        `update_time` = NOW()
    WHERE `sys_user_id` = OLD.user_id;
END$$
DELIMITER ;

-- ================================================================================
-- 第六步：数据迁移 - 将现有 user 表数据关联到 sys_user
-- ================================================================================

-- 注意：这个脚本需要根据实际情况调整
-- 如果已有数据，需要手动建立关联关系

-- 示例：根据 username 匹配
UPDATE `user` u
INNER JOIN `sys_user` su ON u.username = su.user_name
SET u.sys_user_id = su.user_id
WHERE u.sys_user_id IS NULL;

-- ================================================================================
-- 验证脚本
-- ================================================================================

-- 查看关联情况
SELECT 
    u.id AS user_id,
    u.username,
    u.sys_user_id,
    su.user_id AS sys_user_id_check,
    su.user_name
FROM `user` u
LEFT JOIN `sys_user` su ON u.sys_user_id = su.user_id
LIMIT 10;

