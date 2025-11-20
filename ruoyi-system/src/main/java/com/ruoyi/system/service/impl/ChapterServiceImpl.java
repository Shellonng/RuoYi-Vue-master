package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.Chapter;
import com.ruoyi.system.mapper.ChapterMapper;
import com.ruoyi.system.service.IChapterService;

/**
 * 课程章节 服务层实现
 *
 * @author ruoyi
 */
@Service
public class ChapterServiceImpl implements IChapterService
{
    @Autowired
    private ChapterMapper chapterMapper;

    /**
     * 查询章节信息
     *
     * @param id 章节ID
     * @return 章节信息
     */
    @Override
    public Chapter selectChapterById(Long id)
    {
        return chapterMapper.selectChapterById(id);
    }

    /**
     * 查询章节列表
     *
     * @param chapter 章节信息
     * @return 章节集合
     */
    @Override
    public List<Chapter> selectChapterList(Chapter chapter)
    {
        return chapterMapper.selectChapterList(chapter);
    }

    /**
     * 根据课程ID查询章节列表
     *
     * @param courseId 课程ID
     * @return 章节集合
     */
    @Override
    public List<Chapter> selectChapterListByCourseId(Long courseId)
    {
        return chapterMapper.selectChapterListByCourseId(courseId);
    }

    /**
     * 新增章节
     *
     * @param chapter 章节信息
     * @return 结果
     */
    @Override
    public int insertChapter(Chapter chapter)
    {
        return chapterMapper.insertChapter(chapter);
    }

    /**
     * 修改章节
     *
     * @param chapter 章节信息
     * @return 结果
     */
    @Override
    public int updateChapter(Chapter chapter)
    {
        return chapterMapper.updateChapter(chapter);
    }

    /**
     * 删除章节信息
     *
     * @param id 章节ID
     * @return 结果
     */
    @Override
    public int deleteChapterById(Long id)
    {
        return chapterMapper.deleteChapterById(id);
    }

    /**
     * 批量删除章节信息
     *
     * @param ids 需要删除的章节ID
     * @return 结果
     */
    @Override
    public int deleteChapterByIds(Long[] ids)
    {
        return chapterMapper.deleteChapterByIds(ids);
    }
}
