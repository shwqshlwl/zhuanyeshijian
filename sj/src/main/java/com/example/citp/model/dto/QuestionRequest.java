package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 题目请求 DTO
 */
@Data
@Schema(description = "题目请求")
public class QuestionRequest {

    @NotNull(message = "题型ID不能为空")
    @Schema(description = "题型ID：1-单选题，2-多选题，3-判断题，4-填空题，5-简答题，6-编程题")
    private Long questionTypeId;

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Long courseId;

    @NotBlank(message = "题目内容不能为空")
    @Schema(description = "题目内容")
    private String questionContent;

    @Schema(description = "选项（JSON格式，如：[\"A. 选项1\", \"B. 选项2\"]）")
    private String options;

    @Schema(description = "正确答案")
    private String correctAnswer;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "难度：1-简单，2-中等，3-困难")
    private Integer difficulty;

    @Schema(description = "分值")
    private Integer score;

    @Schema(description = "标签（逗号分隔）")
    private String tags;
}
