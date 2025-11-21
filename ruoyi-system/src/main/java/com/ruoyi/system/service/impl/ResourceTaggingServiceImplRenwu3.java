package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.ai.DeepSeekClientRenwu3;
import com.ruoyi.common.utils.ai.WhisperClientRenwu3;
import com.ruoyi.common.utils.file.DocumentParserUtilsRenwu3;
import com.ruoyi.common.utils.video.VideoAudioExtractorRenwu3;
import com.ruoyi.system.domain.CourseResourceKpRenwu3;
import com.ruoyi.system.domain.KnowledgePointRenwu3;
import com.ruoyi.system.mapper.CourseResourceKpMapperRenwu3;
import com.ruoyi.system.mapper.KnowledgePointMapperRenwu3;
import com.ruoyi.system.service.IResourceTaggingServiceRenwu3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

@Service
public class ResourceTaggingServiceImplRenwu3 implements IResourceTaggingServiceRenwu3
{
    private static final Logger log = LoggerFactory.getLogger(ResourceTaggingServiceImplRenwu3.class);

    @Autowired
    private KnowledgePointMapperRenwu3 knowledgePointMapper;

    @Autowired
    private CourseResourceKpMapperRenwu3 courseResourceKpMapper;

    @Override
    public Map<String, Object> analyzeAndRecommendKnowledgePoints(
        File file, 
        String fileType, 
        Long courseId, 
        String courseTitle
    )
    {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try
        {
            String documentText;
            
            // 判断文件类型：视频 or 文档
            boolean isVideo = fileType.equals("mp4") || fileType.equals("avi") || fileType.equals("mov") 
                || fileType.equals("wmv") || fileType.equals("flv") || fileType.equals("mkv");
            
            if (isVideo)
            {
                // 【视频处理流程】
                log.info("【任务3-视频】检测到视频文件，开始处理...");
                
                // 步骤1: 从视频提取音频
                File audioFile = null;
                try
                {
                    audioFile = VideoAudioExtractorRenwu3.extractAudioFromVideo(file);
                    
                    // 步骤2: 使用 Whisper 将音频转为文本
                    log.info("【任务3-视频】开始语音识别...");
                    documentText = WhisperClientRenwu3.transcribeAudio(audioFile);
                    
                    if (documentText == null || documentText.trim().isEmpty())
                    {
                        log.warn("【任务3-视频】语音识别结果为空");
                        result.put("recommendations", recommendations);
                        result.put("extractedText", "");
                        result.put("textLength", 0);
                        return result;
                    }
                    
                    log.info("【任务3-视频】语音识别完成，文本长度: {}", documentText.length());
                }
                finally
                {
                    // 清理临时音频文件
                    if (audioFile != null && audioFile.exists())
                    {
                        audioFile.delete();
                        log.info("【任务3-视频】已删除临时音频文件");
                    }
                }
            }
            else
            {
                // 【文档处理流程】
                log.info("【任务3-资源智能打标】开始解析文档: {}, 类型: {}", file.getName(), fileType);
                documentText = DocumentParserUtilsRenwu3.parseDocument(file, fileType);
                
                if (documentText == null || documentText.trim().isEmpty())
                {
                    log.warn("文档解析结果为空");
                    result.put("recommendations", recommendations);
                    result.put("extractedText", "");
                    result.put("textLength", 0);
                    return result;
                }
                
                log.info("文档解析完成，文本长度: {}", documentText.length());
            }
            
            // 步骤2: 调用LLM提取知识点
            log.info("【任务3】开始调用智谱AI提取知识点...");
            List<String> extractedKpTitles = DeepSeekClientRenwu3.extractKnowledgePoints(documentText, courseTitle);
            
            if (extractedKpTitles.isEmpty())
            {
                log.warn("LLM未提取到知识点");
                result.put("recommendations", recommendations);
                result.put("extractedText", documentText.length() > 500 ? documentText.substring(0, 500) + "..." : documentText);
                result.put("textLength", documentText.length());
                return result;
            }
            
            log.info("【任务3】LLM提取到{}个知识点: {}", extractedKpTitles.size(), extractedKpTitles);
            
            // 步骤3: 获取课程已有的知识点
            List<KnowledgePointRenwu3> existingKps = knowledgePointMapper.selectKnowledgePointListByCourseId(courseId);
            log.info("课程已有{}个知识点", existingKps.size());
            
            // 步骤4: 知识点匹配
            for (String extractedTitle : extractedKpTitles)
            {
                Map<String, Object> recommendation = new HashMap<>();
                recommendation.put("extractedTitle", extractedTitle);
                
                // 查找最佳匹配的已有知识点
                KnowledgePointRenwu3 bestMatch = null;
                double bestScore = 0.0;
                
                for (KnowledgePointRenwu3 kp : existingKps)
                {
                    double similarity = DeepSeekClientRenwu3.calculateSimilarity(extractedTitle, kp.getTitle());
                    if (similarity > bestScore)
                    {
                        bestScore = similarity;
                        bestMatch = kp;
                    }
                }
                
                // 如果相似度超过阈值(0.6)，推荐已有知识点
                if (bestMatch != null && bestScore >= 0.6)
                {
                    recommendation.put("matched", true);
                    recommendation.put("kpId", bestMatch.getId());
                    recommendation.put("kpTitle", bestMatch.getTitle());
                    recommendation.put("similarity", bestScore);
                    recommendation.put("isNew", false);
                    log.info("匹配到已有知识点: {} -> {} (相似度: {})", extractedTitle, bestMatch.getTitle(), bestScore);
                }
                else
                {
                    // 未匹配到，建议创建新知识点
                    recommendation.put("matched", false);
                    recommendation.put("kpTitle", extractedTitle);
                    recommendation.put("isNew", true);
                    log.info("建议创建新知识点: {}", extractedTitle);
                }
                
                recommendations.add(recommendation);
            }
            
            log.info("【任务3】知识点推荐完成，共{}条推荐", recommendations.size());
            
            // 返回完整结果
            result.put("recommendations", recommendations);
            result.put("extractedText", documentText.length() > 500 ? documentText.substring(0, 500) + "..." : documentText);
            result.put("textLength", documentText.length());
        }
        catch (Exception e)
        {
            log.error("【任务3】分析文档失败: {}", e.getMessage(), e);
            result.put("recommendations", recommendations);
            result.put("extractedText", "");
            result.put("textLength", 0);
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean confirmResourceKnowledgePoints(Long resourceId, List<Long> kpIds)
    {
        try
        {
            // 先删除该资源的旧关联
            courseResourceKpMapper.deleteCourseResourceKpByResourceId(resourceId);
            
            // 批量插入新关联
            if (kpIds != null && !kpIds.isEmpty())
            {
                List<CourseResourceKpRenwu3> list = new ArrayList<>();
                for (Long kpId : kpIds)
                {
                    CourseResourceKpRenwu3 relation = new CourseResourceKpRenwu3();
                    relation.setResourceId(resourceId);
                    relation.setKpId(kpId);
                    relation.setIsConfirmed(1); // 教师确认
                    relation.setCreateTime(new Date());
                    list.add(relation);
                }
                
                courseResourceKpMapper.batchInsertCourseResourceKp(list);
                log.info("【任务3】资源ID: {} 关联了{}个知识点", resourceId, kpIds.size());
            }
            
            return true;
        }
        catch (Exception e)
        {
            log.error("【任务3】保存资源知识点关联失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<KnowledgePointRenwu3> getResourceKnowledgePoints(Long resourceId)
    {
        try
        {
            List<Long> kpIds = courseResourceKpMapper.selectKpIdsByResourceId(resourceId);
            List<KnowledgePointRenwu3> knowledgePoints = new ArrayList<>();
            
            for (Long kpId : kpIds)
            {
                KnowledgePointRenwu3 kp = knowledgePointMapper.selectKnowledgePointById(kpId);
                if (kp != null)
                {
                    knowledgePoints.add(kp);
                }
            }
            
            return knowledgePoints;
        }
        catch (Exception e)
        {
            log.error("【任务3】获取资源知识点失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
