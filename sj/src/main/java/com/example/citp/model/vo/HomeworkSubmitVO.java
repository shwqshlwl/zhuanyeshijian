package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业提交 VO
 */
@Data
@Schema(description = "作业提交信息")
public class HomeworkSubmitVO {

    @Schema(description = "提交ID")
    private Long id;

    @Schema(description = "作业ID")
    private Long homeworkId;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "提交内容")
    private String content;

    @Schema(description = "附件URL")
    private String attachment;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "得分")
    private Integer score;

    @Schema(description = "教师评语")
    private String comment;

    @Schema(description = "状态：0-未提交，1-已提交，2-已批改")
    private Integer status;

    @Schema(description = "是否迟交：0-否，1-是")
    private Integer isLate;

    @Schema(description = "批改时间")
    private LocalDateTime gradeTime;

    @Schema(description = "批改教师ID")
    private Long graderId;

    @Schema(description = "批改教师姓名")
    private String graderName;
}
