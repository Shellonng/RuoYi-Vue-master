package com.ruoyi.system.service.impl;

import com.ruoyi.system.utils.BusinessUserUtils;
import com.ruoyi.system.domain.CourseEnrollmentRequest;
import com.ruoyi.system.mapper.CourseEnrollmentRequestMapper;
import com.ruoyi.system.service.ICourseEnrollmentRequestService;
import com.ruoyi.system.service.ICourseStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 选课申请Service业务层处理
 */
@Service
public class CourseEnrollmentRequestServiceImpl implements ICourseEnrollmentRequestService {
    @Autowired
    private CourseEnrollmentRequestMapper enrollmentMapper;

    @Autowired
    private ICourseStudentService courseStudentService;

    /**
     * 查询选课申请列表
     *
     * @param enrollment 选课申请
     * @return 选课申请
     */
    @Override
    public List<CourseEnrollmentRequest> selectEnrollmentList(CourseEnrollmentRequest enrollment) {
        return enrollmentMapper.selectEnrollmentList(enrollment);
    }

    /**
     * 查询教师所教课程的选课申请列表
     *
     * @return 选课申请集合
     */
    @Override
    public List<CourseEnrollmentRequest> selectEnrollmentListByTeacher() {
        Long teacherUserId = BusinessUserUtils.getCurrentBusinessUserId();
        return enrollmentMapper.selectEnrollmentListByTeacher(teacherUserId);
    }

    /**
     * 查询选课申请详情
     *
     * @param id 选课申请ID
     * @return 选课申请
     */
    @Override
    public CourseEnrollmentRequest selectEnrollmentById(Long id) {
        return enrollmentMapper.selectEnrollmentById(id);
    }

    /**
     * 新增选课申请
     *
     * @param enrollment 选课申请
     * @return 结果
     */
    @Override
    public int insertEnrollment(CourseEnrollmentRequest enrollment) {
        return enrollmentMapper.insertEnrollment(enrollment);
    }

    /**
     * 修改选课申请
     *
     * @param enrollment 选课申请
     * @return 结果
     */
    @Override
    @Transactional
    public int updateEnrollment(CourseEnrollmentRequest enrollment) {
        // 先查询原来的状态
        CourseEnrollmentRequest oldEnrollment = enrollmentMapper.selectEnrollmentById(enrollment.getId());
        if (oldEnrollment == null) {
            return 0;
        }

        // 更新选课申请状态
        int result = enrollmentMapper.updateEnrollment(enrollment);

        // 如果更新成功，根据新状态操作 course_student 表
        if (result > 0 && enrollment.getStatus() != null) {
            Long courseId = oldEnrollment.getCourseId();
            Long studentUserId = oldEnrollment.getStudentUserId();

            if (enrollment.getStatus() == 1) {
                // 状态改为通过：添加或恢复学生选课记录
                courseStudentService.addOrRestoreCourseStudent(courseId, studentUserId);
            } else if (enrollment.getStatus() == 2) {
                // 状态改为拒绝：软删除学生选课记录
                courseStudentService.softDeleteByCourseAndStudent(courseId, studentUserId);
            }
        }

        return result;
    }

    /**
     * 批量删除选课申请
     *
     * @param ids 需要删除的选课申请ID
     * @return 结果
     */
    @Override
    public int deleteEnrollmentByIds(Long[] ids) {
        return enrollmentMapper.deleteEnrollmentByIds(ids);
    }

    /**
     * 删除选课申请信息
     *
     * @param id 选课申请ID
     * @return 结果
     */
    @Override
    public int deleteEnrollmentById(Long id) {
        return enrollmentMapper.deleteEnrollmentById(id);
    }

    /**
     * 批量审核通过
     *
     * @param ids           选课申请ID数组
     * @param reviewComment 审核意见
     * @return 结果
     */
    @Override
    @Transactional
    public int batchApprove(Long[] ids, String reviewComment) {
        // 先查询所有待审核的申请记录
        for (Long id : ids) {
            CourseEnrollmentRequest enrollment = enrollmentMapper.selectEnrollmentById(id);
            if (enrollment != null) {
                // 添加或恢复学生选课记录
                courseStudentService.addOrRestoreCourseStudent(
                    enrollment.getCourseId(),
                    enrollment.getStudentUserId()
                );
            }
        }
        
        // 批量更新申请状态为通过
        return enrollmentMapper.batchApprove(ids, reviewComment);
    }

    /**
     * 批量拒绝
     *
     * @param ids           选课申请ID数组
     * @param reviewComment 审核意见
     * @return 结果
     */
    @Override
    @Transactional
    public int batchReject(Long[] ids, String reviewComment) {
        // 先查询所有待拒绝的申请记录
        for (Long id : ids) {
            CourseEnrollmentRequest enrollment = enrollmentMapper.selectEnrollmentById(id);
            if (enrollment != null) {
                // 软删除学生选课记录
                courseStudentService.softDeleteByCourseAndStudent(
                    enrollment.getCourseId(),
                    enrollment.getStudentUserId()
                );
            }
        }
        
        // 批量更新申请状态为拒绝
        return enrollmentMapper.batchReject(ids, reviewComment);
    }
}
