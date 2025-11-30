package com.ruoyi.course.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.course.mapper.KnowledgeGraphMapper;
import com.ruoyi.course.domain.KnowledgeGraph;
import com.ruoyi.course.service.IKnowledgeGraphService;

/**
 * 知识图谱Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-30
 */
@Service
public class KnowledgeGraphServiceImpl implements IKnowledgeGraphService 
{
    @Autowired
    private KnowledgeGraphMapper knowledgeGraphMapper;

    /**
     * 查询知识图谱
     * 
     * @param id 知识图谱主键
     * @return 知识图谱
     */
    @Override
    public KnowledgeGraph selectKnowledgeGraphById(Long id)
    {
        return knowledgeGraphMapper.selectKnowledgeGraphById(id);
    }

    /**
     * 查询知识图谱列表
     * 
     * @param knowledgeGraph 知识图谱
     * @return 知识图谱
     */
    @Override
    public List<KnowledgeGraph> selectKnowledgeGraphList(KnowledgeGraph knowledgeGraph)
    {
        return knowledgeGraphMapper.selectKnowledgeGraphList(knowledgeGraph);
    }

    /**
     * 根据课程ID查询知识图谱
     * 
     * @param courseId 课程ID
     * @param graphType 图谱类型
     * @return 知识图谱
     */
    @Override
    public KnowledgeGraph selectKnowledgeGraphByCourseId(Long courseId, String graphType)
    {
        return knowledgeGraphMapper.selectKnowledgeGraphByCourseId(courseId, graphType);
    }

    /**
     * 新增知识图谱
     * 
     * @param knowledgeGraph 知识图谱
     * @return 结果
     */
    @Override
    public int insertKnowledgeGraph(KnowledgeGraph knowledgeGraph)
    {
        knowledgeGraph.setCreateTime(DateUtils.getNowDate());
        return knowledgeGraphMapper.insertKnowledgeGraph(knowledgeGraph);
    }

    /**
     * 修改知识图谱
     * 
     * @param knowledgeGraph 知识图谱
     * @return 结果
     */
    @Override
    public int updateKnowledgeGraph(KnowledgeGraph knowledgeGraph)
    {
        knowledgeGraph.setUpdateTime(DateUtils.getNowDate());
        return knowledgeGraphMapper.updateKnowledgeGraph(knowledgeGraph);
    }

    /**
     * 保存或更新知识图谱（智能判断）
     * 
     * @param knowledgeGraph 知识图谱
     * @return 结果
     */
    @Override
    public int saveOrUpdateKnowledgeGraph(KnowledgeGraph knowledgeGraph)
    {
        // 根据课程ID和图谱类型查询是否已存在
        KnowledgeGraph existingGraph = knowledgeGraphMapper.selectKnowledgeGraphByCourseId(
            knowledgeGraph.getCourseId(), 
            knowledgeGraph.getGraphType()
        );
        
        if (existingGraph != null) {
            // 已存在，执行更新
            knowledgeGraph.setId(existingGraph.getId());
            knowledgeGraph.setUpdateTime(DateUtils.getNowDate());
            return knowledgeGraphMapper.updateKnowledgeGraph(knowledgeGraph);
        } else {
            // 不存在，执行插入
            knowledgeGraph.setCreateTime(DateUtils.getNowDate());
            return knowledgeGraphMapper.insertKnowledgeGraph(knowledgeGraph);
        }
    }

    /**
     * 批量删除知识图谱
     * 
     * @param ids 需要删除的知识图谱主键
     * @return 结果
     */
    @Override
    public int deleteKnowledgeGraphByIds(Long[] ids)
    {
        return knowledgeGraphMapper.deleteKnowledgeGraphByIds(ids);
    }

    /**
     * 删除知识图谱信息
     * 
     * @param id 知识图谱主键
     * @return 结果
     */
    @Override
    public int deleteKnowledgeGraphById(Long id)
    {
        return knowledgeGraphMapper.deleteKnowledgeGraphById(id);
    }
}
