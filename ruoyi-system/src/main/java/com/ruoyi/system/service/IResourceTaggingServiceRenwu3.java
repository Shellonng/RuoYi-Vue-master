package com.ruoyi.system.service;

import com.ruoyi.system.domain.KnowledgePointRenwu3;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 智能资源打标Service接口（任务3-资源智能打标）
 * 
 * @author ruoyi
 * @date 2025-01-18
 */
public interface IResourceTaggingServiceRenwu3
{
    /**
     * 分析文档并推荐知识点
     * 
     * @param file 文件
     * @param fileType 文件类型
     * @param courseId 课程ID
     * @param courseTitle 课程名称
     * @return 分析结果（包含推荐列表、提取的文本等）
     */
    Map<String, Object> analyzeAndRecommendKnowledgePoints(
        File file, 
        String fileType, 
        Long courseId, 
        String courseTitle
    );

    /**
     * 确认并保存资源与知识点的关联
     * 
     * @param resourceId 资源ID
     * @param kpIds 知识点ID列表
     * @return 操作结果
     */
    boolean confirmResourceKnowledgePoints(Long resourceId, List<Long> kpIds);

    /**
     * 获取资源已关联的知识点
     * 
     * @param resourceId 资源ID
     * @return 知识点列表
     */
    List<KnowledgePointRenwu3> getResourceKnowledgePoints(Long resourceId);
}
