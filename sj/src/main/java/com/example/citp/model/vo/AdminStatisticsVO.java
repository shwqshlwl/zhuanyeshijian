package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员统计数据 VO
 */
@Data
@Schema(description = "管理员统计数据")
public class AdminStatisticsVO {

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "教师总数")
    private Long totalTeachers;

    @Schema(description = "学生总数")
    private Long totalStudents;

    @Schema(description = "总课程数")
    private Long totalCourses;

    @Schema(description = "总班级数")
    private Long totalClasses;

    @Schema(description = "总作业数")
    private Long totalHomeworks;

    @Schema(description = "总考试数")
    private Long totalExams;
}
