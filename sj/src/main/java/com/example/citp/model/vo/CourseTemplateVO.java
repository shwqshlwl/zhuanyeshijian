package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程模板信息 VO（含开课状态）
 */
@Data
@Schema(description = "课程模板信息")
public class CourseTemplateVO {

    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程编码")
    private String courseCode;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "学分")
    private BigDecimal credit;

    @Schema(description = "总学时")
    private Integer totalHours;

    @Schema(description = "课程类型：0-必修课，1-选修课")
    private Integer courseType;

    @Schema(description = "教师ID")
    private Long teacherId;

    @Schema(description = "教师姓名")
    private String teacherName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "本学期是否已开课")
    private Boolean isOfferedThisSemester;

    @Schema(description = "本学期开课实例ID")
    private Long offeringId;

    @Schema(description = "本学期开课状态：0-未开课，1-进行中，2-已结课")
    private Integer offeringStatus;

    @Schema(description = "本学期授课教师ID")
    private Long offeringTeacherId;

    @Schema(description = "本学期授课教师姓名")
    private String offeringTeacherName;

    @Schema(description = "历史开课次数")
    private Integer offeringCount;
}
