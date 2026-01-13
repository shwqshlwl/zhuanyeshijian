package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实验请求 DTO
 */
@Data
@Schema(description = "实验请求")
public class ExperimentRequest {

    @NotBlank(message = "实验名称不能为空")
    @Schema(description = "实验名称")
    private String experimentName;

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "实验描述")
    private String description;

    @Schema(description = "实验要求")
    private String requirement;

    @Schema(description = "模板代码")
    private String templateCode;

    @Schema(description = "测试用例（JSON格式）")
    private String testCases;

    @Schema(description = "编程语言：java, python, c, cpp")
    private String language;

    @Schema(description = "时间限制（毫秒）")
    private Integer timeLimit;

    @Schema(description = "内存限制（MB）")
    private Integer memoryLimit;

    @Schema(description = "总分")
    private Integer totalScore;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "截止时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：0-草稿，1-已发布，2-已截止")
    private Integer status;
}
