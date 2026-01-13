package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试记录实体类
 */
@Data
@TableName("exam_record")
public class ExamRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examId;

    private Long studentId;

    private Long paperId;

    private String answers;

    private BigDecimal score;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    /**
     * 状态：0-未开始，1-答题中，2-已提交
     */
    private Integer status;

    /**
     * 是否超时：0-否，1-是
     */
    private Integer isTimeout;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
