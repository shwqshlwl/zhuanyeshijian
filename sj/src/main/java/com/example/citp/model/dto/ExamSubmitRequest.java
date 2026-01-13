package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 考试提交请求 DTO
 */
@Data
@Schema(description = "考试提交请求")
public class ExamSubmitRequest {

    @Schema(description = "学生答案（题目ID -> 答案）")
    private Map<Long, String> answers;
}
