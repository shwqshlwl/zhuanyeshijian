package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学期实体类
 */
@Data
@TableName("semester")
public class Semester implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学期名称，如：2024-2025学年第1学期
     */
    private String semesterName;

    /**
     * 学期代码，如：202401
     */
    private String semesterCode;

    /**
     * 学年，如：2024-2025
     */
    private String academicYear;

    /**
     * 学期序号：1-春季，2-夏季，3-秋季
     */
    private Integer termNumber;

    /**
     * 学期开始日期
     */
    private LocalDate startDate;

    /**
     * 学期结束日期
     */
    private LocalDate endDate;

    /**
     * 状态：0-未开始，1-进行中，2-已结束
     */
    private Integer status;

    /**
     * 是否当前学期：0-否，1-是（全局只能有一个）
     */
    private Integer isCurrent;

    /**
     * 学期描述
     */
    private String description;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
