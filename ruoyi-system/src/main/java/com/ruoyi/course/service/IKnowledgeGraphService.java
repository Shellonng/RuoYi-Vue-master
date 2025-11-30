package com.ruoyi.course.service;

import java.util.List;
import com.ruoyi.course.domain.KnowledgeGraph;

/**
 * 知识图谱Service接口
 * 
 * @author ruoyi
 * @date 2025-11-30
 */
public interface IKnowledgeGraphService 
{
    /**
     * 查询知识图谱
     * 
     * @param id 知识图谱主键
     * @return 知识图谱
     */
    public KnowledgeGraph selectKnowledgeGraphById(Long id);

    /**
     * 查询知识图谱列表
     * 
     * @param knowledgeGraph 知识图谱
     * @return 知识图谱集合
     */
    public List<KnowledgeGraph> selectKnowledgeGraphList(KnowledgeGraph knowledgeGraph);

    /**
     * 根据课程ID查询知识图谱
     * 
     * @param courseId 课程ID
     * @param graphType 图谱类型
     * @return 知识图谱
     */
    public KnowledgeGraph selectKnowledgeGraphByCourseId(Long courseId, String graphType);

    /**
     * 新增知识图谱
     * 
     * @param knowledgeGraph 知识图谱
     * @return 结果
     */
    public int insertKnowledgeGraph(KnowledgeGraph knowledgeGraph);

    /**
     * 修改知识图谱
     * 
     * @param knowledgeGraph 知识图谱
     * @return 结果
     */
    public int updateKnowledgeGraph(KnowledgeGraph knowledgeGraph);

    /**
     * 保存或更新知识图谱（智能判断）
     * 
     * @param knowledgeGraph 知识图谱
     * @return 结果
     */
    public int saveOrUpdateKnowledgeGraph(KnowledgeGraph knowledgeGraph);

    /**
     * 批量删除知识图谱
     * 
     * @param ids 需要删除的知识图谱主键集合
     * @return 结果
     */
    public int deleteKnowledgeGraphByIds(Long[] ids);

    /**
     * 删除知识图谱信息
     * 
     * @param id 知识图谱主键
     * @return 结果
     */
    public int deleteKnowledgeGraphById(Long id);
}
