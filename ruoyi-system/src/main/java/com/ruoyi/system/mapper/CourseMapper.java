package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Course;
import org.apache.ibatis.annotations.Param;

/**
 * 课程表 数据层
 *
 * @author ruoyi
 */
public interface CourseMapper
{
    /**
     * 查询课程信息
     *
     * @param id 课程ID
     * @return 课程信息
     */
    public Course selectCourseById(Long id);

    /**
     * 查询课程列表
     *
     * @param course 课程信息
     * @return 课程集合
     */
    public List<Course> selectCourseList(Course course);

    /**
     * 根据教师ID查询课程列表
     *
     * @param teacherUserId 教师用户ID
     * @return 课程集合
     */
    public List<Course> selectCourseListByTeacherId(@Param("teacherUserId") Long teacherUserId);

    /**
     * 新增课程
     *
     * @param course 课程信息
     * @return 结果
     */
    public int insertCourse(Course course);

    /**
     * 修改课程
     *
     * @param course 课程信息
     * @return 结果
     */
    public int updateCourse(Course course);

    /**
     * 删除课程（物理删除）
     *
     * @param id 课程ID
     * @return 结果
     */
    public int deleteCourseById(Long id);

    /**
     * 软删除课程
     *
     * @param id 课程ID
     * @return 结果
     */
    public int softDeleteCourseById(Long id);

    /**
     * 批量删除课程信息（物理删除）
     *
     * @param ids 需要删除的课程ID
     * @return 结果
     */
    public int deleteCourseByIds(Long[] ids);

    /**
     * 批量软删除课程信息
     *
     * @param ids 需要删除的课程ID
     * @return 结果
     */
    public int softDeleteCourseByIds(Long[] ids);

    /**
     * 查询课程统计信息
     *
     * @param teacherUserId 教师用户ID
     * @return 统计信息Map
     */
    public java.util.Map<String, Object> selectCourseStats(@Param("teacherUserId") Long teacherUserId);
}
