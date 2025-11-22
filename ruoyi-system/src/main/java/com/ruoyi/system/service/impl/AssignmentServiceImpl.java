package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.AssignmentMapper;
import com.ruoyi.system.mapper.AssignmentKpMapper;
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
    
    @Autowired
    private AssignmentKpMapper assignmentKpMapper;

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
        // 如果是要发布考试（status=1），则校验是否关联了知识点
        if (assignment.getStatus() != null && assignment.getStatus() == 1L) {
            int kpCount = assignmentKpMapper.countKpByAssignmentId(assignment.getId());
            if (kpCount == 0) {
                throw new ServiceException("该考试仍未关联任何知识点，无法发布！请先关联知识点后再发布。");
            }
        }
        
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
