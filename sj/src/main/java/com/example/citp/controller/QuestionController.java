package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.dto.QuestionRequest;
import com.example.citp.model.entity.QuestionType;
import com.example.citp.model.vo.QuestionVO;
import com.example.citp.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题库控制器
 */
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Tag(name = "题库管理", description = "题库相关接口")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/types")
    @Operation(summary = "获取所有题型")
    public Result<List<QuestionType>> getAllQuestionTypes() {
        List<QuestionType> types = questionService.getAllQuestionTypes();
        return Result.success(types);
    }

    @GetMapping
    @Operation(summary = "分页查询题目列表")
    public Result<Page<QuestionVO>> getQuestionList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long questionTypeId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer difficulty) {
        Page<QuestionVO> page = questionService.getQuestionList(pageNum, pageSize, questionTypeId, courseId, difficulty);
        return Result.success(page);
    }

    @PostMapping
    @Operation(summary = "创建题目")
    public Result<Void> createQuestion(@Valid @RequestBody QuestionRequest request) {
        questionService.createQuestion(request);
        return Result.successMsg("创建成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取题目详情")
    public Result<QuestionVO> getQuestionById(@PathVariable Long id) {
        QuestionVO question = questionService.getQuestionById(id);
        return Result.success(question);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新题目")
    public Result<Void> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        questionService.updateQuestion(id, request);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.successMsg("删除成功");
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除题目")
    public Result<Void> batchDeleteQuestions(@RequestBody List<Long> ids) {
        questionService.batchDeleteQuestions(ids);
        return Result.successMsg("删除成功");
    }
}
