package com.exampl.smartcourse.mapper.smartpaper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exampl.smartcourse.entity.smartpaper.Assignment;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * 作业/考试Mapper接口
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@Mapper
@Component("smartPaperAssignmentMapper")
public interface AssignmentMapper extends BaseMapper<Assignment> {

    /**
     * 分页查询作业（带统计信息）
     *
     * @param page           分页对象
     * @param courseId       课程ID
     * @param type           类型
     * @param status         状态
     * @param publisherUserId 发布者ID
     * @return 分页结果
     */
    IPage<Assignment> selectAssignmentPage(Page<Assignment> page,
                                           @Param("courseId") Long courseId,
                                           @Param("type") String type,
                                           @Param("status") Integer status,
                                           @Param("publisherUserId") Long publisherUserId);

    /**
     * 查询作业详情（带题目列表）
     *
     * @param assignmentId 作业ID
     * @return 作业详情
     */
    Assignment selectAssignmentDetail(@Param("assignmentId") Long assignmentId);

}
