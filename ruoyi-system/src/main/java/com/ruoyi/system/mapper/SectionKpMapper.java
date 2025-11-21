package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SectionKp;
import org.apache.ibatis.annotations.Param;

/**
 * 小节与知识点关联表 数据层
 *
 * @author ruoyi
 */
public interface SectionKpMapper
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
    public List<SectionKp> selectSectionKpListBySectionId(@Param("sectionId") Long sectionId);

    /**
     * 根据知识点ID查询关联的小节列表
     *
     * @param kpId 知识点ID
     * @return 关联集合
     */
    public List<SectionKp> selectSectionKpListByKpId(@Param("kpId") Long kpId);

    /**
     * 检查小节和知识点是否已关联
     *
     * @param sectionId 小节ID
     * @param kpId 知识点ID
     * @return 关联信息，不存在返回null
     */
    public SectionKp checkSectionKpExists(@Param("sectionId") Long sectionId, @Param("kpId") Long kpId);

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
    public int deleteSectionKpBySectionId(@Param("sectionId") Long sectionId);

    /**
     * 删除知识点的所有小节关联
     *
     * @param kpId 知识点ID
     * @return 结果
     */
    public int deleteSectionKpByKpId(@Param("kpId") Long kpId);

    /**
     * 根据小节ID查询关联列表（用于删除时查找关联的知识点）
     *
     * @param sectionId 小节ID
     * @return 关联集合
     */
    public List<SectionKp> selectSectionKpListBySection(@Param("sectionId") Long sectionId);

    /**
     * 统计某个知识点被多少个小节引用
     *
     * @param kpId 知识点ID
     * @return 引用次数
     */
    public int countSectionsByKpId(@Param("kpId") Long kpId);
}
