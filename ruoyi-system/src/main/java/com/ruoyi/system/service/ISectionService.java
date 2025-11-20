package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.Section;

/**
 * 课程小节 服务层
 *
 * @author ruoyi
 */
public interface ISectionService
{
    /**
     * 查询小节信息
     *
     * @param id 小节ID
     * @return 小节信息
     */
    public Section selectSectionById(Long id);

    /**
     * 查询小节列表
     *
     * @param section 小节信息
     * @return 小节集合
     */
    public List<Section> selectSectionList(Section section);

    /**
     * 根据章节ID查询小节列表
     *
     * @param chapterId 章节ID
     * @return 小节集合
     */
    public List<Section> selectSectionListByChapterId(Long chapterId);

    /**
     * 新增小节
     *
     * @param section 小节信息
     * @return 结果
     */
    public int insertSection(Section section);

    /**
     * 修改小节
     *
     * @param section 小节信息
     * @return 结果
     */
    public int updateSection(Section section);

    /**
     * 删除小节信息
     *
     * @param id 小节ID
     * @return 结果
     */
    public int deleteSectionById(Long id);

    /**
     * 批量删除小节信息
     *
     * @param ids 需要删除的小节ID
     * @return 结果
     */
    public int deleteSectionByIds(Long[] ids);
}
