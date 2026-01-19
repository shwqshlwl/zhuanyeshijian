package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生统计数据 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生统计数据")
public class StudentStatisticsVO {

    @Schema(description = "我的班级数量")
    private Integer classCount;

    @Schema(description = "我的课程数量")
    private Integer courseCount;

    @Schema(description = "我的作业数量")
    private Integer homeworkCount;

    @Schema(description = "我的考试数量")
    private Integer examCount;
}
