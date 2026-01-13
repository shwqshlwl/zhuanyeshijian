package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程信息 VO
 */
@Data
@Schema(description = "课程信息")
public class CourseVO {

    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程编码")
    private String courseCode;

    @Schema(description = "授课教师ID")
    private Long teacherId;

    @Schema(description = "授课教师姓名")
    private String teacherName;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "课程封面")
    private String coverImage;

    @Schema(description = "课程大纲")
    private String syllabus;

    @Schema(description = "学分")
    private BigDecimal credit;

    @Schema(description = "总学时")
    private Integer totalHours;

    @Schema(description = "学期")
    private String semester;

    @Schema(description = "状态：0-未开课，1-进行中，2-已结束")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
