package com.example.citp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.citp.exception.BusinessException;
import com.example.citp.model.dto.AiChatRequest;
import com.example.citp.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    private static final String API_KEY = "sk-6680d02c195d408eb022eb55e266e0bc";
    // 注意：兼容模式的 URL 通常以 /chat/completions 结尾
    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String MODEL = "qwen3-235b-a22b-thinking-2507";

    @Override
    public String chat(AiChatRequest request) {
        if (StrUtil.isBlank(request.getMessage())) {
            throw new BusinessException("消息不能为空");
        }

        // 构建请求体
        JSONObject body = new JSONObject();
        body.set("model", MODEL);
        
        JSONArray messages = new JSONArray();
        
        // 添加系统提示词
        if (StrUtil.isNotBlank(request.getSystemPrompt())) {
            JSONObject systemMsg = new JSONObject();
            systemMsg.set("role", "system");
            systemMsg.set("content", request.getSystemPrompt());
            messages.add(systemMsg);
        } else {
            // 默认系统提示
            JSONObject systemMsg = new JSONObject();
            systemMsg.set("role", "system");
            systemMsg.set("content", "你是一个乐于助人的课程AI助教，可以帮助学生解答编程问题、分析作业题目等。");
            messages.add(systemMsg);
        }

        // TODO: 处理历史消息 (如果前端传了 history 且格式正确，可以在这里解析并添加)
        // 目前简化处理，只发送当前消息
        
        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", request.getMessage());
        messages.add(userMsg);

        body.set("messages", messages);
        
        // 其他可选参数
        // body.set("temperature", 0.7);

        log.info("AI Request Body: {}", body.toString());

        try {
            HttpResponse response = HttpRequest.post(BASE_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(60000) // 60秒超时
                    .execute();

            String resBody = response.body();
            log.info("AI Response Body: {}", resBody);

            if (!response.isOk()) {
                log.error("AI API Error: status={}, body={}", response.getStatus(), resBody);
                throw new BusinessException("AI 服务响应异常: " + response.getStatus());
            }

            // 解析响应
            JSONObject jsonRes = JSONUtil.parseObj(resBody);
            
            // 检查是否有错误
            if (jsonRes.containsKey("error")) {
                JSONObject error = jsonRes.getJSONObject("error");
                throw new BusinessException("AI 服务错误: " + error.getStr("message"));
            }
            
            JSONArray choices = jsonRes.getJSONArray("choices");
            if (CollUtil.isEmpty(choices)) {
                return "AI 没有返回任何内容";
            }
            
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            return message.getStr("content");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 AI 接口失败", e);
            throw new BusinessException("调用 AI 接口失败: " + e.getMessage());
        }
    }
}
