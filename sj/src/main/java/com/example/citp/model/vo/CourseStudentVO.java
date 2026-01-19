package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选课学生信息 VO
 */
@Data
@Schema(description = "选课学生信息")
public class CourseStudentVO {

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "学号")
    private String studentNumber;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "选课时间")
    private LocalDateTime selectionTime;

    @Schema(description = "课程类型：0-必修课，1-选修课")
    private Integer courseType;

    @Schema(description = "状态：1-正常，0-已退课")
    private Integer status;
}
