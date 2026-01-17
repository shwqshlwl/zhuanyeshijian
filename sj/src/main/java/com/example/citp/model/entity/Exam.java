package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考试实体类
 */
@Data
@TableName("exam")
public class Exam implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String examName;

    private Long courseId;

    private Long classId;

    private Long teacherId;

    private String description;

    private Integer totalScore;

    private Integer passScore;

    private Integer duration;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 状态：0-未开始，1-进行中，2-已结束
     */
    private Integer status;

    /**
     * 考试类型：1-随堂测验，2-期中考试，3-期末考试
     */
    private Integer examType;

    /**
     * 是否乱序：0-否，1-是
     */
    private Integer isShuffle;

    /**
     * 是否显示答案：0-否，1-是
     */
    private Integer showAnswer;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
