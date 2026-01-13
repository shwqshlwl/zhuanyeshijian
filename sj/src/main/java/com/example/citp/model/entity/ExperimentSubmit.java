package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实验提交实体类
 */
@Data
@TableName("experiment_submit")
public class ExperimentSubmit implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long experimentId;

    private Long studentId;

    private String code;

    private String language;

    private LocalDateTime submitTime;

    /**
     * 状态：0-待评测，1-评测中，2-通过，3-未通过，4-编译错误，5-运行错误
     */
    private Integer status;

    private Integer score;

    private Integer passCount;

    private Integer totalCount;

    private Integer executeTime;

    private Integer memoryUsed;

    private String errorMessage;

    private String resultDetail;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
