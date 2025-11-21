package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.KnowledgePoint;
import com.ruoyi.system.mapper.KnowledgePointMapper;
import com.ruoyi.system.service.IKnowledgePointService;

/**
 * 知识点 服务层实现
 *
 * @author ruoyi
 */
@Service
public class KnowledgePointServiceImpl implements IKnowledgePointService
{
    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    /**
     * 查询知识点信息
     *
     * @param id 知识点ID
     * @return 知识点信息
     */
    @Override
    public KnowledgePoint selectKnowledgePointById(Long id)
    {
        return knowledgePointMapper.selectKnowledgePointById(id);
    }

    /**
     * 查询知识点列表
     *
     * @param knowledgePoint 知识点信息
     * @return 知识点集合
     */
    @Override
    public List<KnowledgePoint> selectKnowledgePointList(KnowledgePoint knowledgePoint)
    {
        return knowledgePointMapper.selectKnowledgePointList(knowledgePoint);
    }

    /**
     * 根据课程ID查询知识点列表
     *
     * @param courseId 课程ID
     * @return 知识点集合
     */
    @Override
    public List<KnowledgePoint> selectKnowledgePointListByCourseId(Long courseId)
    {
        return knowledgePointMapper.selectKnowledgePointListByCourseId(courseId);
    }

    /**
     * 根据课程ID和难度级别查询知识点列表
     *
     * @param courseId 课程ID
     * @param level 难度级别
     * @return 知识点集合
     */
    @Override
    public List<KnowledgePoint> selectKnowledgePointListByCourseIdAndLevel(Long courseId, String level)
    {
        return knowledgePointMapper.selectKnowledgePointListByCourseIdAndLevel(courseId, level);
    }

    /**
     * 新增知识点
     *
     * @param knowledgePoint 知识点信息
     * @return 结果
     */
    @Override
    public int insertKnowledgePoint(KnowledgePoint knowledgePoint)
    {
        return knowledgePointMapper.insertKnowledgePoint(knowledgePoint);
    }

    /**
     * 修改知识点
     *
     * @param knowledgePoint 知识点信息
     * @return 结果
     */
    @Override
    public int updateKnowledgePoint(KnowledgePoint knowledgePoint)
    {
        return knowledgePointMapper.updateKnowledgePoint(knowledgePoint);
    }

    /**
     * 删除知识点（软删除）
     *
     * @param id 知识点ID
     * @return 结果
     */
    @Override
    public int deleteKnowledgePointById(Long id)
    {
        return knowledgePointMapper.deleteKnowledgePointById(id);
    }

    /**
     * 批量删除知识点（软删除）
     *
     * @param ids 需要删除的知识点ID
     * @return 结果
     */
    @Override
    public int deleteKnowledgePointByIds(Long[] ids)
    {
        return knowledgePointMapper.deleteKnowledgePointByIds(ids);
    }

    /**
     * 批量插入知识点（用于AI生成）
     *
     * @param knowledgePointList 知识点列表
     * @return 结果
     */
    @Override
    public int batchInsertKnowledgePoints(List<KnowledgePoint> knowledgePointList)
    {
        if (knowledgePointList == null || knowledgePointList.isEmpty())
        {
            return 0;
        }
        return knowledgePointMapper.batchInsertKnowledgePoints(knowledgePointList);
    }
}
