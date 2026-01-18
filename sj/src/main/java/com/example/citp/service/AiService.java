package com.example.citp.service;

import com.example.citp.model.dto.AiChatRequest;

public interface AiService {
    /**
     * 发送消息给 AI
     * @param request 请求参数
     * @return AI 的回复
     */
    String chat(AiChatRequest request);
}
