package com.exampl.smartcourse.service.aiGrading;

import com.exampl.smartcourse.entity.aiGrading.AiGrading;
import com.exampl.smartcourse.mapper.aiGrading.AiGradingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GradingStatusService {

    private final AiGradingMapper aiGradingMapper;
    private final com.exampl.smartcourse.mapper.aiGrading.AssignmentSubmissionMapper assignmentSubmissionMapper;

    public boolean deleteLatest(Long submissionId) {
        AiGrading latest = aiGradingMapper.selectLatestBySubmissionId(submissionId);
        if (latest == null) {
            return false;
        }
        int r = aiGradingMapper.deleteById(latest.getId());
        return r > 0;
    }

    public int markAllHavingGradingAsGraded() {
        return assignmentSubmissionMapper.markAllWithGradingAsGraded();
    }
}
