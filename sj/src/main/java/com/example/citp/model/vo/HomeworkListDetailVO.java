package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "作业详情")
public class HomeworkListDetailVO {

    @Schema(description = "作业ID")
    private Long id;

    @Schema(description = "作业标题")
    private String homeworkTitle;

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
    private Integer allowLate;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "已提交人数")
    private Integer submittedCount;

    @Schema(description = "总人数")
    private Integer totalCount;
}
