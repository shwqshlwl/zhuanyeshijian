package com.example.citp.controller;

import com.example.citp.common.Result;
import com.example.citp.model.dto.AiChatRequest;
import com.example.citp.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 助教接口")
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;

    @Operation(summary = "与 AI 对话")
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody AiChatRequest request) {
        String response = aiService.chat(request);
        return Result.success(response);
    }
}
