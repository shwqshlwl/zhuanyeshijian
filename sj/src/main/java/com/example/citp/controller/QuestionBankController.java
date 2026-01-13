package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.dto.BatchImportQuestionRequest;
import com.example.citp.model.dto.QuestionRequest;
import com.example.citp.model.vo.BatchImportResultVO;
import com.example.citp.model.vo.QuestionVO;
import com.example.citp.service.QuestionBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 题库控制器
 */
@RestController
@RequestMapping("/question-bank")
@RequiredArgsConstructor
@Tag(name = "题库管理", description = "题库相关接口")
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    @GetMapping
    @Operation(summary = "分页查询题目列表")
    public Result<Page<QuestionVO>> getQuestionList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long questionTypeId,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword) {
        Page<QuestionVO> page = questionBankService.getQuestionList(pageNum, pageSize, courseId, 
                questionTypeId, difficulty, keyword);
        return Result.success(page);
    }

    @PostMapping
    @Operation(summary = "创建题目")
    public Result<Void> createQuestion(@Valid @RequestBody QuestionRequest request) {
        questionBankService.createQuestion(request);
        return Result.successMsg("创建成功");
    }

    @PostMapping("/batch-import")
    @Operation(summary = "批量导入题目")
    public Result<BatchImportResultVO> batchImportQuestions(@Valid @RequestBody BatchImportQuestionRequest request) {
        BatchImportResultVO result = questionBankService.batchImportQuestions(request);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取题目详情")
    public Result<QuestionVO> getQuestionById(@PathVariable Long id) {
        QuestionVO question = questionBankService.getQuestionById(id);
        return Result.success(question);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新题目")
    public Result<Void> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        questionBankService.updateQuestion(id, request);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionBankService.deleteQuestion(id);
        return Result.successMsg("删除成功");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除题目")
    public Result<Void> batchDeleteQuestions(@RequestBody List<Long> ids) {
        questionBankService.batchDeleteQuestions(ids);
        return Result.successMsg("删除成功");
    }

    @GetMapping("/random")
    @Operation(summary = "随机抽取题目")
    public Result<List<QuestionVO>> randomSelectQuestions(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long questionTypeId,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "10") Integer count) {
        List<QuestionVO> questions = questionBankService.randomSelectQuestions(courseId, 
                questionTypeId, difficulty, count);
        return Result.success(questions);
    }

    @GetMapping("/types")
    @Operation(summary = "获取题型列表")
    public Result<List<Map<String, Object>>> getQuestionTypes() {
        List<Map<String, Object>> types = questionBankService.getQuestionTypes();
        return Result.success(types);
    }

    @GetMapping("/stats")
    @Operation(summary = "按课程统计题目数量")
    public Result<Map<String, Object>> getQuestionStatsByCourse(
            @RequestParam(required = false) Long courseId) {
        Map<String, Object> stats = questionBankService.getQuestionStatsByCourse(courseId);
        return Result.success(stats);
    }

    @GetMapping("/stats/difficulty")
    @Operation(summary = "按难度统计题目数量")
    public Result<List<Map<String, Object>>> getQuestionStatsByDifficulty(
            @RequestParam(required = false) Long courseId) {
        List<Map<String, Object>> stats = questionBankService.getQuestionStatsByDifficulty(courseId);
        return Result.success(stats);
    }
}
