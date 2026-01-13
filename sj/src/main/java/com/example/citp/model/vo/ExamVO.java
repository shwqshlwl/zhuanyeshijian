package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试信息 VO
 */
@Data
@Schema(description = "考试信息")
public class ExamVO {

    @Schema(description = "考试ID")
    private Long id;

    @Schema(description = "考试名称")
    private String examName;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "教师ID")
    private Long teacherId;

    @Schema(description = "教师姓名")
    private String teacherName;

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

    @Schema(description = "状态：0-未开始，1-进行中，2-已结束")
    private Integer status;

    @Schema(description = "考试类型：1-随堂测验，2-期中考试，3-期末考试")
    private Integer examType;

    @Schema(description = "是否乱序：0-否，1-是")
    private Integer isShuffle;

    @Schema(description = "是否显示答案：0-否，1-是")
    private Integer showAnswer;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
