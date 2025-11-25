package com.ruoyi.system.service.impl;

import com.ruoyi.system.utils.BusinessUserUtils;
import com.ruoyi.system.domain.CourseEnrollmentRequest;
import com.ruoyi.system.mapper.CourseEnrollmentRequestMapper;
import com.ruoyi.system.service.ICourseEnrollmentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 选课申请Service业务层处理
 */
@Service
public class CourseEnrollmentRequestServiceImpl implements ICourseEnrollmentRequestService {
    @Autowired
    private CourseEnrollmentRequestMapper enrollmentMapper;

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
    public int updateEnrollment(CourseEnrollmentRequest enrollment) {
        return enrollmentMapper.updateEnrollment(enrollment);
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
    public int batchApprove(Long[] ids, String reviewComment) {
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
    public int batchReject(Long[] ids, String reviewComment) {
        return enrollmentMapper.batchReject(ids, reviewComment);
    }
}
