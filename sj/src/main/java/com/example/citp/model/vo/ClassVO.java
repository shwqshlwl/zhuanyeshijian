package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级信息 VO
 */
@Data
@Schema(description = "班级信息")
public class ClassVO {

    @Schema(description = "班级ID")
    private Long id;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "班级编码")
    private String classCode;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "关联课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "教师ID")
    private Long teacherId;

    @Schema(description = "教师姓名")
    private String teacherName;

    @Schema(description = "学生人数")
    private Integer studentCount;

    @Schema(description = "班级描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
