package com.example.citp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学期信息 VO
 */
@Data
@Schema(description = "学期信息")
public class SemesterVO {

    @Schema(description = "学期ID")
    private Long id;

    @Schema(description = "学期名称")
    private String semesterName;

    @Schema(description = "学期代码")
    private String semesterCode;

    @Schema(description = "学年")
    private String academicYear;

    @Schema(description = "学期序号：1-春季，2-夏季，3-秋季")
    private Integer termNumber;

    @Schema(description = "学期开始日期")
    private LocalDate startDate;

    @Schema(description = "学期结束日期")
    private LocalDate endDate;

    @Schema(description = "状态：0-未开始，1-进行中，2-已结束")
    private Integer status;

    @Schema(description = "是否当前学期")
    private Integer isCurrent;

    @Schema(description = "学期描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "开课数量")
    private Integer offeringCount;
}
