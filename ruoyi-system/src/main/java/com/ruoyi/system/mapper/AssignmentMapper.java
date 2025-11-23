package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Assignment;

/**
 * 任务Mapper接口
 * 
 * @author DYH
 * @date 2025-11-18
 */
public interface AssignmentMapper 
{
    /**
     * 查询任务
     * 
     * @param id 任务主键
     * @return 任务
     */
    public Assignment selectAssignmentById(Long id);

    /**
     * 查询任务列表
     * 
     * @param assignment 任务
     * @return 任务集合
     */
    public List<Assignment> selectAssignmentList(Assignment assignment);

    /**
     * 新增任务
     * 
     * @param assignment 任务
     * @return 结果
     */
    public int insertAssignment(Assignment assignment);

    /**
     * 修改任务
     * 
     * @param assignment 任务
     * @return 结果
     */
    public int updateAssignment(Assignment assignment);

    /**
     * 删除任务
     * 
     * @param id 任务主键
     * @return 结果
     */
    public int deleteAssignmentById(Long id);

    /**
     * 批量删除任务
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAssignmentByIds(Long[] ids);
    
    /**
     * 根据知识点ID查询关联的作业/考试列表
     * 
     * @param kpId 知识点ID
     * @return 作业集合
     */
    public List<Assignment> selectAssignmentsByKnowledgePointId(Long kpId);
}
