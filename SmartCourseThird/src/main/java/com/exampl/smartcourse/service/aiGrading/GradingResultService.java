package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.GradingResultResponse;
import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.mapper.aiGrading.AiGradingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradingResultService {

    private final AiGradingMapper aiGradingMapper;

    public GradingResultResponse getBySubmissionId(Long submissionId) {
        // 使用selectLatestBySubmissionId方法获取最新的AI批改结果，避免TooManyResultsException异常
        AiGrading g = aiGradingMapper.selectLatestBySubmissionId(submissionId);
        if (g == null) {
            return null;
        }
        GradingResultResponse r = new GradingResultResponse();
        r.setContentScore(g.getContentScore());
        r.setLogicScore(g.getLogicScore());
        r.setKnowledgeScore(g.getKnowledgeScore());
        r.setInnovationScore(g.getInnovationScore());
        r.setTotalScore(g.getTotalScore());
        r.setAiComment(g.getAiComment());
        r.setImprovementSuggestions(g.getImprovementSuggestions());
        r.setCoveredKnowledgePoints(g.getCoveredKnowledgePoints());
        r.setMissingKnowledgePoints(g.getMissingKnowledgePoints());
        r.setLlmModel(g.getLlmModel());
        r.setTeacherConfirmed(g.getTeacherConfirmed());
        r.setTeacherModifiedScore(g.getTeacherModifiedScore());
        r.setTeacherComment(g.getTeacherComment());
        r.setConfirmedBy(g.getConfirmedBy());
        r.setConfirmedAt(g.getConfirmedAt());
        r.setCreatedAt(g.getCreatedAt());
        r.setUpdatedAt(g.getUpdatedAt());
        return r;
    }
}