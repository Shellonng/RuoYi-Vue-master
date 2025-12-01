package com.exampl.smartcourse.dto.aiGrading;

import lombok.Data;
import java.util.List;

@Data
public class GradingPromptPayload {
    private String content;
    private String assignmentTitle;
    private String assignmentDescription;
    private String startTime;
    private Long courseId;
    private List<Kp> knowledgePoints;
    private List<Resource> resources;

    @Data
    public static class Kp {
        private String title;
        private String description;
        private Integer sequence;
        private Boolean required;
    }

    @Data
    public static class Resource {
        private String name;
        private String description;
    }
}