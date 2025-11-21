package com.ruoyi.system.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.Chapter;
import com.ruoyi.system.domain.KnowledgePoint;
import com.ruoyi.system.domain.Section;
import com.ruoyi.system.domain.SectionKp;
import com.ruoyi.system.domain.dto.CourseStructureDTO;
import com.ruoyi.system.mapper.ChapterMapper;
import com.ruoyi.system.mapper.KnowledgePointMapper;
import com.ruoyi.system.mapper.SectionMapper;
import com.ruoyi.system.mapper.SectionKpMapper;

/**
 * 课程生成服务 - 将AI生成的结构保存到数据库
 *
 * @author ruoyi
 */
@Service
public class CourseGenerationService
{
    private static final Logger log = LoggerFactory.getLogger(CourseGenerationService.class);

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private SectionMapper sectionMapper;

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Autowired
    private SectionKpMapper sectionKpMapper;

    /**
     * 保存AI生成的课程结构
     *
     * @param courseId 课程ID
     * @param structure AI生成的课程结构
     * @param creatorUserId 创建者用户ID
     * @return 保存结果统计
     */
    @Transactional(rollbackFor = Exception.class)
    public String saveCourseStructure(Long courseId, CourseStructureDTO structure, Long creatorUserId)
    {
        log.info("开始保存课程结构，课程ID：{}，章节数：{}", courseId, structure.getChapters().size());
        
        // 先删除该课程的所有章节和小节（级联会自动删除 section_kp 关联，但保留知识点）
        try {
            // 查询该课程的所有章节
            Chapter queryChapter = new Chapter();
            queryChapter.setCourseId(courseId);
            List<Chapter> existingChapters = chapterMapper.selectChapterList(queryChapter);
            
            if (!existingChapters.isEmpty()) {
                log.info("删除课程 {} 的旧结构，章节数：{}", courseId, existingChapters.size());
                
                // 收集所有需要删除的知识点ID（通过小节-知识点关联表）
                List<Long> knowledgePointIdsToDelete = new ArrayList<>();
                
                // 遍历删除每个章节（会级联删除小节和 section_kp）
                for (Chapter chapter : existingChapters) {
                    // 先查询该章节下的所有小节
                    Section querySection = new Section();
                    querySection.setChapterId(chapter.getId());
                    List<Section> sections = sectionMapper.selectSectionList(querySection);
                    
                    if (!sections.isEmpty()) {
                        // 遍历每个小节，查询其关联的知识点
                        for (Section section : sections) {
                            // 查询该小节关联的所有知识点ID
                            List<SectionKp> sectionKps = sectionKpMapper.selectSectionKpListBySection(section.getId());
                            for (SectionKp sectionKp : sectionKps) {
                                Long kpId = sectionKp.getKpId();
                                // 检查该知识点是否只被当前小节引用（即是否为孤立知识点）
                                // 查询该知识点被多少个小节引用
                                int refCount = sectionKpMapper.countSectionsByKpId(kpId);
                                if (refCount == 1) {
                                    // 只被当前小节引用，可以删除
                                    if (!knowledgePointIdsToDelete.contains(kpId)) {
                                        knowledgePointIdsToDelete.add(kpId);
                                    }
                                }
                            }
                        }
                        
                        // 删除所有小节（会级联删除 section_kp）
                        Long[] sectionIds = sections.stream().map(Section::getId).toArray(Long[]::new);
                        sectionMapper.deleteSectionByIds(sectionIds);
                        log.debug("删除章节 {} 下的 {} 个小节", chapter.getId(), sectionIds.length);
                    }
                    
                    // 删除章节
                    chapterMapper.deleteChapterById(chapter.getId());
                }
                
                // 删除收集到的孤立知识点
                if (!knowledgePointIdsToDelete.isEmpty()) {
                    Long[] kpIdsArray = knowledgePointIdsToDelete.toArray(new Long[0]);
                    knowledgePointMapper.deleteKnowledgePointByIds(kpIdsArray);
                    log.info("删除了 {} 个孤立的知识点", knowledgePointIdsToDelete.size());
                }
                
                log.info("旧结构删除完成");
            }
        } catch (Exception e) {
            log.error("删除旧课程结构失败", e);
            throw new ServiceException("删除旧课程结构失败：" + e.getMessage());
        }
        
        int chapterCount = 0;
        int sectionCount = 0;
        int knowledgePointCount = 0;
        int relationCount = 0;

        try
        {
            // 遍历章节
            for (CourseStructureDTO.ChapterDTO chapterDTO : structure.getChapters())
            {
                // 创建章节
                Chapter chapter = new Chapter();
                chapter.setCourseId(courseId);
                chapter.setTitle(chapterDTO.getTitle());
                chapter.setDescription(chapterDTO.getDescription());
                chapter.setSortOrder(chapterDTO.getSortOrder());
                
                chapterMapper.insertChapter(chapter);
                chapterCount++;
                log.debug("插入章节：{}，ID：{}", chapter.getTitle(), chapter.getId());

                // 遍历小节
                if (chapterDTO.getSections() != null)
                {
                    for (CourseStructureDTO.SectionDTO sectionDTO : chapterDTO.getSections())
                    {
                        // 创建小节
                        Section section = new Section();
                        section.setChapterId(chapter.getId());
                        section.setTitle(sectionDTO.getTitle());
                        section.setDescription(sectionDTO.getDescription());
                        section.setSortOrder(sectionDTO.getSortOrder());
                        
                        sectionMapper.insertSection(section);
                        sectionCount++;
                        log.debug("插入小节：{}，ID：{}", section.getTitle(), section.getId());

                        // 遍历知识点
                        if (sectionDTO.getKnowledgePoints() != null)
                        {
                            List<SectionKp> sectionKpList = new ArrayList<>();
                            
                            for (CourseStructureDTO.KnowledgePointDTO kpDTO : sectionDTO.getKnowledgePoints())
                            {
                                // 创建知识点
                                KnowledgePoint kp = new KnowledgePoint();
                                kp.setCourseId(courseId);
                                kp.setTitle(kpDTO.getTitle());
                                kp.setDescription(kpDTO.getDescription());
                                kp.setLevel(kpDTO.getLevel());
                                kp.setCreatorUserId(creatorUserId);
                                
                                knowledgePointMapper.insertKnowledgePoint(kp);
                                knowledgePointCount++;
                                log.debug("插入知识点：{}，ID：{}", kp.getTitle(), kp.getId());

                                // 创建小节-知识点关联
                                SectionKp sectionKp = new SectionKp();
                                sectionKp.setSectionId(section.getId());
                                sectionKp.setKpId(kp.getId());
                                sectionKp.setSequence(kpDTO.getSequence());
                                sectionKpList.add(sectionKp);
                            }

                            // 批量插入关联关系
                            if (!sectionKpList.isEmpty())
                            {
                                sectionKpMapper.batchInsertSectionKp(sectionKpList);
                                relationCount += sectionKpList.size();
                                log.debug("批量插入小节-知识点关联，数量：{}", sectionKpList.size());
                            }
                        }
                    }
                }
            }

            String result = String.format("成功生成课程结构：%d个章节，%d个小节，%d个知识点，%d个关联关系",
                chapterCount, sectionCount, knowledgePointCount, relationCount);
            log.info(result);
            return result;
        }
        catch (Exception e)
        {
            log.error("保存课程结构失败", e);
            throw new ServiceException("保存课程结构失败：" + e.getMessage());
        }
    }
}
