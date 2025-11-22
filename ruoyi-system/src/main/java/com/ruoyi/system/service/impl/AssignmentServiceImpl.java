package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.AssignmentMapper;
import com.ruoyi.system.domain.Assignment;
import com.ruoyi.system.service.IAssignmentService;

/**
 * 任务Service业务层处理
 * 
 * @author DYH
 * @date 2025-11-18
 */
@Service
public class AssignmentServiceImpl implements IAssignmentService 
{
    @Autowired
    private AssignmentMapper assignmentMapper;

    /**
     * 查询任务
     * 
     * @param id 任务主键
     * @return 任务
     */
    @Override
    public Assignment selectAssignmentById(Long id)
    {
        return assignmentMapper.selectAssignmentById(id);
    }

    /**
     * 查询任务列表
     * 
     * @param assignment 任务
     * @return 任务
     */
    @Override
    public List<Assignment> selectAssignmentList(Assignment assignment)
    {
        return assignmentMapper.selectAssignmentList(assignment);
    }

    /**
     * 新增任务
     * 
     * @param assignment 任务
     * @return 结果
     */
    @Override
    public int insertAssignment(Assignment assignment)
    {
        assignment.setCreateTime(DateUtils.getNowDate());
        // 确保默认值正确设置
        if (assignment.getIsDeleted() == null) {
            assignment.setIsDeleted(0);
        }
        if (assignment.getStatus() == null) {
            assignment.setStatus(0L);
        }
        if (assignment.getMode() == null) {
            assignment.setMode("question");
        }
        return assignmentMapper.insertAssignment(assignment);
    }

    /**
     * 修改任务
     * 
     * @param assignment 任务
     * @return 结果
     */
    @Override
    public int updateAssignment(Assignment assignment)
    {
        assignment.setUpdateTime(DateUtils.getNowDate());
        return assignmentMapper.updateAssignment(assignment);
    }

    /**
     * 批量删除任务
     * 
     * @param ids 需要删除的任务主键
     * @return 结果
     */
    @Override
    public int deleteAssignmentByIds(Long[] ids)
    {
        return assignmentMapper.deleteAssignmentByIds(ids);
    }

    /**
     * 删除任务信息
     * 
     * @param id 任务主键
     * @return 结果
     */
    @Override
    public int deleteAssignmentById(Long id)
    {
        return assignmentMapper.deleteAssignmentById(id);
    }
}
