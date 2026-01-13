package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程请求 DTO
 */
@Data
@Schema(description = "课程请求")
public class CourseRequest {

    @NotBlank(message = "课程名称不能为空")
    @Schema(description = "课程名称")
    private String courseName;

    @NotBlank(message = "课程编码不能为空")
    @Schema(description = "课程编码")
    private String courseCode;

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
}
