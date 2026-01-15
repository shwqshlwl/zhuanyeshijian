package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业请求 DTO
 */
@Data
@Schema(description = "作业请求")
public class HomeworkRequest {

    @NotBlank(message = "作业标题不能为空")
    @Schema(description = "作业标题")
    private String homeworkTitle;

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "作业内容")
    private String content;

    @Schema(description = "附件URL")
    private String attachment;

    @Schema(description = "总分")
    private Integer totalScore;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "截止时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：0-草稿，1-已发布，2-已截止")
    private Integer status;

    @Schema(description = "是否允许迟交：0-不允许，1-允许")
    private Boolean allowLate;

    @Override
    public String toString() {
        return "HomeworkRequest{" +
                "homeworkTitle='" + homeworkTitle + '\'' +
                ", courseId=" + courseId +
                ", classId=" + classId +
                ", content='" + content + '\'' +
                ", attachment='" + attachment + '\'' +
                ", totalScore=" + totalScore +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                ", allowLate=" + allowLate +
                '}';
    }
}
