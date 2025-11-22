package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SectionCommentMapper;
import com.ruoyi.system.domain.SectionComment;
import com.ruoyi.system.service.ISectionCommentService;

/**
 * 小节评论Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SectionCommentServiceImpl implements ISectionCommentService 
{
    @Autowired
    private SectionCommentMapper sectionCommentMapper;

    /**
     * 查询小节评论
     * 
     * @param id 小节评论主键
     * @return 小节评论
     */
    @Override
    public SectionComment selectSectionCommentById(Long id)
    {
        return sectionCommentMapper.selectSectionCommentById(id);
    }

    /**
     * 查询小节评论列表
     * 
     * @param sectionComment 小节评论
     * @return 小节评论
     */
    @Override
    public List<SectionComment> selectSectionCommentList(SectionComment sectionComment)
    {
        return sectionCommentMapper.selectSectionCommentList(sectionComment);
    }

    /**
     * 根据小节ID查询评论列表（树形结构，含回复）
     * 
     * @param sectionId 小节ID
     * @return 评论树形列表
     */
    @Override
    public List<SectionComment> selectCommentTreeBySectionId(Long sectionId)
    {
        // 1. 查询所有一级评论
        List<SectionComment> topComments = sectionCommentMapper.selectTopCommentsBySectionId(sectionId);
        
        // 2. 为每个一级评论加载回复
        for (SectionComment comment : topComments)
        {
            List<SectionComment> replies = sectionCommentMapper.selectRepliesByParentId(comment.getId());
            comment.setReplies(replies);
            
            // 如果没有从数据库查询到回复数，手动设置
            if (comment.getReplyCount() == null)
            {
                comment.setReplyCount(replies.size());
            }
        }
        
        return topComments;
    }

    /**
     * 新增小节评论
     * 
     * @param sectionComment 小节评论
     * @return 结果
     */
    @Override
    public int insertSectionComment(SectionComment sectionComment)
    {
        sectionComment.setCreateTime(new Date());
        sectionComment.setUpdateTime(new Date());
        sectionComment.setIsDeleted(0);
        return sectionCommentMapper.insertSectionComment(sectionComment);
    }

    /**
     * 修改小节评论
     * 
     * @param sectionComment 小节评论
     * @return 结果
     */
    @Override
    public int updateSectionComment(SectionComment sectionComment)
    {
        sectionComment.setUpdateTime(new Date());
        return sectionCommentMapper.updateSectionComment(sectionComment);
    }

    /**
     * 批量删除小节评论
     * 
     * @param ids 需要删除的小节评论主键
     * @return 结果
     */
    @Override
    public int deleteSectionCommentByIds(Long[] ids)
    {
        int result = 0;
        for (Long id : ids)
        {
            result += deleteSectionCommentById(id);
        }
        return result;
    }

    /**
     * 删除小节评论信息（级联删除所有回复）
     * 
     * @param id 小节评论主键
     * @return 结果
     */
    @Override
    public int deleteSectionCommentById(Long id)
    {
        // 1. 先删除所有回复
        sectionCommentMapper.deleteSectionCommentByParentId(id);
        
        // 2. 再删除评论本身
        return sectionCommentMapper.deleteSectionCommentById(id);
    }
}
