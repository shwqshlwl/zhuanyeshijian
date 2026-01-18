package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI消息实体类
 */
@Data
@TableName("ai_message")
public class AiMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    /**
     * 角色：user, assistant, system
     */
    private String role;

    private String content;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
