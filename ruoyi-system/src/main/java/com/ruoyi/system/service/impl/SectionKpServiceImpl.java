package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.SectionKp;
import com.ruoyi.system.mapper.SectionKpMapper;
import com.ruoyi.system.service.ISectionKpService;

/**
 * 小节与知识点关联 服务层实现
 *
 * @author ruoyi
 */
@Service
public class SectionKpServiceImpl implements ISectionKpService
{
    @Autowired
    private SectionKpMapper sectionKpMapper;

    /**
     * 查询小节知识点关联信息
     *
     * @param id 关联ID
     * @return 关联信息
     */
    @Override
    public SectionKp selectSectionKpById(Long id)
    {
        return sectionKpMapper.selectSectionKpById(id);
    }

    /**
     * 查询小节知识点关联列表
     *
     * @param sectionKp 关联信息
     * @return 关联集合
     */
    @Override
    public List<SectionKp> selectSectionKpList(SectionKp sectionKp)
    {
        return sectionKpMapper.selectSectionKpList(sectionKp);
    }

    /**
     * 根据小节ID查询关联的知识点列表（含知识点详情）
     *
     * @param sectionId 小节ID
     * @return 关联集合
     */
    @Override
    public List<SectionKp> selectSectionKpListBySectionId(Long sectionId)
    {
        return sectionKpMapper.selectSectionKpListBySectionId(sectionId);
    }

    /**
     * 根据知识点ID查询关联的小节列表
     *
     * @param kpId 知识点ID
     * @return 关联集合
     */
    @Override
    public List<SectionKp> selectSectionKpListByKpId(Long kpId)
    {
        return sectionKpMapper.selectSectionKpListByKpId(kpId);
    }

    /**
     * 新增小节知识点关联
     *
     * @param sectionKp 关联信息
     * @return 结果
     */
    @Override
    public int insertSectionKp(SectionKp sectionKp)
    {
        // 检查是否已存在关联
        SectionKp exists = sectionKpMapper.checkSectionKpExists(sectionKp.getSectionId(), sectionKp.getKpId());
        if (exists != null)
        {
            return 0; // 已存在，不重复添加
        }
        return sectionKpMapper.insertSectionKp(sectionKp);
    }

    /**
     * 批量新增小节知识点关联（用于AI生成）
     *
     * @param sectionKpList 关联列表
     * @return 结果
     */
    @Override
    @Transactional
    public int batchInsertSectionKp(List<SectionKp> sectionKpList)
    {
        if (sectionKpList == null || sectionKpList.isEmpty())
        {
            return 0;
        }
        
        // 过滤掉已存在的关联
        List<SectionKp> toInsert = new ArrayList<>();
        for (SectionKp sk : sectionKpList)
        {
            SectionKp exists = sectionKpMapper.checkSectionKpExists(sk.getSectionId(), sk.getKpId());
            if (exists == null)
            {
                toInsert.add(sk);
            }
        }
        
        if (toInsert.isEmpty())
        {
            return 0;
        }
        
        return sectionKpMapper.batchInsertSectionKp(toInsert);
    }

    /**
     * 删除小节知识点关联
     *
     * @param id 关联ID
     * @return 结果
     */
    @Override
    public int deleteSectionKpById(Long id)
    {
        return sectionKpMapper.deleteSectionKpById(id);
    }

    /**
     * 批量删除小节知识点关联
     *
     * @param ids 需要删除的关联ID
     * @return 结果
     */
    @Override
    public int deleteSectionKpByIds(Long[] ids)
    {
        return sectionKpMapper.deleteSectionKpByIds(ids);
    }

    /**
     * 删除小节的所有知识点关联
     *
     * @param sectionId 小节ID
     * @return 结果
     */
    @Override
    public int deleteSectionKpBySectionId(Long sectionId)
    {
        return sectionKpMapper.deleteSectionKpBySectionId(sectionId);
    }

    /**
     * 为小节批量设置知识点（先删除旧关联，再创建新关联）
     *
     * @param sectionId 小节ID
     * @param kpIds 知识点ID列表
     * @return 结果
     */
    @Override
    @Transactional
    public int setSectionKnowledgePoints(Long sectionId, Long[] kpIds)
    {
        // 先删除该小节的所有知识点关联
        sectionKpMapper.deleteSectionKpBySectionId(sectionId);
        
        // 如果没有新的知识点，返回1表示成功（已删除旧关联）
        if (kpIds == null || kpIds.length == 0)
        {
            return 1;
        }
        
        // 创建新的关联
        List<SectionKp> sectionKpList = new ArrayList<>();
        for (int i = 0; i < kpIds.length; i++)
        {
            SectionKp sectionKp = new SectionKp();
            sectionKp.setSectionId(sectionId);
            sectionKp.setKpId(kpIds[i]);
            sectionKp.setSequence(i + 1); // 按照数组顺序设置序号
            sectionKpList.add(sectionKp);
        }
        
        return sectionKpMapper.batchInsertSectionKp(sectionKpList);
    }
}
