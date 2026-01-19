package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 开课实例信息 VO
 */
@Data
@Schema(description = "开课实例信息")
public class CourseOfferingVO {

    @Schema(description = "开课实例ID")
    private Long id;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程编码")
    private String courseCode;

    @Schema(description = "学分")
    private BigDecimal credit;

    @Schema(description = "课程类型：0-必修课，1-选修课")
    private Integer courseType;

    @Schema(description = "学期ID")
    private Long semesterId;

    @Schema(description = "学期名称")
    private String semesterName;

    @Schema(description = "学年")
    private String academicYear;

    @Schema(description = "教师ID")
    private Long teacherId;

    @Schema(description = "教师姓名")
    private String teacherName;

    @Schema(description = "状态：0-未开课，1-进行中，2-已结课")
    private Integer status;

    @Schema(description = "最大选课人数")
    private Integer maxStudents;

    @Schema(description = "当前选课人数")
    private Integer currentStudents;

    @Schema(description = "上课地点")
    private String classLocation;

    @Schema(description = "上课时间安排")
    private String classSchedule;

    @Schema(description = "备注信息")
    private String notes;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "关联班级数量")
    private Integer classCount;

    @Schema(description = "是否可以编辑")
    private Boolean canEdit;

    @Schema(description = "是否可以删除")
    private Boolean canDelete;
}
