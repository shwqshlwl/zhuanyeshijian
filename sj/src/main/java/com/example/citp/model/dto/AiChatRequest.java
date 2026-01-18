package com.example.citp.model.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    /**
     * 用户输入的消息
     */
    private String message;

    /**
     * 会话历史（可选，用于上下文）
     * 格式暂定为 JSON 字符串或简化处理
     */
    private String history;
    
    /**
     * 系统提示词（可选）
     */
    private String systemPrompt;
}
