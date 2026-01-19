package com.example.citp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.AiMessageMapper;
import com.example.citp.mapper.AiSessionMapper;
import com.example.citp.model.dto.AiChatRequest;
import com.example.citp.model.entity.AiMessage;
import com.example.citp.model.entity.AiSession;
import com.example.citp.model.vo.AiChatResponse;
import com.example.citp.model.vo.AiMessageVO;
import com.example.citp.model.vo.AiSessionVO;
import com.example.citp.service.AiService;
import com.example.citp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;

    private static final String API_KEY = "sk-6680d02c195d408eb022eb55e266e0bc";
    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String MODEL = "qwen3-235b-a22b-thinking-2507";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatResponse chat(AiChatRequest request) {
        if (StrUtil.isBlank(request.getMessage())) {
            throw new BusinessException("消息不能为空");
        }

        Long sessionId = request.getSessionId();
        AiSession session;
        Long userId = null;
        try {
            userId = SecurityUtils.getUserId();
        } catch (Exception e) {
            // ignore if not logged in
        }

        if (sessionId != null) {
            session = aiSessionMapper.selectById(sessionId);
            if (session == null) {
                throw new BusinessException("会话不存在");
            }
            // 简单的权限校验
            if (userId != null && session.getUserId() != null && !userId.equals(session.getUserId())) {
                 // 如果登录了，但会话属于别人
                 throw new BusinessException("无权访问该会话");
            }
            // 如果未登录，且会话有visitorId，应校验visitorId (此处略简化，假设visitorId匹配)
        } else {
            // 创建新会话
            session = new AiSession();
            if (userId != null) {
                session.setUserId(userId);
            } else {
                session.setVisitorId(request.getVisitorId());
            }
            // 标题取消息前20个字
            String title = StrUtil.subPre(request.getMessage(), 20);
            session.setTitle(title);
            aiSessionMapper.insert(session);
            sessionId = session.getId();
        }

        // 1. 保存用户消息
        AiMessage userMsg = new AiMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(request.getMessage());
        aiMessageMapper.insert(userMsg);

        // 2. 构建上下文
        // 获取最近的N条消息作为上下文
        LambdaQueryWrapper<AiMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiMessage::getSessionId, sessionId)
                .orderByAsc(AiMessage::getCreateTime); // 按时间正序
        List<AiMessage> historyMessages = aiMessageMapper.selectList(queryWrapper);

        // 构建请求体
        JSONObject body = new JSONObject();
        body.set("model", MODEL);
        JSONArray messages = new JSONArray();

        // 添加系统提示词
        JSONObject systemMsg = new JSONObject();
        systemMsg.set("role", "system");
        if (StrUtil.isNotBlank(request.getSystemPrompt())) {
            systemMsg.set("content", request.getSystemPrompt());
        } else {
            systemMsg.set("content", "你是一个乐于助人的课程AI助教，可以帮助学生解答编程问题、分析作业题目等。");
        }
        messages.add(systemMsg);

        // 添加历史消息
        for (AiMessage msg : historyMessages) {
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.set("role", msg.getRole());
            jsonMsg.set("content", msg.getContent());
            messages.add(jsonMsg);
        }

        body.set("messages", messages);
        log.info("AI Request Body: {}", body.toString());

        // 3. 调用 API
        String aiContent = "";
        try {
            HttpResponse response = HttpRequest.post(BASE_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(60000)
                    .execute();

            String resBody = response.body();
            log.info("AI Response Body: {}", resBody);

            if (!response.isOk()) {
                throw new BusinessException("AI 服务响应异常: " + response.getStatus());
            }

            JSONObject jsonRes = JSONUtil.parseObj(resBody);
            if (jsonRes.containsKey("error")) {
                JSONObject error = jsonRes.getJSONObject("error");
                throw new BusinessException("AI 服务错误: " + error.getStr("message"));
            }

            JSONArray choices = jsonRes.getJSONArray("choices");
            if (CollUtil.isEmpty(choices)) {
                aiContent = "AI 没有返回任何内容";
            } else {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                aiContent = message.getStr("content");
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 AI 接口失败", e);
            throw new BusinessException("调用 AI 接口失败: " + e.getMessage());
        }

        // 4. 保存 AI 回复
        AiMessage assistantMsg = new AiMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(aiContent);
        aiMessageMapper.insert(assistantMsg);

        return AiChatResponse.builder()
                .response(aiContent)
                .sessionId(sessionId)
                .title(session.getTitle())
                .build();
    }

    @Override
    public SseEmitter streamChat(AiChatRequest request) {
        if (StrUtil.isBlank(request.getMessage())) {
            throw new BusinessException("消息不能为空");
        }

        Long sessionId = request.getSessionId();
        AiSession session;
        Long userId = null;
        try {
            userId = SecurityUtils.getUserId();
        } catch (Exception e) {
            // ignore
        }

        if (sessionId != null) {
            session = aiSessionMapper.selectById(sessionId);
            if (session == null) {
                throw new BusinessException("会话不存在");
            }
            if (userId != null && session.getUserId() != null && !userId.equals(session.getUserId())) {
                throw new BusinessException("无权访问该会话");
            }
        } else {
            session = new AiSession();
            if (userId != null) {
                session.setUserId(userId);
            } else {
                session.setVisitorId(request.getVisitorId());
            }
            String title = StrUtil.subPre(request.getMessage(), 20);
            session.setTitle(title);
            aiSessionMapper.insert(session);
            sessionId = session.getId();
        }

        // 1. 保存用户消息
        AiMessage userMsg = new AiMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(request.getMessage());
        aiMessageMapper.insert(userMsg);

        // 2. 构建上下文
        LambdaQueryWrapper<AiMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiMessage::getSessionId, sessionId)
                .orderByAsc(AiMessage::getCreateTime);
        List<AiMessage> historyMessages = aiMessageMapper.selectList(queryWrapper);

        // 构建请求体
        JSONObject body = new JSONObject();
        body.set("model", MODEL);
        body.set("stream", true);
        JSONArray messages = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.set("role", "system");
        if (StrUtil.isNotBlank(request.getSystemPrompt())) {
            systemMsg.set("content", request.getSystemPrompt());
        } else {
            systemMsg.set("content", "你是一个乐于助人的课程AI助教，可以帮助学生解答编程问题、分析作业题目等。");
        }
        messages.add(systemMsg);

        for (AiMessage msg : historyMessages) {
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.set("role", msg.getRole());
            jsonMsg.set("content", msg.getContent());
            messages.add(jsonMsg);
        }
        body.set("messages", messages);

        SseEmitter emitter = new SseEmitter(0L); // 无超时限制
        Long finalSessionId = sessionId;

        CompletableFuture.runAsync(() -> {
            StringBuilder fullContent = new StringBuilder();
            try {
                HttpClient client = HttpClient.newHttpClient();
                java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(BASE_URL))
                        .header("Authorization", "Bearer " + API_KEY)
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();

                java.net.http.HttpResponse<InputStream> response = client.send(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    try (InputStream is = response.body()) {
                        String err = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        log.error("AI Stream Error: " + err);
                        emitter.send(SseEmitter.event().name("error").data("AI服务异常: " + response.statusCode()));
                    }
                    emitter.complete();
                    return;
                }

                // 发送会话元数据
                JSONObject meta = new JSONObject();
                meta.set("sessionId", finalSessionId);
                meta.set("title", session.getTitle());
                emitter.send(SseEmitter.event().name("meta").data(meta.toString()));

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (StrUtil.isBlank(line)) continue;
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            if ("[DONE]".equals(data)) break;

                            try {
                                JSONObject json = JSONUtil.parseObj(data);
                                if (json.containsKey("choices")) {
                                    JSONArray choices = json.getJSONArray("choices");
                                    if (CollUtil.isNotEmpty(choices)) {
                                        JSONObject choice = choices.getJSONObject(0);
                                        if (choice.containsKey("delta")) {
                                            String content = choice.getJSONObject("delta").getStr("content");
                                            if (content != null) {
                                                fullContent.append(content);
                                                emitter.send(SseEmitter.event().data(content));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("Parse stream error: " + line);
                            }
                        }
                    }
                }

                // 保存 AI 回复
                if (fullContent.length() > 0) {
                    AiMessage assistantMsg = new AiMessage();
                    assistantMsg.setSessionId(finalSessionId);
                    assistantMsg.setRole("assistant");
                    assistantMsg.setContent(fullContent.toString());
                    aiMessageMapper.insert(assistantMsg);
                }

                emitter.complete();

            } catch (Exception e) {
                log.error("Stream Chat Error", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @Override
    public List<AiSessionVO> getSessionList(Long userId, String visitorId) {
        LambdaQueryWrapper<AiSession> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(AiSession::getUserId, userId);
        } else if (StrUtil.isNotBlank(visitorId)) {
            queryWrapper.eq(AiSession::getVisitorId, visitorId);
        } else {
            return new ArrayList<>();
        }
        queryWrapper.orderByDesc(AiSession::getUpdateTime);
        
        List<AiSession> sessions = aiSessionMapper.selectList(queryWrapper);
        return sessions.stream().map(session -> {
            AiSessionVO vo = new AiSessionVO();
            BeanUtils.copyProperties(session, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AiMessageVO> getSessionMessages(Long sessionId) {
        // 简单校验
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        // 这里可以加权限校验

        LambdaQueryWrapper<AiMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiMessage::getSessionId, sessionId)
                .orderByAsc(AiMessage::getCreateTime);
        
        List<AiMessage> messages = aiMessageMapper.selectList(queryWrapper);
        return messages.stream().map(msg -> {
            AiMessageVO vo = new AiMessageVO();
            BeanUtils.copyProperties(msg, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        aiSessionMapper.deleteById(sessionId);
        // 逻辑删除消息
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getSessionId, sessionId);
        aiMessageMapper.delete(wrapper);
    }
}
