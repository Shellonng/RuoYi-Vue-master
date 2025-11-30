package com.ruoyi.course.mapper;

import java.util.List;
import com.ruoyi.course.domain.KnowledgeGraph;
import org.apache.ibatis.annotations.Param;

/**
 * 知识图谱Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-30
 */
public interface KnowledgeGraphMapper 
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
    public KnowledgeGraph selectKnowledgeGraphByCourseId(@Param("courseId") Long courseId, @Param("graphType") String graphType);

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
     * 删除知识图谱
     * 
     * @param id 知识图谱主键
     * @return 结果
     */
    public int deleteKnowledgeGraphById(Long id);

    /**
     * 批量删除知识图谱
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteKnowledgeGraphByIds(Long[] ids);
}
