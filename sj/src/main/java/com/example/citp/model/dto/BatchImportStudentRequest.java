package com.example.citp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量导入学生请求 DTO
 */
@Data
@Schema(description = "批量导入学生请求")
public class BatchImportStudentRequest {

    @Schema(description = "课程ID（可选）")
    private Long courseId;

    @NotNull(message = "班级ID不能为空")
    @Schema(description = "班级ID")
    private Long classId;

    @NotNull(message = "学生列表不能为空")
    @Schema(description = "学生列表")
    private List<StudentInfo> students;

    @Data
    @Schema(description = "学生信息")
    public static class StudentInfo {
        @NotBlank(message = "学号不能为空")
        @Schema(description = "学号")
        private String studentNo;

        @NotBlank(message = "姓名不能为空")
        @Schema(description = "姓名")
        private String realName;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "手机号")
        private String phone;
    }
}
