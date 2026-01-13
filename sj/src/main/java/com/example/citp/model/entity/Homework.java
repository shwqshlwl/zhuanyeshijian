package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作业实体类
 */
@Data
@TableName("homework")
public class Homework implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String homeworkTitle;

    private Long courseId;

    private Long classId;

    private Long teacherId;

    private String content;

    private String attachment;

    private Integer totalScore;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 状态：0-草稿，1-已发布，2-已截止
     */
    private Integer status;

    /**
     * 是否允许迟交：0-不允许，1-允许
     */
    private Integer allowLate;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
