package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 实验提交统计 VO
 */
@Data
@Schema(description = "实验提交统计")
public class ExperimentSubmitStatVO {

    @Schema(description = "总学生数")
    private Integer totalStudents;

    @Schema(description = "已提交学生数")
    private Integer submittedCount;

    @Schema(description = "未提交学生数")
    private Integer unsubmittedCount;

    @Schema(description = "未提交学生列表")
    private List<StudentInfo> unsubmittedStudents;

    @Data
    @Schema(description = "学生信息")
    public static class StudentInfo {
        @Schema(description = "学生ID")
        private Long studentId;

        @Schema(description = "学号")
        private String studentNo;

        @Schema(description = "学生姓名")
        private String studentName;

        @Schema(description = "班级名称")
        private String className;
    }
}
