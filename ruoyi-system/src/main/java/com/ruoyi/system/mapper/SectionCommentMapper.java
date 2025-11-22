package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SectionComment;
import org.apache.ibatis.annotations.Param;

/**
 * 小节评论Mapper接口
 * 
 * @author ruoyi
 */
public interface SectionCommentMapper 
{
    /**
     * 查询小节评论
     * 
     * @param id 小节评论主键
     * @return 小节评论
     */
    public SectionComment selectSectionCommentById(Long id);

    /**
     * 查询小节评论列表
     * 
     * @param sectionComment 小节评论
     * @return 小节评论集合
     */
    public List<SectionComment> selectSectionCommentList(SectionComment sectionComment);

    /**
     * 根据小节ID查询一级评论列表（含用户信息）
     * 
     * @param sectionId 小节ID
     * @return 一级评论集合
     */
    public List<SectionComment> selectTopCommentsBySectionId(@Param("sectionId") Long sectionId);

    /**
     * 根据父评论ID查询回复列表
     * 
     * @param parentId 父评论ID
     * @return 回复集合
     */
    public List<SectionComment> selectRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 统计小节的评论数量
     * 
     * @param sectionId 小节ID
     * @return 评论数量
     */
    public int countCommentsBySectionId(@Param("sectionId") Long sectionId);

    /**
     * 统计评论的回复数量
     * 
     * @param parentId 父评论ID
     * @return 回复数量
     */
    public int countRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 新增小节评论
     * 
     * @param sectionComment 小节评论
     * @return 结果
     */
    public int insertSectionComment(SectionComment sectionComment);

    /**
     * 修改小节评论
     * 
     * @param sectionComment 小节评论
     * @return 结果
     */
    public int updateSectionComment(SectionComment sectionComment);

    /**
     * 删除小节评论
     * 
     * @param id 小节评论主键
     * @return 结果
     */
    public int deleteSectionCommentById(Long id);

    /**
     * 批量删除小节评论
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSectionCommentByIds(Long[] ids);

    /**
     * 根据父评论ID删除所有回复
     * 
     * @param parentId 父评论ID
     * @return 结果
     */
    public int deleteSectionCommentByParentId(@Param("parentId") Long parentId);
}
