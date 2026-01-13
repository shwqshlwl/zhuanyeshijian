package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试请求 DTO
 */
@Data
@Schema(description = "考试请求")
public class ExamRequest {

    @NotBlank(message = "考试名称不能为空")
    @Schema(description = "考试名称")
    private String examName;

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "考试说明")
    private String description;

    @Schema(description = "总分")
    private Integer totalScore;

    @Schema(description = "及格分")
    private Integer passScore;

    @Schema(description = "考试时长（分钟）")
    private Integer duration;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "考试类型：1-随堂测验，2-期中考试，3-期末考试")
    private Integer examType;

    @Schema(description = "是否乱序：0-否，1-是")
    private Integer isShuffle;

    @Schema(description = "是否显示答案：0-否，1-是")
    private Integer showAnswer;

    @Schema(description = "题目列表")
    private List<ExamQuestionItem> questions;

    @Data
    @Schema(description = "考试题目项")
    public static class ExamQuestionItem {
        @Schema(description = "题目ID")
        private Long questionId;

        @Schema(description = "题目分值")
        private Integer questionScore;

        @Schema(description = "题目顺序")
        private Integer questionOrder;
    }
}
