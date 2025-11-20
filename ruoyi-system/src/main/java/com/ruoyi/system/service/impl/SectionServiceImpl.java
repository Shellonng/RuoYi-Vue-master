package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.Section;
import com.ruoyi.system.mapper.SectionMapper;
import com.ruoyi.system.service.ISectionService;

/**
 * 课程小节 服务层实现
 *
 * @author ruoyi
 */
@Service
public class SectionServiceImpl implements ISectionService
{
    @Autowired
    private SectionMapper sectionMapper;

    /**
     * 查询小节信息
     *
     * @param id 小节ID
     * @return 小节信息
     */
    @Override
    public Section selectSectionById(Long id)
    {
        return sectionMapper.selectSectionById(id);
    }

    /**
     * 查询小节列表
     *
     * @param section 小节信息
     * @return 小节集合
     */
    @Override
    public List<Section> selectSectionList(Section section)
    {
        return sectionMapper.selectSectionList(section);
    }

    /**
     * 根据章节ID查询小节列表
     *
     * @param chapterId 章节ID
     * @return 小节集合
     */
    @Override
    public List<Section> selectSectionListByChapterId(Long chapterId)
    {
        return sectionMapper.selectSectionListByChapterId(chapterId);
    }

    /**
     * 新增小节
     *
     * @param section 小节信息
     * @return 结果
     */
    @Override
    public int insertSection(Section section)
    {
        return sectionMapper.insertSection(section);
    }

    /**
     * 修改小节
     *
     * @param section 小节信息
     * @return 结果
     */
    @Override
    public int updateSection(Section section)
    {
        return sectionMapper.updateSection(section);
    }

    /**
     * 删除小节信息
     *
     * @param id 小节ID
     * @return 结果
     */
    @Override
    public int deleteSectionById(Long id)
    {
        return sectionMapper.deleteSectionById(id);
    }

    /**
     * 批量删除小节信息
     *
     * @param ids 需要删除的小节ID
     * @return 结果
     */
    @Override
    public int deleteSectionByIds(Long[] ids)
    {
        return sectionMapper.deleteSectionByIds(ids);
    }
}
