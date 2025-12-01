package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.GradingModificationRequest;
import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.mapper.aiGrading.AiGradingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TeacherConfirmService {

    private final AiGradingMapper aiGradingMapper;

    public AiGrading confirm(Long submissionId, Long teacherId, GradingModificationRequest req) {
        AiGrading latest = aiGradingMapper.selectLatestBySubmissionId(submissionId);
        AiGrading g = new AiGrading();
        g.setAssignmentSubmissionId(submissionId);
        g.setContentScore(req.getContentScore() != null ? req.getContentScore() : latest != null ? latest.getContentScore() : null);
        g.setLogicScore(req.getLogicScore() != null ? req.getLogicScore() : latest != null ? latest.getLogicScore() : null);
        g.setKnowledgeScore(req.getKnowledgeScore() != null ? req.getKnowledgeScore() : latest != null ? latest.getKnowledgeScore() : null);
        g.setInnovationScore(req.getInnovationScore() != null ? req.getInnovationScore() : latest != null ? latest.getInnovationScore() : null);
        g.setTotalScore(req.getTotalScore() != null ? req.getTotalScore() : latest != null ? latest.getTotalScore() : null);
        g.setAiComment(req.getAiComment() != null ? req.getAiComment() : latest != null ? latest.getAiComment() : null);
        g.setImprovementSuggestions(req.getImprovementSuggestions() != null ? req.getImprovementSuggestions() : latest != null ? latest.getImprovementSuggestions() : null);
        g.setCoveredKnowledgePoints(req.getCoveredKnowledgePoints() != null ? req.getCoveredKnowledgePoints() : latest != null ? latest.getCoveredKnowledgePoints() : null);
        g.setMissingKnowledgePoints(req.getMissingKnowledgePoints() != null ? req.getMissingKnowledgePoints() : latest != null ? latest.getMissingKnowledgePoints() : null);
        g.setLlmModel(req.getLlmModel() != null ? req.getLlmModel() : latest != null ? latest.getLlmModel() : null);
        g.setTeacherConfirmed(true);
        g.setTeacherModifiedScore(req.getTotalScore() != null ? req.getTotalScore() : latest != null ? latest.getTotalScore() : null);
        g.setTeacherComment(req.getTeacherComment());
        g.setConfirmedBy(teacherId);
        g.setConfirmedAt(LocalDateTime.now());
        g.setCreatedAt(LocalDateTime.now());
        g.setUpdatedAt(LocalDateTime.now());
        g.setStatus("completed");
        aiGradingMapper.insert(g);
        return g;
    }
}
