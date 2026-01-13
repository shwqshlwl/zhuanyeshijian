package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作业提交实体类
 */
@Data
@TableName("homework_submit")
public class HomeworkSubmit implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long homeworkId;

    private Long studentId;

    private String content;

    private String attachment;

    private LocalDateTime submitTime;

    private Integer score;

    private String comment;

    /**
     * 状态：0-未提交，1-已提交，2-已批改
     */
    private Integer status;

    /**
     * 是否迟交：0-否，1-是
     */
    private Integer isLate;

    private LocalDateTime gradeTime;

    private Long graderId;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
