package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CourseStudent;

/**
 * 学生选课Service接口
 * 
 * @author ruoyi
 */
public interface ICourseStudentService {
    /**
     * 查询学生选课
     * 
     * @param id 学生选课主键
     * @return 学生选课
     */
    public CourseStudent selectCourseStudentById(Long id);

    /**
     * 根据课程ID和学生ID查询选课记录
     * 
     * @param courseId 课程ID
     * @param studentUserId 学生ID
     * @return 学生选课
     */
    public CourseStudent selectByCourseAndStudent(Long courseId, Long studentUserId);

    /**
     * 查询学生选课列表
     * 
     * @param courseStudent 学生选课
     * @return 学生选课集合
     */
    public List<CourseStudent> selectCourseStudentList(CourseStudent courseStudent);

    /**
     * 新增学生选课
     * 
     * @param courseStudent 学生选课
     * @return 结果
     */
    public int insertCourseStudent(CourseStudent courseStudent);

    /**
     * 修改学生选课
     * 
     * @param courseStudent 学生选课
     * @return 结果
     */
    public int updateCourseStudent(CourseStudent courseStudent);

    /**
     * 批量删除学生选课
     * 
     * @param ids 需要删除的学生选课主键集合
     * @return 结果
     */
    public int deleteCourseStudentByIds(Long[] ids);

    /**
     * 删除学生选课信息
     * 
     * @param id 学生选课主键
     * @return 结果
     */
    public int deleteCourseStudentById(Long id);

    /**
     * 软删除学生选课（将is_deleted设置为1）
     * 
     * @param courseId 课程ID
     * @param studentUserId 学生ID
     * @return 结果
     */
    public int softDeleteByCourseAndStudent(Long courseId, Long studentUserId);

    /**
     * 恢复学生选课（将is_deleted设置为0）
     * 
     * @param courseId 课程ID
     * @param studentUserId 学生ID
     * @return 结果
     */
    public int restoreByCourseAndStudent(Long courseId, Long studentUserId);

    /**
     * 添加或恢复学生选课
     * 如果记录不存在则新增，如果存在但已删除则恢复
     * 
     * @param courseId 课程ID
     * @param studentUserId 学生ID
     * @return 结果
     */
    public int addOrRestoreCourseStudent(Long courseId, Long studentUserId);
}
