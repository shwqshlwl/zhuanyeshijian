package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目 VO
 */
@Data
@Schema(description = "题目信息")
public class QuestionVO {

    @Schema(description = "题目ID")
    private Long id;

    @Schema(description = "题型ID")
    private Long questionTypeId;

    @Schema(description = "题型名称")
    private String questionTypeName;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "教师ID")
    private Long teacherId;

    @Schema(description = "教师姓名")
    private String teacherName;

    @Schema(description = "题目内容")
    private String questionContent;

    @Schema(description = "选项（JSON格式）")
    private String options;

    @Schema(description = "正确答案")
    private String correctAnswer;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "难度：1-简单，2-中等，3-困难")
    private Integer difficulty;

    @Schema(description = "分值")
    private Integer score;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "使用次数")
    private Integer usageCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
