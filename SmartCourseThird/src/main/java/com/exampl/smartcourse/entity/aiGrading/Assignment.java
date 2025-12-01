package com.exampl.smartcourse.entity.aiGrading;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;

@Data
@Alias("AiGradingAssignment")
public class Assignment {
    private Long id;
    private String title;
    private Long courseId;
    private Long publisherUserId;
    private String publisherRealName;
    private String type;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private Integer status;
    private LocalDateTime updateTime;
    private String mode;
    private Integer timeLimit;
    private Integer totalScore;
    private Integer duration;
    @JsonDeserialize(using = com.exampl.smartcourse.entity.aiGrading.json.AllowedFileTypesDeserializer.class)
    private String allowedFileTypes;
    @JsonDeserialize(using = com.exampl.smartcourse.entity.aiGrading.json.AttachmentsDeserializer.class)
    private String attachments;
    private Boolean isDeleted;
    private LocalDateTime deleteTime;

    
}
