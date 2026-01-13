package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题库实体类
 */
@Data
@TableName("question_bank")
public class QuestionBank implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionTypeId;

    private Long courseId;

    private Long teacherId;

    private String questionContent;

    private String options;

    private String correctAnswer;

    private String analysis;

    /**
     * 难度：1-简单，2-中等，3-困难
     */
    private Integer difficulty;

    private Integer score;

    private String tags;

    private Integer usageCount;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
