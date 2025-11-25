package com.ruoyi.system.service;

import com.ruoyi.system.domain.CourseEnrollmentRequest;

import java.util.List;

/**
 * 选课申请Service接口
 */
public interface ICourseEnrollmentRequestService {
    /**
     * 查询选课申请列表
     *
     * @param enrollment 选课申请
     * @return 选课申请集合
     */
    List<CourseEnrollmentRequest> selectEnrollmentList(CourseEnrollmentRequest enrollment);

    /**
     * 查询教师所教课程的选课申请列表
     *
     * @return 选课申请集合
     */
    List<CourseEnrollmentRequest> selectEnrollmentListByTeacher();

    /**
     * 查询选课申请详情
     *
     * @param id 选课申请ID
     * @return 选课申请
     */
    CourseEnrollmentRequest selectEnrollmentById(Long id);

    /**
     * 新增选课申请
     *
     * @param enrollment 选课申请
     * @return 结果
     */
    int insertEnrollment(CourseEnrollmentRequest enrollment);

    /**
     * 修改选课申请
     *
     * @param enrollment 选课申请
     * @return 结果
     */
    int updateEnrollment(CourseEnrollmentRequest enrollment);

    /**
     * 批量删除选课申请
     *
     * @param ids 需要删除的选课申请ID
     * @return 结果
     */
    int deleteEnrollmentByIds(Long[] ids);

    /**
     * 删除选课申请信息
     *
     * @param id 选课申请ID
     * @return 结果
     */
    int deleteEnrollmentById(Long id);

    /**
     * 批量审核通过
     *
     * @param ids 选课申请ID数组
     * @param reviewComment 审核意见
     * @return 结果
     */
    int batchApprove(Long[] ids, String reviewComment);

    /**
     * 批量拒绝
     *
     * @param ids 选课申请ID数组
     * @param reviewComment 审核意见
     * @return 结果
     */
    int batchReject(Long[] ids, String reviewComment);
}
