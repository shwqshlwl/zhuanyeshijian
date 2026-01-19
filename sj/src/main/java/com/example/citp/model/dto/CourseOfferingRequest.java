package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开课实例请求 DTO
 */
@Data
@Schema(description = "开课实例请求")
public class CourseOfferingRequest {

    @Schema(description = "课程模板ID")
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @Schema(description = "学期ID（不填则使用当前学期）")
    private Long semesterId;

    @Schema(description = "最大选课人数")
    private Integer maxStudents;

    @Schema(description = "上课地点")
    private String classLocation;

    @Schema(description = "上课时间安排")
    private String classSchedule;

    @Schema(description = "备注信息")
    private String notes;
}
