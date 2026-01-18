package com.example.citp.model.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    /**
     * 会话ID (可选，如果为空则创建新会话)
     */
    private Long sessionId;
    
    /**
     * 访客ID (可选，未登录用户使用)
     */
    private String visitorId;

    /**
     * 用户输入的消息
     */
    private String message;

    /**
     * 系统提示词（可选）
     */
    private String systemPrompt;
}
