package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Section;
import org.apache.ibatis.annotations.Param;

/**
 * 课程小节表 数据层
 *
 * @author ruoyi
 */
public interface SectionMapper
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
    public List<Section> selectSectionListByChapterId(@Param("chapterId") Long chapterId);

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
     * 删除小节
     *
     * @param id 小节ID
     * @return 结果
     */
    public int deleteSectionById(Long id);

    /**
     * 批量删除小节
     *
     * @param ids 需要删除的数据ID数组
     * @return 结果
     */
    public int deleteSectionByIds(Long[] ids);

    /**
     * 根据视频URL查询小节
     *
     * @param videoUrl 视频URL
     * @return 小节信息
     */
    public Section selectSectionByVideoUrl(@Param("videoUrl") String videoUrl);
}
