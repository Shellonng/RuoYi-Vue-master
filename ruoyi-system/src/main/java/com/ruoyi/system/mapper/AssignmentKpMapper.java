package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.AssignmentKp;
import org.apache.ibatis.annotations.Param;

/**
 * 作业与知识点关联Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-23
 */
public interface AssignmentKpMapper 
{
    /**
     * 查询作业知识点关联信息
     *
     * @param id 关联ID
     * @return 关联信息
     */
    public AssignmentKp selectAssignmentKpById(Long id);

    /**
     * 查询作业知识点关联列表
     *
     * @param assignmentKp 关联信息
     * @return 关联集合
     */
    public List<AssignmentKp> selectAssignmentKpList(AssignmentKp assignmentKp);

    /**
     * 根据作业ID查询关联的知识点列表（含知识点详情）
     *
     * @param assignmentId 作业ID
     * @return 关联集合
     */
    public List<AssignmentKp> selectAssignmentKpListByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * 根据知识点ID查询关联的作业列表
     *
     * @param kpId 知识点ID
     * @return 关联集合
     */
    public List<AssignmentKp> selectAssignmentKpListByKpId(@Param("kpId") Long kpId);

    /**
     * 检查作业和知识点是否已关联
     *
     * @param assignmentId 作业ID
     * @param kpId 知识点ID
     * @return 关联信息，不存在返回null
     */
    public AssignmentKp checkAssignmentKpExists(@Param("assignmentId") Long assignmentId, @Param("kpId") Long kpId);

    /**
     * 新增作业知识点关联
     *
     * @param assignmentKp 关联信息
     * @return 结果
     */
    public int insertAssignmentKp(AssignmentKp assignmentKp);

    /**
     * 批量新增作业知识点关联
     *
     * @param assignmentKpList 关联列表
     * @return 结果
     */
    public int batchInsertAssignmentKp(List<AssignmentKp> assignmentKpList);

    /**
     * 删除作业知识点关联
     *
     * @param id 关联ID
     * @return 结果
     */
    public int deleteAssignmentKpById(Long id);

    /**
     * 批量删除作业知识点关联
     *
     * @param ids 需要删除的关联ID
     * @return 结果
     */
    public int deleteAssignmentKpByIds(Long[] ids);

    /**
     * 删除作业的所有知识点关联
     *
     * @param assignmentId 作业ID
     * @return 结果
     */
    public int deleteAssignmentKpByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * 统计作业关联的知识点数量
     * 
     * @param assignmentId 作业ID
     * @return 知识点数量
     */
    int countKpByAssignmentId(@Param("assignmentId") Long assignmentId);
}

