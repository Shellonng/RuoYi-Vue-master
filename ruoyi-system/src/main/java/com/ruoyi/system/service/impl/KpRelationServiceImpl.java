package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.KnowledgePoint;
import com.ruoyi.system.domain.KpRelation;
import com.ruoyi.system.mapper.KnowledgePointMapper;
import com.ruoyi.system.mapper.KpRelationMapper;
import com.ruoyi.system.service.AIService;
import com.ruoyi.system.service.IKpRelationService;

/**
 * 知识点关系Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class KpRelationServiceImpl implements IKpRelationService
{
    private static final Logger log = LoggerFactory.getLogger(KpRelationServiceImpl.class);

    @Autowired
    private KpRelationMapper kpRelationMapper;

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Autowired
    private AIService aiService;

    /**
     * 查询知识点关系列表
     *
     * @param kpRelation 知识点关系
     * @return 知识点关系集合
     */
    @Override
    public List<KpRelation> selectKpRelationList(KpRelation kpRelation)
    {
        return kpRelationMapper.selectKpRelationList(kpRelation);
    }

    /**
     * 根据课程ID查询所有知识点关系
     *
     * @param courseId 课程ID
     * @return 知识点关系集合
     */
    @Override
    public List<KpRelation> selectKpRelationListByCourseId(Long courseId)
    {
        return kpRelationMapper.selectKpRelationListByCourseId(courseId);
    }

    /**
     * 新增知识点关系
     *
     * @param kpRelation 知识点关系
     * @return 结果
     */
    @Override
    public int insertKpRelation(KpRelation kpRelation)
    {
        return kpRelationMapper.insertKpRelation(kpRelation);
    }

    /**
     * 批量新增知识点关系
     *
     * @param kpRelations 知识点关系列表
     * @return 结果
     */
    @Override
    @Transactional
    public int batchInsertKpRelations(List<KpRelation> kpRelations)
    {
        if (kpRelations == null || kpRelations.isEmpty())
        {
            return 0;
        }
        return kpRelationMapper.batchInsertKpRelations(kpRelations);
    }

    /**
     * 删除知识点关系
     *
     * @param id 知识点关系主键
     * @return 结果
     */
    @Override
    public int deleteKpRelationById(Long id)
    {
        return kpRelationMapper.deleteKpRelationById(id);
    }

    /**
     * 根据课程ID删除所有知识点关系
     *
     * @param courseId 课程ID
     * @return 结果
     */
    @Override
    public int deleteKpRelationsByCourseId(Long courseId)
    {
        return kpRelationMapper.deleteKpRelationsByCourseId(courseId);
    }

    /**
     * AI生成课程知识点关系图谱
     *
     * @param courseId 课程ID
     * @return 生成结果消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateKnowledgeGraph(Long courseId)
    {
        log.info("开始为课程{}生成知识点关系图谱", courseId);

        try
        {
            // 1. 查询该课程所有未删除的知识点
            KnowledgePoint queryKp = new KnowledgePoint();
            queryKp.setCourseId(courseId);
            queryKp.setIsDeleted(0);
            List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectKnowledgePointList(queryKp);

            if (knowledgePoints == null || knowledgePoints.isEmpty())
            {
                return "该课程暂无知识点，无法生成关系图谱";
            }

            if (knowledgePoints.size() < 2)
            {
                return "知识点数量不足（至少需要2个），无法生成关系图谱";
            }

            log.info("课程{}共有{}个知识点", courseId, knowledgePoints.size());

            // 2. 构建知识点列表JSON（只包含id和title）
            JSONArray kpArray = new JSONArray();
            for (KnowledgePoint kp : knowledgePoints)
            {
                JSONObject kpObj = new JSONObject();
                kpObj.put("id", kp.getId());
                kpObj.put("title", kp.getTitle());
                if (kp.getLevel() != null && !kp.getLevel().isEmpty())
                {
                    kpObj.put("level", kp.getLevel());
                }
                kpArray.add(kpObj);
            }

            String knowledgePointsJson = kpArray.toJSONString();
            log.info("准备调用AI生成关系，知识点JSON：{}", knowledgePointsJson);

            // 3. 调用AI生成知识点关系
            String aiResponseJson = aiService.generateKnowledgePointRelations(knowledgePointsJson);
            log.info("AI返回的关系JSON：{}", aiResponseJson);

            // 4. 解析AI返回的关系
            JSONObject responseObj = JSON.parseObject(aiResponseJson);
            JSONArray relations = responseObj.getJSONArray("relations");

            if (relations == null || relations.isEmpty())
            {
                log.warn("AI未生成任何知识点关系");
                return "AI分析后未发现明显的知识点关系";
            }

            // 5. 删除该课程原有的关系（重新生成）
            int deletedCount = kpRelationMapper.deleteKpRelationsByCourseId(courseId);
            log.info("删除课程{}原有的{}条知识点关系", courseId, deletedCount);

            // 6. 转换为KpRelation对象并批量插入
            List<KpRelation> kpRelations = new ArrayList<>();
            for (int i = 0; i < relations.size(); i++)
            {
                JSONObject relationObj = relations.getJSONObject(i);
                
                Long fromKpId = relationObj.getLong("fromKpId");
                Long toKpId = relationObj.getLong("toKpId");
                String relationType = relationObj.getString("relationType");

                // 验证关系类型
                if (!isValidRelationType(relationType))
                {
                    log.warn("跳过无效的关系类型：{}", relationType);
                    continue;
                }

                // 验证知识点ID是否在课程中
                if (!isKpInCourse(fromKpId, knowledgePoints) || !isKpInCourse(toKpId, knowledgePoints))
                {
                    log.warn("跳过无效的知识点ID：from={}, to={}", fromKpId, toKpId);
                    continue;
                }

                KpRelation relation = new KpRelation();
                relation.setFromKpId(fromKpId);
                relation.setToKpId(toKpId);
                relation.setRelationType(relationType);
                relation.setAiGenerated(1);
                
                kpRelations.add(relation);
            }

            if (kpRelations.isEmpty())
            {
                log.warn("没有有效的知识点关系可以保存");
                return "AI生成的关系不符合要求，未保存任何关系";
            }

            // 7. 批量插入关系
            int insertedCount = kpRelationMapper.batchInsertKpRelations(kpRelations);
            log.info("成功插入{}条知识点关系", insertedCount);

            return String.format("成功生成%d条知识点关系", insertedCount);
        }
        catch (Exception e)
        {
            log.error("生成知识点关系图谱失败，课程ID：{}", courseId, e);
            throw new ServiceException("生成知识点关系图谱失败：" + e.getMessage());
        }
    }

    /**
     * 验证关系类型是否有效
     */
    private boolean isValidRelationType(String relationType)
    {
        if (relationType == null)
        {
            return false;
        }
        return relationType.equals("prerequisite_of") 
            || relationType.equals("similar_to")
            || relationType.equals("extension_of")
            || relationType.equals("derived_from")
            || relationType.equals("counterexample_of");
    }

    /**
     * 验证知识点ID是否在课程的知识点列表中
     */
    private boolean isKpInCourse(Long kpId, List<KnowledgePoint> knowledgePoints)
    {
        if (kpId == null)
        {
            return false;
        }
        for (KnowledgePoint kp : knowledgePoints)
        {
            if (kp.getId().equals(kpId))
            {
                return true;
            }
        }
        return false;
    }
}
