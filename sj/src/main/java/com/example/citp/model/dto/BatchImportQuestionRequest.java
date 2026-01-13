package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量导入题目请求 DTO
 */
@Data
@Schema(description = "批量导入题目请求")
public class BatchImportQuestionRequest {

    @NotEmpty(message = "题目列表不能为空")
    @Schema(description = "题目列表")
    private List<QuestionRequest> questions;
}
