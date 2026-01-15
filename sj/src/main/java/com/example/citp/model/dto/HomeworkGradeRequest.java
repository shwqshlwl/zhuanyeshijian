package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 作业批改请求 DTO
 */
@Data
@Schema(description = "作业批改请求")
public class HomeworkGradeRequest {

    @NotNull(message = "分数不能为空")
    @Min(value = 0, message = "分数不能小于0")
    @Schema(description = "分数")
    private Integer score;

    @Schema(description = "评语")
    private String comment;
}
