package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.GradingPromptPayload;
import com.exampl.smartcourse.dto.aiGrading.TriggerRequest;
import com.exampl.smartcourse.dto.aiGrading.TriggerResponse;
import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.entity.aiGrading.Assignment;
import com.exampl.smartcourse.entity.aiGrading.AssignmentKp;
import com.exampl.smartcourse.entity.aiGrading.AssignmentSubmission;
import com.exampl.smartcourse.entity.aiGrading.KnowledgePoint;
import com.exampl.smartcourse.entity.aiGrading.CourseResource;
import com.exampl.smartcourse.llmclient.aiGrading.SiliconFlowClient;
import com.exampl.smartcourse.mapper.aiGrading.AiGradingMapper;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentKpMapper;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentMapper;
import com.exampl.smartcourse.mapper.aiGrading.AssignmentSubmissionMapper;
import com.exampl.smartcourse.mapper.aiGrading.KnowledgePointMapper;
import com.exampl.smartcourse.mapper.aiGrading.CourseResourceMapper;
import com.exampl.smartcourse.util.FilePathResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class GradingTriggerService {

    private final AssignmentSubmissionMapper assignmentSubmissionMapper;
    private final AssignmentMapper assignmentMapper;
    private final AiGradingMapper aiGradingMapper;
    private final SiliconFlowClient siliconFlowClient;
    private final AssignmentKpMapper assignmentKpMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final FilePathResolver filePathResolver;
    private final CourseResourceMapper courseResourceMapper;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Logger logger = LoggerFactory.getLogger(GradingTriggerService.class);

    public TriggerResponse trigger(TriggerRequest request) {
        List<Long> accepted = new ArrayList<>();
        List<Long> failed = new ArrayList<>();
        String model = request.getLlmModel();
        java.util.List<Long> ids = request.getSubmissionIds();
        if ((ids == null || ids.isEmpty()) && request.getSubmissionId() != null) {
            ids = java.util.List.of(request.getSubmissionId());
        }
        if (ids == null || ids.isEmpty()) {
            return new TriggerResponse(accepted, failed, "no submissionId(s) provided");
        }
        for (Long sid : ids) {
            try {
                AssignmentSubmission sub = assignmentSubmissionMapper.selectById(sid);
                if (sub == null) { failed.add(sid); continue; }
                Assignment assignment = assignmentMapper.selectById(sub.getAssignmentId());
                if (assignment == null || !"file".equalsIgnoreCase(assignment.getMode())) { failed.add(sid); continue; }
                String localPath = filePathResolver.resolve(sub.getFilePath());
                logger.info("trigger reading file sid={} path={} ", sid, localPath);
                String content = FileContentReader.readFileContent(localPath);
                logger.info("trigger file read ok sid={} content_len={}", sid, content == null ? 0 : content.length());
                GradingPromptPayload payload = new GradingPromptPayload();
                payload.setContent(content);
                payload.setAssignmentTitle(assignment.getTitle());
                payload.setAssignmentDescription(assignment.getDescription());
                payload.setStartTime(assignment.getStartTime() != null ? assignment.getStartTime().toString() : null);
                payload.setCourseId(assignment.getCourseId());
                java.util.List<AssignmentKp> links = assignmentKpMapper.selectByAssignmentId(assignment.getId());
                java.util.List<GradingPromptPayload.Kp> kps = new java.util.ArrayList<>();
                for (AssignmentKp link : links) {
                    KnowledgePoint kp = knowledgePointMapper.selectById(link.getKpId());
                    if (kp == null) continue;
                    GradingPromptPayload.Kp item = new GradingPromptPayload.Kp();
                    item.setTitle(kp.getTitle());
                    item.setDescription(kp.getDescription());
                    item.setSequence(link.getSequence());
                    item.setRequired(link.getIsRequired());
                    kps.add(item);
                }
                payload.setKnowledgePoints(kps);
                List<CourseResource> resources = courseResourceMapper.selectByCourseId(assignment.getCourseId());
                List<GradingPromptPayload.Resource> rs = new java.util.ArrayList<>();
                for (CourseResource cr : resources) {
                    GradingPromptPayload.Resource rsrc = new GradingPromptPayload.Resource();
                    rsrc.setName(cr.getName());
                    rsrc.setDescription(cr.getDescription());
                    rs.add(rsrc);
                }
                payload.setResources(rs);
                AiGrading g = new AiGrading();
                g.setAssignmentSubmissionId(sid);
                g.setLlmModel(model);
                g.setTeacherConfirmed(false);
                g.setTeacherModifiedScore(null);
                g.setTeacherComment(null);
                g.setConfirmedBy(null);
                g.setConfirmedAt(null);
                g.setCreatedAt(LocalDateTime.now());
                g.setUpdatedAt(LocalDateTime.now());
                g.setStatus("processing");
                aiGradingMapper.insert(g);
                logger.info("trigger inserted grading sid={} gradingId={} status={}", sid, g.getId(), g.getStatus());

                final Long recordId = g.getId();
                executor.submit(() -> {
                    try {
                        logger.info("async grading start sid={} gradingId={} model={} ", sid, recordId, model);
                        var result = siliconFlowClient.grade(payload, model);
                        g.setContentScore(result.getContentScore());
                        g.setLogicScore(result.getLogicScore());
                        g.setKnowledgeScore(result.getKnowledgeScore());
                        g.setInnovationScore(result.getInnovationScore());
                        g.setTotalScore(result.getTotalScore());
                        g.setAiComment(result.getAiComment());
                        g.setImprovementSuggestions(result.getImprovementSuggestions());
                        g.setCoveredKnowledgePoints(result.getCoveredKnowledgePoints());
                        g.setMissingKnowledgePoints(result.getMissingKnowledgePoints());
                        g.setLlmTokens(result.getLlmTokens());
                        g.setProcessingTime(result.getProcessingTime());
                        g.setStatus("completed");
                        g.setUpdatedAt(LocalDateTime.now());
                        aiGradingMapper.update(g);
                        logger.info("async grading done sid={} gradingId={} status={} tokens={} latency_ms={} ", sid, recordId, g.getStatus(), g.getLlmTokens(), g.getProcessingTime());
                    } catch (Exception ex) {
                        g.setStatus("failed");
                        g.setErrorMessage(ex.getMessage());
                        g.setUpdatedAt(LocalDateTime.now());
                        aiGradingMapper.update(g);
                        logger.error("async grading failed sid={} gradingId={} error={} ", sid, recordId, ex.getMessage(), ex);
                    }
                });
                accepted.add(sid);
            } catch (UnsupportedOperationException e) {
                failed.add(sid);
                logger.error("trigger failed sid={} error={} ", sid, e.getMessage(), e);
            } catch (Exception e) {
                failed.add(sid);
                logger.error("trigger failed sid={} error={} ", sid, e.getMessage(), e);
            }
        }
        return new TriggerResponse(accepted, failed, "trigger done");
    }
}