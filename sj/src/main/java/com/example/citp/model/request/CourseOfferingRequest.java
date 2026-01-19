package com.example.citp.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 开课实例请求对象
 */
@Data
@Schema(description = "开课实例请求")
public class CourseOfferingRequest {

    @Schema(description = "课程ID")
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @Schema(description = "学期ID")
    @NotNull(message = "学期ID不能为空")
    private Long semesterId;

    @Schema(description = "教师ID")
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @Schema(description = "状态：0-未开课，1-进行中，2-已结课")
    private Integer status;

    @Schema(description = "最大选课人数")
    private Integer maxStudents;

    @Schema(description = "上课地点")
    private String classLocation;

    @Schema(description = "上课时间安排")
    private String classSchedule;

    @Schema(description = "备注信息")
    private String notes;
}
