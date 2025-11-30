package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.CourseStudentMapper;
import com.ruoyi.system.domain.CourseStudent;
import com.ruoyi.system.service.ICourseStudentService;

/**
 * 学生选课Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class CourseStudentServiceImpl implements ICourseStudentService {
    @Autowired
    private CourseStudentMapper courseStudentMapper;

    /**
     * 查询学生选课
     * 
     * @param id 学生选课主键
     * @return 学生选课
     */
    @Override
    public CourseStudent selectCourseStudentById(Long id) {
        return courseStudentMapper.selectCourseStudentById(id);
    }

    /**
     * 根据课程ID和学生ID查询选课记录
     * 
     * @param courseId 课程ID
     * @param studentUserId 学生ID
     * @return 学生选课
     */
    @Override
    public CourseStudent selectByCourseAndStudent(Long courseId, Long studentUserId) {
        return courseStudentMapper.selectByCourseAndStudent(courseId, studentUserId);
    }

    /**
     * 查询学生选课列表
     * 
     * @param courseStudent 学生选课
     * @return 学生选课
     */
    @Override
    public List<CourseStudent> selectCourseStudentList(CourseStudent courseStudent) {
        return courseStudentMapper.selectCourseStudentList(courseStudent);
    }

    /**
     * 新增学生选课
     * 
     * @param courseStudent 学生选课
     * @return 结果
     */
    @Override
    public int insertCourseStudent(CourseStudent courseStudent) {
        if (courseStudent.getEnrollTime() == null) {
            courseStudent.setEnrollTime(new Date());
        }
        if (courseStudent.getCollected() == null) {
            courseStudent.setCollected(0);
        }
        if (courseStudent.getIsDeleted() == null) {
            courseStudent.setIsDeleted(0);
        }
        return courseStudentMapper.insertCourseStudent(courseStudent);
    }

    /**
     * 修改学生选课
     * 
     * @param courseStudent 学生选课
     * @return 结果
     */
    @Override
    public int updateCourseStudent(CourseStudent courseStudent) {
        return courseStudentMapper.updateCourseStudent(courseStudent);
    }

    /**
     * 批量删除学生选课
     * 
     * @param ids 需要删除的学生选课主键
     * @return 结果
     */
    @Override
    public int deleteCourseStudentByIds(Long[] ids) {
        return courseStudentMapper.deleteCourseStudentByIds(ids);
    }

    /**
     * 删除学生选课信息
     * 
     * @param id 学生选课主键
     * @return 结果
     */
    @Override
    public int deleteCourseStudentById(Long id) {
        return courseStudentMapper.deleteCourseStudentById(id);
    }

    /**
     * 软删除学生选课（将is_deleted设置为1）
     * 
     * @param courseId 课程ID
     * @param studentUserId 学生ID
     * @return 结果
     */
    @Override
    public int softDeleteByCourseAndStudent(Long courseId, Long studentUserId) {
        return courseStudentMapper.softDeleteByCourseAndStudent(courseId, studentUserId);
    }

    /**
     * 恢复学生选课（将is_deleted设置为0）
     * 
     * @param courseId 课程ID
     * @param studentUserId 学生ID
     * @return 结果
     */
    @Override
    public int restoreByCourseAndStudent(Long courseId, Long studentUserId) {
        return courseStudentMapper.restoreByCourseAndStudent(courseId, studentUserId);
    }

    /**
     * 添加或恢复学生选课
     * 如果记录不存在则新增，如果存在但已删除则恢复
     * 
     * @param courseId 课程ID
     * @param studentUserId 学生ID
     * @return 结果
     */
    @Override
    public int addOrRestoreCourseStudent(Long courseId, Long studentUserId) {
        // 先查询是否存在记录
        CourseStudent existing = courseStudentMapper.selectByCourseAndStudent(courseId, studentUserId);
        
        if (existing == null) {
            // 不存在则新增
            CourseStudent newRecord = new CourseStudent();
            newRecord.setCourseId(courseId);
            newRecord.setStudentUserId(studentUserId);
            newRecord.setEnrollTime(new Date());
            newRecord.setCollected(0);
            newRecord.setIsDeleted(0);
            return courseStudentMapper.insertCourseStudent(newRecord);
        } else if (existing.getIsDeleted() == 1) {
            // 存在但已删除，则恢复
            return courseStudentMapper.restoreByCourseAndStudent(courseId, studentUserId);
        }
        
        // 记录已存在且未删除，返回0表示无需操作
        return 0;
    }
}
