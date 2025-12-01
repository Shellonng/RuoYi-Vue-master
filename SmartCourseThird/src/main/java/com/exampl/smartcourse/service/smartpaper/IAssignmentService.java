package com.exampl.smartcourse.service.smartpaper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exampl.smartcourse.common.smartpaper.PageResult;
import com.exampl.smartcourse.dto.smartpaper.request.CreateAssignmentRequest;
import com.exampl.smartcourse.dto.smartpaper.request.PublishAssignmentRequest;
import com.exampl.smartcourse.dto.smartpaper.request.UpdateAssignmentRequest;
import com.exampl.smartcourse.dto.smartpaper.response.AssignmentDetailResponse;
import com.exampl.smartcourse.dto.smartpaper.response.AssignmentResponse;
import com.exampl.smartcourse.entity.smartpaper.Assignment;

import java.util.List;

/**
 * 作业/考试服务接口
 *
 * @author 开发团队
 * @since 2025-11-17
 */
public interface IAssignmentService extends IService<Assignment> {

    /**
     * 手动创建作业（指定题目）
     *
     * @param title       作业标题
     * @param courseId    课程ID
     * @param type        类型
     * @param description 描述
     * @param questionIds 题目ID列表
     * @param userId      创建人ID
     * @return 作业ID
     */
    Long createAssignment(CreateAssignmentRequest request, Long userId);

    /**
     * 更新作业信息
     *
     * @param assignmentId 作业ID
     * @param title        作业标题
     * @param description  描述
     * @param questionIds  题目ID列表（可选）
     * @return 是否成功
     */
    Boolean updateAssignment(Long assignmentId, UpdateAssignmentRequest request);

    /**
     * 发布作业
     *
     * @param request 发布请求
     * @return 是否成功
     */
    Boolean publishAssignment(PublishAssignmentRequest request);

    /**
     * 取消发布作业
     *
     * @param assignmentId 作业ID
     * @return 是否成功
     */
    Boolean unpublishAssignment(Long assignmentId);

    /**
     * 删除作业
     *
     * @param assignmentId 作业ID
     * @return 是否成功
     */
    Boolean deleteAssignment(Long assignmentId);

    /**
     * 查询作业详情
     *
     * @param assignmentId 作业ID
     * @return 作业详情
     */
    AssignmentDetailResponse getAssignmentDetail(Long assignmentId);

    /**
     * 查询作业列表（教师端）
     *
     * @param courseId        课程ID
     * @param type            类型
     * @param status          状态
     * @param publisherUserId 发布者ID
     * @param page            页码
     * @param pageSize        每页数量
     * @return 分页结果
     */
    PageResult<AssignmentResponse> queryAssignments(Long courseId,
            String type,
            Integer status,
            Long publisherUserId,
            Integer page,
            Integer pageSize);

    /**
     * 添加题目到作业
     *
     * @param assignmentId 作业ID
     * @param questionIds  题目ID列表
     * @return 是否成功
     */
    Boolean addQuestionsToAssignment(Long assignmentId, List<Long> questionIds);

    /**
     * 从作业中移除题目
     *
     * @param assignmentId 作业ID
     * @param questionIds  题目ID列表
     * @return 是否成功
     */
    /**
     * 智能组卷
     *
     * @param request 智能组卷请求
     * @param userId  创建人ID
     * @return 作业ID
     */
    Long smartGenerate(com.exampl.smartcourse.dto.smartpaper.request.SmartPaperRequest request, Long userId);

    Boolean removeQuestionsFromAssignment(Long assignmentId, List<Long> questionIds);
}
