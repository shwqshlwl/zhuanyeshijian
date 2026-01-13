package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题型实体类
 */
@Data
@TableName("question_type")
public class QuestionType implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String typeName;

    private String typeCode;

    private String description;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
