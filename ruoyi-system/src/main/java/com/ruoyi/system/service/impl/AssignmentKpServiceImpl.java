package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.AssignmentKp;
import com.ruoyi.system.mapper.AssignmentKpMapper;
import com.ruoyi.system.service.IAssignmentKpService;

/**
 * 作业与知识点关联 服务层实现
 *
 * @author ruoyi
 */
@Service
public class AssignmentKpServiceImpl implements IAssignmentKpService
{
    @Autowired
    private AssignmentKpMapper assignmentKpMapper;

    /**
     * 查询作业知识点关联信息
     *
     * @param id 关联ID
     * @return 关联信息
     */
    @Override
    public AssignmentKp selectAssignmentKpById(Long id)
    {
        return assignmentKpMapper.selectAssignmentKpById(id);
    }

    /**
     * 查询作业知识点关联列表
     *
     * @param assignmentKp 关联信息
     * @return 关联集合
     */
    @Override
    public List<AssignmentKp> selectAssignmentKpList(AssignmentKp assignmentKp)
    {
        return assignmentKpMapper.selectAssignmentKpList(assignmentKp);
    }

    /**
     * 根据作业ID查询关联的知识点列表（含知识点详情）
     *
     * @param assignmentId 作业ID
     * @return 关联集合
     */
    @Override
    public List<AssignmentKp> selectAssignmentKpListByAssignmentId(Long assignmentId)
    {
        return assignmentKpMapper.selectAssignmentKpListByAssignmentId(assignmentId);
    }

    /**
     * 根据知识点ID查询关联的作业列表
     *
     * @param kpId 知识点ID
     * @return 关联集合
     */
    @Override
    public List<AssignmentKp> selectAssignmentKpListByKpId(Long kpId)
    {
        return assignmentKpMapper.selectAssignmentKpListByKpId(kpId);
    }

    /**
     * 新增作业知识点关联
     *
     * @param assignmentKp 关联信息
     * @return 结果
     */
    @Override
    public int insertAssignmentKp(AssignmentKp assignmentKp)
    {
        // 检查是否已存在关联
        AssignmentKp exists = assignmentKpMapper.checkAssignmentKpExists(
            assignmentKp.getAssignmentId(), assignmentKp.getKpId());
        if (exists != null)
        {
            return 0; // 已存在，不重复添加
        }
        return assignmentKpMapper.insertAssignmentKp(assignmentKp);
    }

    /**
     * 批量新增作业知识点关联
     *
     * @param assignmentKpList 关联列表
     * @return 结果
     */
    @Override
    @Transactional
    public int batchInsertAssignmentKp(List<AssignmentKp> assignmentKpList)
    {
        if (assignmentKpList == null || assignmentKpList.isEmpty())
        {
            return 0;
        }
        
        // 过滤掉已存在的关联
        List<AssignmentKp> toInsert = new ArrayList<>();
        for (AssignmentKp ak : assignmentKpList)
        {
            AssignmentKp exists = assignmentKpMapper.checkAssignmentKpExists(
                ak.getAssignmentId(), ak.getKpId());
            if (exists == null)
            {
                toInsert.add(ak);
            }
        }
        
        if (toInsert.isEmpty())
        {
            return 0;
        }
        
        return assignmentKpMapper.batchInsertAssignmentKp(toInsert);
    }

    /**
     * 删除作业知识点关联
     *
     * @param id 关联ID
     * @return 结果
     */
    @Override
    public int deleteAssignmentKpById(Long id)
    {
        return assignmentKpMapper.deleteAssignmentKpById(id);
    }

    /**
     * 批量删除作业知识点关联
     *
     * @param ids 需要删除的关联ID
     * @return 结果
     */
    @Override
    public int deleteAssignmentKpByIds(Long[] ids)
    {
        return assignmentKpMapper.deleteAssignmentKpByIds(ids);
    }

    /**
     * 删除作业的所有知识点关联
     *
     * @param assignmentId 作业ID
     * @return 结果
     */
    @Override
    public int deleteAssignmentKpByAssignmentId(Long assignmentId)
    {
        return assignmentKpMapper.deleteAssignmentKpByAssignmentId(assignmentId);
    }

    /**
     * 为作业批量设置知识点（先删除旧关联，再创建新关联）
     *
     * @param assignmentId 作业ID
     * @param kpIds 知识点ID列表
     * @return 结果
     */
    @Override
    @Transactional
    public int setAssignmentKnowledgePoints(Long assignmentId, Long[] kpIds)
    {
        // 先删除该作业的所有知识点关联
        assignmentKpMapper.deleteAssignmentKpByAssignmentId(assignmentId);
        
        // 如果没有新的知识点，直接返回
        if (kpIds == null || kpIds.length == 0)
        {
            return 0;
        }
        
        // 创建新的关联
        List<AssignmentKp> assignmentKpList = new ArrayList<>();
        for (int i = 0; i < kpIds.length; i++)
        {
            AssignmentKp assignmentKp = new AssignmentKp();
            assignmentKp.setAssignmentId(assignmentId);
            assignmentKp.setKpId(kpIds[i]);
            assignmentKp.setSequence(i + 1); // 按照数组顺序设置序号
            assignmentKp.setIsRequired(0); // 默认非必须
            assignmentKpList.add(assignmentKp);
        }
        
        return assignmentKpMapper.batchInsertAssignmentKp(assignmentKpList);
    }
}
