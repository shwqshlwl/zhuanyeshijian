package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生选课实体类
 */
@Data
@TableName("student_course")
public class StudentCourse implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程类型：0-必修课，1-选修课
     */
    private Integer courseType;

    /**
     * 选课时间
     */
    private LocalDateTime selectionTime;

    /**
     * 状态：1-正常，0-已退课
     */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
