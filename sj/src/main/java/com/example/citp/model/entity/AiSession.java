package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI会话实体类
 */
@Data
@TableName("ai_session")
public class AiSession implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String visitorId;

    private String title;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
