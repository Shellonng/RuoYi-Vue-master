package com.ruoyi.system.domain.dto;

import java.util.List;

/**
 * AI生成的课程结构DTO
 *
 * @author ruoyi
 */
public class CourseStructureDTO
{
    /** 章节列表 */
    private List<ChapterDTO> chapters;

    public List<ChapterDTO> getChapters() {
        return chapters;
    }

    public void setChapters(List<ChapterDTO> chapters) {
        this.chapters = chapters;
    }

    /**
     * 章节DTO
     */
    public static class ChapterDTO
    {
        /** 章节名称 */
        private String title;

        /** 章节描述 */
        private String description;

        /** 排序顺序 */
        private Integer sortOrder;

        /** 小节列表 */
        private List<SectionDTO> sections;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public List<SectionDTO> getSections() {
            return sections;
        }

        public void setSections(List<SectionDTO> sections) {
            this.sections = sections;
        }
    }

    /**
     * 小节DTO
     */
    public static class SectionDTO
    {
        /** 小节名称 */
        private String title;

        /** 小节描述 */
        private String description;

        /** 排序顺序 */
        private Integer sortOrder;

        /** 知识点列表 */
        private List<KnowledgePointDTO> knowledgePoints;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public List<KnowledgePointDTO> getKnowledgePoints() {
            return knowledgePoints;
        }

        public void setKnowledgePoints(List<KnowledgePointDTO> knowledgePoints) {
            this.knowledgePoints = knowledgePoints;
        }
    }

    /**
     * 知识点DTO
     */
    public static class KnowledgePointDTO
    {
        /** 知识点名称 */
        private String title;

        /** 知识点描述 */
        private String description;

        /** 难度级别 BASIC-基础, INTERMEDIATE-中级, ADVANCED-高级 */
        private String level;

        /** 在小节中的顺序 */
        private Integer sequence;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public Integer getSequence() {
            return sequence;
        }

        public void setSequence(Integer sequence) {
            this.sequence = sequence;
        }
    }
}
