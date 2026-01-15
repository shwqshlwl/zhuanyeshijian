package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实验结果 VO
 */
@Data
@Schema(description = "实验结果")
public class ExperimentResultVO {

    @Schema(description = "提交ID")
    private Long submitId;

    @Schema(description = "状态：0-待评测，1-评测中，2-通过，3-未通过，4-编译错误，5-运行错误")
    private Integer status;

    @Schema(description = "得分")
    private Integer score;

    @Schema(description = "通过测试用例数")
    private Integer passCount;

    @Schema(description = "总测试用例数")
    private Integer totalCount;

    @Schema(description = "执行时间（毫秒）")
    private Integer executeTime;

    @Schema(description = "内存使用（KB）")
    private Integer memoryUsed;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "详细结果（JSON格式）")
    private String resultDetail;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "代码")
    private String code;
}
