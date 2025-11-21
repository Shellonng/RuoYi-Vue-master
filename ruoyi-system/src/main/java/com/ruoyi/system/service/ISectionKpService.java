package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SectionKp;

/**
 * 小节与知识点关联 服务层
 *
 * @author ruoyi
 */
public interface ISectionKpService
{
    /**
     * 查询小节知识点关联信息
     *
     * @param id 关联ID
     * @return 关联信息
     */
    public SectionKp selectSectionKpById(Long id);

    /**
     * 查询小节知识点关联列表
     *
     * @param sectionKp 关联信息
     * @return 关联集合
     */
    public List<SectionKp> selectSectionKpList(SectionKp sectionKp);

    /**
     * 根据小节ID查询关联的知识点列表（含知识点详情）
     *
     * @param sectionId 小节ID
     * @return 关联集合
     */
    public List<SectionKp> selectSectionKpListBySectionId(Long sectionId);

    /**
     * 根据知识点ID查询关联的小节列表
     *
     * @param kpId 知识点ID
     * @return 关联集合
     */
    public List<SectionKp> selectSectionKpListByKpId(Long kpId);

    /**
     * 新增小节知识点关联
     *
     * @param sectionKp 关联信息
     * @return 结果
     */
    public int insertSectionKp(SectionKp sectionKp);

    /**
     * 批量新增小节知识点关联（用于AI生成）
     *
     * @param sectionKpList 关联列表
     * @return 结果
     */
    public int batchInsertSectionKp(List<SectionKp> sectionKpList);

    /**
     * 删除小节知识点关联
     *
     * @param id 关联ID
     * @return 结果
     */
    public int deleteSectionKpById(Long id);

    /**
     * 批量删除小节知识点关联
     *
     * @param ids 需要删除的关联ID
     * @return 结果
     */
    public int deleteSectionKpByIds(Long[] ids);

    /**
     * 删除小节的所有知识点关联
     *
     * @param sectionId 小节ID
     * @return 结果
     */
    public int deleteSectionKpBySectionId(Long sectionId);

    /**
     * 为小节批量设置知识点（先删除旧关联，再创建新关联）
     *
     * @param sectionId 小节ID
     * @param kpIds 知识点ID列表
     * @return 结果
     */
    public int setSectionKnowledgePoints(Long sectionId, Long[] kpIds);
}
