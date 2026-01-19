package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 开课实例实体类
 */
@Data
@TableName("course_offering")
public class CourseOffering implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程模板ID（关联course表）
     */
    private Long courseId;

    /**
     * 学期ID（关联semester表）
     */
    private Long semesterId;

    /**
     * 授课教师ID
     */
    private Long teacherId;

    /**
     * 状态：0-未开课，1-进行中，2-已结课
     */
    private Integer status;

    /**
     * 最大选课人数（选修课）
     */
    private Integer maxStudents;

    /**
     * 当前选课人数
     */
    private Integer currentStudents;

    /**
     * 上课地点
     */
    private String classLocation;

    /**
     * 上课时间安排
     */
    private String classSchedule;

    /**
     * 备注信息
     */
    private String notes;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
