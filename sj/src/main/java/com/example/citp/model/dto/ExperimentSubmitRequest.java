package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 实验提交请求 DTO
 */
@Data
@Schema(description = "实验提交请求")
public class ExperimentSubmitRequest {

    @NotBlank(message = "代码不能为空")
    @Schema(description = "提交代码")
    private String code;

    @Schema(description = "编程语言")
    private String language;
}
