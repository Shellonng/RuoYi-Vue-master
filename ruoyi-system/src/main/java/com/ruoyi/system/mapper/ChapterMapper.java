package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Chapter;
import org.apache.ibatis.annotations.Param;

/**
 * 课程章节表 数据层
 *
 * @author ruoyi
 */
public interface ChapterMapper
{
    /**
     * 查询章节信息
     *
     * @param id 章节ID
     * @return 章节信息
     */
    public Chapter selectChapterById(Long id);

    /**
     * 查询章节列表
     *
     * @param chapter 章节信息
     * @return 章节集合
     */
    public List<Chapter> selectChapterList(Chapter chapter);

    /**
     * 根据课程ID查询章节列表
     *
     * @param courseId 课程ID
     * @return 章节集合
     */
    public List<Chapter> selectChapterListByCourseId(@Param("courseId") Long courseId);

    /**
     * 新增章节
     *
     * @param chapter 章节信息
     * @return 结果
     */
    public int insertChapter(Chapter chapter);

    /**
     * 修改章节
     *
     * @param chapter 章节信息
     * @return 结果
     */
    public int updateChapter(Chapter chapter);

    /**
     * 删除章节
     *
     * @param id 章节ID
     * @return 结果
     */
    public int deleteChapterById(Long id);

    /**
     * 批量删除章节
     *
     * @param ids 需要删除的数据ID数组
     * @return 结果
     */
    public int deleteChapterByIds(Long[] ids);
}
