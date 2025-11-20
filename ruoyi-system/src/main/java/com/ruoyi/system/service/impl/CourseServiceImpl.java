package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.Course;
import com.ruoyi.system.mapper.CourseMapper;
import com.ruoyi.system.service.ICourseService;
import com.ruoyi.system.utils.BusinessUserUtils;

/**
 * 课程 服务层实现
 *
 * @author ruoyi
 */
@Service
public class CourseServiceImpl implements ICourseService
{
    @Autowired
    private CourseMapper courseMapper;

    /**
     * 查询课程信息
     *
     * @param id 课程ID
     * @return 课程信息
     */
    @Override
    public Course selectCourseById(Long id)
    {
        Course course = courseMapper.selectCourseById(id);
        if (course != null) {
            // 计算课程进度
            course.setProgress(calculateProgress(course));
        }
        return course;
    }

    /**
     * 查询课程列表
     *
     * @param course 课程信息
     * @return 课程集合
     */
    @Override
    public List<Course> selectCourseList(Course course)
    {
        List<Course> list = courseMapper.selectCourseList(course);
        // 为每个课程计算进度
        for (Course c : list) {
            c.setProgress(calculateProgress(c));
        }
        return list;
    }

    /**
     * 查询当前教师的课程列表
     *
     * @return 课程集合
     */
    @Override
    public List<Course> selectMyCourseList()
    {
        // 获取当前用户的业务ID（user.id）
        Long userId = BusinessUserUtils.getCurrentBusinessUserId();
        List<Course> list = courseMapper.selectCourseListByTeacherId(userId);
        // 为每个课程计算进度
        for (Course course : list) {
            course.setProgress(calculateProgress(course));
        }
        return list;
    }

    /**
     * 新增课程
     *
     * @param course 课程信息
     * @return 结果
     */
    @Override
    public int insertCourse(Course course)
    {
        // 获取当前用户的业务ID作为任课教师
        Long userId = BusinessUserUtils.getCurrentBusinessUserId();
        course.setTeacherUserId(userId);

        // 设置默认值
        if (course.getStatus() == null || course.getStatus().isEmpty()) {
            course.setStatus("未开始");
        }
        if (course.getStudentCount() == null) {
            course.setStudentCount(0);
        }

        return courseMapper.insertCourse(course);
    }

    /**
     * 修改课程
     *
     * @param course 课程信息
     * @return 结果
     */
    @Override
    public int updateCourse(Course course)
    {
        return courseMapper.updateCourse(course);
    }

    /**
     * 批量删除课程信息（软删除）
     *
     * @param ids 需要删除的课程ID
     * @return 结果
     */
    @Override
    public int deleteCourseByIds(Long[] ids)
    {
        return courseMapper.softDeleteCourseByIds(ids);
    }

    /**
     * 删除课程信息（软删除）
     *
     * @param id 课程ID
     * @return 结果
     */
    @Override
    public int deleteCourseById(Long id)
    {
        return courseMapper.softDeleteCourseById(id);
    }

    /**
     * 查询课程统计信息
     *
     * @return 统计信息
     */
    @Override
    public Map<String, Object> selectCourseStats()
    {
        // 获取当前用户的业务ID
        Long userId = BusinessUserUtils.getCurrentBusinessUserId();
        return courseMapper.selectCourseStats(userId);
    }

    /**
     * 计算课程进度
     *
     * @param course 课程信息
     * @return 进度百分比
     */
    private Integer calculateProgress(Course course)
    {
        if (course == null || course.getStartTime() == null || course.getEndTime() == null) {
            return 0;
        }

        Date now = new Date();
        Date startTime = course.getStartTime();
        Date endTime = course.getEndTime();

        // 如果还未开始
        if (now.before(startTime)) {
            return 0;
        }

        // 如果已经结束
        if (now.after(endTime)) {
            return 100;
        }

        // 计算进度百分比
        long totalDuration = endTime.getTime() - startTime.getTime();
        long elapsedDuration = now.getTime() - startTime.getTime();

        if (totalDuration <= 0) {
            return 0;
        }

        return (int) ((elapsedDuration * 100) / totalDuration);
    }
}
