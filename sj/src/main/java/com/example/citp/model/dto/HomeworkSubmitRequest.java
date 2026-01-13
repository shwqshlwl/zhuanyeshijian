package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 作业提交请求 DTO
 */
@Data
@Schema(description = "作业提交请求")
public class HomeworkSubmitRequest {

    @NotBlank(message = "提交内容不能为空")
    @Schema(description = "提交内容")
    private String content;

    @Schema(description = "附件URL")
    private String attachment;
}
