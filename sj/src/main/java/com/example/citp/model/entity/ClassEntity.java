package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班级实体类
 */
@Data
@TableName("class")
public class ClassEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String className;

    private String classCode;

    private String grade;

    private String major;

    private Long courseId;

    private Long teacherId;

    private Integer studentCount;

    private String description;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
