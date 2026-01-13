package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实验信息 VO
 */
@Data
@Schema(description = "实验信息")
public class ExperimentVO {

    @Schema(description = "实验ID")
    private Long id;

    @Schema(description = "实验名称")
    private String experimentName;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "教师ID")
    private Long teacherId;

    @Schema(description = "教师姓名")
    private String teacherName;

    @Schema(description = "实验描述")
    private String description;

    @Schema(description = "实验要求")
    private String requirement;

    @Schema(description = "模板代码")
    private String templateCode;

    @Schema(description = "测试用例（JSON格式）")
    private String testCases;

    @Schema(description = "编程语言")
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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
