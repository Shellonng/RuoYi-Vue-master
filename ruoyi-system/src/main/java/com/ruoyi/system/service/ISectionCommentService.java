package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SectionComment;

/**
 * 小节评论Service接口
 * 
 * @author ruoyi
 */
public interface ISectionCommentService 
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
     * 根据小节ID查询评论列表（树形结构，含回复）
     * 
     * @param sectionId 小节ID
     * @return 评论树形列表
     */
    public List<SectionComment> selectCommentTreeBySectionId(Long sectionId);

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
     * 批量删除小节评论
     * 
     * @param ids 需要删除的小节评论主键集合
     * @return 结果
     */
    public int deleteSectionCommentByIds(Long[] ids);

    /**
     * 删除小节评论信息
     * 
     * @param id 小节评论主键
     * @return 结果
     */
    public int deleteSectionCommentById(Long id);
}
