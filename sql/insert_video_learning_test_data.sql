-- 插入视频学习行为测试数据
-- 学生ID: 22, 24, 27, 28, 29, 33, 34, 35, 36
-- 视频ID: 2, 3, 4
-- 模拟今天不同时间段的观看行为

-- 清空已有数据（可选）
-- TRUNCATE TABLE video_learning_behavior;

-- 早上8:00-9:00 高峰期
-- 学生22 观看视频2，8:10开始，观看了30分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (22, 2, 1800, 3600, 50.00, 1, 0, 2, 3, 1.0, DATE_ADD(CURDATE(), INTERVAL 8 HOUR), DATE_ADD(CURDATE(), INTERVAL 8 HOUR) + INTERVAL 30 MINUTE);

-- 学生24 观看视频3，8:15开始，观看了25分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (24, 3, 1500, 2400, 62.50, 1, 0, 1, 2, 1.0, DATE_ADD(CURDATE(), INTERVAL 8 HOUR) + INTERVAL 15 MINUTE, DATE_ADD(CURDATE(), INTERVAL 8 HOUR) + INTERVAL 40 MINUTE);

-- 学生27 观看视频2，8:20开始，观看了40分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (27, 2, 2400, 3600, 66.67, 1, 0, 3, 4, 1.0, DATE_ADD(CURDATE(), INTERVAL 8 HOUR) + INTERVAL 20 MINUTE, DATE_ADD(CURDATE(), INTERVAL 9 HOUR));

-- 学生28 观看视频4，8:30开始，观看了20分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (28, 4, 1200, 1800, 66.67, 1, 0, 1, 1, 1.0, DATE_ADD(CURDATE(), INTERVAL 8 HOUR) + INTERVAL 30 MINUTE, DATE_ADD(CURDATE(), INTERVAL 8 HOUR) + INTERVAL 50 MINUTE);

-- 上午10:00-11:00
-- 学生29 观看视频3，10:00开始，观看了45分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (29, 3, 2700, 2400, 100.00, 1, 1, 0, 2, 1.0, DATE_ADD(CURDATE(), INTERVAL 10 HOUR), DATE_ADD(CURDATE(), INTERVAL 10 HOUR) + INTERVAL 45 MINUTE);

-- 学生33 观看视频2，10:15开始，观看了35分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (33, 2, 2100, 3600, 58.33, 1, 0, 2, 3, 1.0, DATE_ADD(CURDATE(), INTERVAL 10 HOUR) + INTERVAL 15 MINUTE, DATE_ADD(CURDATE(), INTERVAL 10 HOUR) + INTERVAL 50 MINUTE);

-- 下午14:00-15:00
-- 学生34 观看视频4，14:00开始，观看了30分钟（看完）
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (34, 4, 1800, 1800, 100.00, 1, 1, 0, 1, 1.0, DATE_ADD(CURDATE(), INTERVAL 14 HOUR), DATE_ADD(CURDATE(), INTERVAL 14 HOUR) + INTERVAL 30 MINUTE);

-- 学生35 观看视频3，14:20开始，观看了25分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (35, 3, 1500, 2400, 62.50, 1, 0, 1, 2, 1.5, DATE_ADD(CURDATE(), INTERVAL 14 HOUR) + INTERVAL 20 MINUTE, DATE_ADD(CURDATE(), INTERVAL 14 HOUR) + INTERVAL 45 MINUTE);

-- 学生36 观看视频2，14:30开始，观看了50分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (36, 2, 3000, 3600, 83.33, 1, 0, 2, 4, 1.0, DATE_ADD(CURDATE(), INTERVAL 14 HOUR) + INTERVAL 30 MINUTE, DATE_ADD(CURDATE(), INTERVAL 15 HOUR) + INTERVAL 20 MINUTE);

-- 下午16:00-17:00
-- 学生22 观看视频4，16:00开始，观看了28分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (22, 4, 1680, 1800, 93.33, 1, 0, 1, 2, 1.0, DATE_ADD(CURDATE(), INTERVAL 16 HOUR), DATE_ADD(CURDATE(), INTERVAL 16 HOUR) + INTERVAL 28 MINUTE);

-- 学生24 观看视频2，16:30开始，观看了40分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (24, 2, 2400, 3600, 66.67, 1, 0, 2, 3, 1.0, DATE_ADD(CURDATE(), INTERVAL 16 HOUR) + INTERVAL 30 MINUTE, DATE_ADD(CURDATE(), INTERVAL 17 HOUR) + INTERVAL 10 MINUTE);

-- 晚上19:00-20:00 高峰期
-- 学生27 观看视频3，19:00开始，观看了40分钟（看完）
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (27, 3, 2400, 2400, 100.00, 2, 1, 0, 1, 1.0, DATE_ADD(CURDATE(), INTERVAL 19 HOUR), DATE_ADD(CURDATE(), INTERVAL 19 HOUR) + INTERVAL 40 MINUTE);

-- 学生28 观看视频2，19:10开始，观看了55分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (28, 2, 3300, 3600, 91.67, 1, 0, 1, 2, 1.0, DATE_ADD(CURDATE(), INTERVAL 19 HOUR) + INTERVAL 10 MINUTE, DATE_ADD(CURDATE(), INTERVAL 20 HOUR) + INTERVAL 5 MINUTE);

-- 学生29 观看视频4，19:15开始，观看了30分钟（看完）
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (29, 4, 1800, 1800, 100.00, 2, 1, 0, 0, 1.5, DATE_ADD(CURDATE(), INTERVAL 19 HOUR) + INTERVAL 15 MINUTE, DATE_ADD(CURDATE(), INTERVAL 19 HOUR) + INTERVAL 45 MINUTE);

-- 学生33 观看视频3，19:30开始，观看了35分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (33, 3, 2100, 2400, 87.50, 1, 0, 2, 2, 1.0, DATE_ADD(CURDATE(), INTERVAL 19 HOUR) + INTERVAL 30 MINUTE, DATE_ADD(CURDATE(), INTERVAL 20 HOUR) + INTERVAL 5 MINUTE);

-- 学生34 观看视频2，19:45开始，观看了45分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (34, 2, 2700, 3600, 75.00, 1, 0, 3, 4, 1.0, DATE_ADD(CURDATE(), INTERVAL 19 HOUR) + INTERVAL 45 MINUTE, DATE_ADD(CURDATE(), INTERVAL 20 HOUR) + INTERVAL 30 MINUTE);

-- 晚上20:00-21:00 高峰期
-- 学生35 观看视频4，20:00开始，观看了25分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (35, 4, 1500, 1800, 83.33, 1, 0, 1, 1, 1.0, DATE_ADD(CURDATE(), INTERVAL 20 HOUR), DATE_ADD(CURDATE(), INTERVAL 20 HOUR) + INTERVAL 25 MINUTE);

-- 学生36 观看视频3，20:10开始，观看了38分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (36, 3, 2280, 2400, 95.00, 1, 0, 2, 3, 1.0, DATE_ADD(CURDATE(), INTERVAL 20 HOUR) + INTERVAL 10 MINUTE, DATE_ADD(CURDATE(), INTERVAL 20 HOUR) + INTERVAL 48 MINUTE);

-- 学生22 观看视频3，20:30开始，观看了30分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (22, 3, 1800, 2400, 75.00, 1, 0, 1, 2, 1.0, DATE_ADD(CURDATE(), INTERVAL 20 HOUR) + INTERVAL 30 MINUTE, DATE_ADD(CURDATE(), INTERVAL 21 HOUR));

-- 晚上21:00-22:00
-- 学生24 观看视频4，21:00开始，观看了20分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (24, 4, 1200, 1800, 66.67, 1, 0, 1, 1, 1.0, DATE_ADD(CURDATE(), INTERVAL 21 HOUR), DATE_ADD(CURDATE(), INTERVAL 21 HOUR) + INTERVAL 20 MINUTE);

-- 学生27 观看视频4，21:15开始，观看了15分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (27, 4, 900, 1800, 50.00, 1, 0, 2, 1, 1.5, DATE_ADD(CURDATE(), INTERVAL 21 HOUR) + INTERVAL 15 MINUTE, DATE_ADD(CURDATE(), INTERVAL 21 HOUR) + INTERVAL 30 MINUTE);

-- 凌晨1:00-2:00 少量学生
-- 学生28 观看视频3，1:00开始，观看了35分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (28, 3, 2100, 2400, 87.50, 1, 0, 1, 2, 1.0, DATE_ADD(CURDATE(), INTERVAL 1 HOUR), DATE_ADD(CURDATE(), INTERVAL 1 HOUR) + INTERVAL 35 MINUTE);

-- 中午12:00-13:00
-- 学生29 观看视频2，12:00开始，观看了20分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (29, 2, 1200, 3600, 33.33, 1, 0, 2, 1, 1.0, DATE_ADD(CURDATE(), INTERVAL 12 HOUR), DATE_ADD(CURDATE(), INTERVAL 12 HOUR) + INTERVAL 20 MINUTE);

-- 学生33 观看视频4，12:30开始，观看了18分钟
INSERT INTO video_learning_behavior (student_id, video_id, watch_duration, video_duration, completion_rate, watch_count, is_completed, fast_forward_count, pause_count, playback_speed, first_watch_at, last_watch_at)
VALUES (33, 4, 1080, 1800, 60.00, 1, 0, 1, 1, 1.0, DATE_ADD(CURDATE(), INTERVAL 12 HOUR) + INTERVAL 30 MINUTE, DATE_ADD(CURDATE(), INTERVAL 12 HOUR) + INTERVAL 48 MINUTE);

-- 查询验证数据
SELECT 
    HOUR(last_watch_at) as hour,
    COUNT(DISTINCT student_id) as student_count,
    COUNT(*) as watch_count
FROM video_learning_behavior
WHERE DATE(last_watch_at) = CURDATE()
GROUP BY HOUR(last_watch_at)
ORDER BY hour;
