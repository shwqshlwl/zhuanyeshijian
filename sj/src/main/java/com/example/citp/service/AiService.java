package com.example.citp.service;

import com.example.citp.model.dto.AiChatRequest;
import com.example.citp.model.vo.AiChatResponse;
import com.example.citp.model.vo.AiMessageVO;
import com.example.citp.model.vo.AiSessionVO;

import java.util.List;

public interface AiService {
    /**
     * 发送消息给 AI
     * @param request 请求参数
     * @return AI 的回复响应
     */
    AiChatResponse chat(AiChatRequest request);

    /**
     * 获取会话列表
     * @param userId 用户ID (登录用户)
     * @param visitorId 访客ID (未登录用户)
     * @return 会话列表
     */
    List<AiSessionVO> getSessionList(Long userId, String visitorId);

    /**
     * 获取会话详情（消息列表）
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<AiMessageVO> getSessionMessages(Long sessionId);

    /**
     * 删除会话
     * @param sessionId 会话ID
     */
    void deleteSession(Long sessionId);
}
