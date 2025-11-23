package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.KpRelation;

/**
 * 知识点关系Mapper接口
 *
 * @author ruoyi
 */
public interface KpRelationMapper
{
    /**
     * 查询知识点关系列表
     *
     * @param kpRelation 知识点关系
     * @return 知识点关系集合
     */
    public List<KpRelation> selectKpRelationList(KpRelation kpRelation);

    /**
     * 根据课程ID查询所有知识点关系
     *
     * @param courseId 课程ID
     * @return 知识点关系集合
     */
    public List<KpRelation> selectKpRelationListByCourseId(Long courseId);

    /**
     * 新增知识点关系
     *
     * @param kpRelation 知识点关系
     * @return 结果
     */
    public int insertKpRelation(KpRelation kpRelation);

    /**
     * 修改知识点关系
     *
     * @param kpRelation 知识点关系
     * @return 结果
     */
    public int updateKpRelation(KpRelation kpRelation);

    /**
     * 批量新增知识点关系
     *
     * @param kpRelations 知识点关系列表
     * @return 结果
     */
    public int batchInsertKpRelations(List<KpRelation> kpRelations);

    /**
     * 删除知识点关系
     *
     * @param id 知识点关系主键
     * @return 结果
     */
    public int deleteKpRelationById(Long id);

    /**
     * 根据课程ID删除所有知识点关系
     *
     * @param courseId 课程ID
     * @return 结果
     */
    public int deleteKpRelationsByCourseId(Long courseId);
}
