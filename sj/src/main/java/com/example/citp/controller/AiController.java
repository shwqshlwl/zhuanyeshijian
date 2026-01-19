package com.example.citp.controller;

import com.example.citp.common.Result;
import com.example.citp.model.dto.AiChatRequest;
import com.example.citp.model.vo.AiChatResponse;
import com.example.citp.model.vo.AiMessageVO;
import com.example.citp.model.vo.AiSessionVO;
import com.example.citp.service.AiService;
import com.example.citp.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;

import java.util.List;

@Tag(name = "AI 助教接口")
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;

    @Operation(summary = "与 AI 对话")
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        AiChatResponse response = aiService.chat(request);
        return Result.success(response);
    }

    @Operation(summary = "与 AI 对话 (流式)")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody AiChatRequest request) {
        return aiService.streamChat(request);
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public Result<List<AiSessionVO>> getSessionList(@RequestParam(required = false) String visitorId) {
        Long userId = null;
        try {
            userId = SecurityUtils.getUserId();
        } catch (Exception e) {
            // ignore
        }
        List<AiSessionVO> list = aiService.getSessionList(userId, visitorId);
        return Result.success(list);
    }

    @Operation(summary = "获取会话详情")
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<AiMessageVO>> getSessionMessages(@PathVariable Long sessionId) {
        List<AiMessageVO> list = aiService.getSessionMessages(sessionId);
        return Result.success(list);
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        aiService.deleteSession(sessionId);
        return Result.success(null);
    }
}
