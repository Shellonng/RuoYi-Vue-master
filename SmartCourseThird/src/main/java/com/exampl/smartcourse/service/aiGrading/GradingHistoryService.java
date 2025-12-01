package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.mapper.aiGrading.AiGradingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradingHistoryService {

    private final AiGradingMapper aiGradingMapper;

    public List<AiGrading> listHistoryBySubmissionId(Long submissionId) {
        return aiGradingMapper.selectAllBySubmissionId(submissionId);
    }
}
