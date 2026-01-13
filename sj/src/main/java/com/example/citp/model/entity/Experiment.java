package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实验实体类
 */
@Data
@TableName("experiment")
public class Experiment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String experimentName;

    private Long courseId;

    private Long teacherId;

    private String description;

    private String requirement;

    private String templateCode;

    private String testCases;

    private String language;

    private Integer timeLimit;

    private Integer memoryLimit;

    private Integer totalScore;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 状态：0-草稿，1-已发布，2-已截止
     */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
