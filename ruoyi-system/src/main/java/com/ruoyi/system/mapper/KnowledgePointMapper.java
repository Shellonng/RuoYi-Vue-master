package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.KnowledgePoint;
import org.apache.ibatis.annotations.Param;

/**
 * 知识点表 数据层
 *
 * @author ruoyi
 */
public interface KnowledgePointMapper
{
    /**
     * 查询知识点信息
     *
     * @param id 知识点ID
     * @return 知识点信息
     */
    public KnowledgePoint selectKnowledgePointById(Long id);

    /**
     * 查询知识点列表
     *
     * @param knowledgePoint 知识点信息
     * @return 知识点集合
     */
    public List<KnowledgePoint> selectKnowledgePointList(KnowledgePoint knowledgePoint);

    /**
     * 根据课程ID查询知识点列表
     *
     * @param courseId 课程ID
     * @return 知识点集合
     */
    public List<KnowledgePoint> selectKnowledgePointListByCourseId(@Param("courseId") Long courseId);

    /**
     * 根据课程ID和难度级别查询知识点列表
     *
     * @param courseId 课程ID
     * @param level 难度级别
     * @return 知识点集合
     */
    public List<KnowledgePoint> selectKnowledgePointListByCourseIdAndLevel(@Param("courseId") Long courseId, @Param("level") String level);

    /**
     * 新增知识点
     *
     * @param knowledgePoint 知识点信息
     * @return 结果
     */
    public int insertKnowledgePoint(KnowledgePoint knowledgePoint);

    /**
     * 修改知识点
     *
     * @param knowledgePoint 知识点信息
     * @return 结果
     */
    public int updateKnowledgePoint(KnowledgePoint knowledgePoint);

    /**
     * 删除知识点（软删除）
     *
     * @param id 知识点ID
     * @return 结果
     */
    public int deleteKnowledgePointById(Long id);

    /**
     * 批量删除知识点（软删除）
     *
     * @param ids 需要删除的知识点ID
     * @return 结果
     */
    public int deleteKnowledgePointByIds(Long[] ids);

    /**
     * 批量插入知识点（用于AI生成）
     *
     * @param knowledgePointList 知识点列表
     * @return 结果
     */
    public int batchInsertKnowledgePoints(List<KnowledgePoint> knowledgePointList);
}
