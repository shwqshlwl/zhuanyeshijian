package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 选修课信息 VO（用于选课中心）
 */
@Data
@Schema(description = "选修课信息")
public class ElectiveCourseVO {

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

    @Schema(description = "学分")
    private BigDecimal credit;

    @Schema(description = "总学时")
    private Integer totalHours;

    @Schema(description = "学期")
    private String semester;

    @Schema(description = "最大选课人数（0表示不限制）")
    private Integer maxStudents;

    @Schema(description = "当前选课人数")
    private Integer currentStudents;

    @Schema(description = "选课开始时间")
    private LocalDateTime selectionStartTime;

    @Schema(description = "选课结束时间")
    private LocalDateTime selectionEndTime;

    @Schema(description = "是否已选")
    private Boolean isSelected;

    @Schema(description = "是否可选（在选课时间内且未满员）")
    private Boolean canSelect;

    @Schema(description = "是否可退（在选课时间内且已选）")
    private Boolean canDrop;

    @Schema(description = "不可选原因")
    private String unavailableReason;
}
