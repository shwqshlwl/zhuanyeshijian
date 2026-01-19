package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学期请求 DTO
 */
@Data
@Schema(description = "学期请求")
public class SemesterRequest {

    @Schema(description = "学期名称")
    @NotBlank(message = "学期名称不能为空")
    private String semesterName;

    @Schema(description = "学期代码")
    @NotBlank(message = "学期代码不能为空")
    private String semesterCode;

    @Schema(description = "学年")
    @NotBlank(message = "学年不能为空")
    private String academicYear;

    @Schema(description = "学期序号：1-春季，2-夏季，3-秋季")
    @NotNull(message = "学期序号不能为空")
    private Integer termNumber;

    @Schema(description = "学期开始日期")
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @Schema(description = "学期结束日期")
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @Schema(description = "学期描述")
    private String description;
}
