package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 班级请求 DTO
 */
@Data
@Schema(description = "班级请求")
public class ClassRequest {

    @NotBlank(message = "班级名称不能为空")
    @Schema(description = "班级名称")
    private String className;

    @NotBlank(message = "班级编码不能为空")
    @Schema(description = "班级编码")
    private String classCode;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "关联课程ID（单个，保持向后兼容）")
    private Long courseId;

    @Schema(description = "关联课程ID列表（多对多）")
    private List<Long> courseIds;

    @Schema(description = "班级描述")
    private String description;
}
