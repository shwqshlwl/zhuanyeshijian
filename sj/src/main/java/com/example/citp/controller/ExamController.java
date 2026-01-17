package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.dto.ExamRequest;
import com.example.citp.model.dto.ExamStartResponseDTO;
import com.example.citp.model.dto.ExamSubmitRequest;
import com.example.citp.model.vo.ExamVO;
import com.example.citp.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 考试控制器
 */
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
@Tag(name = "考试管理", description = "考试相关接口")
public class ExamController {

    private final ExamService examService;

    @GetMapping
    @Operation(summary = "分页查询考试列表")
    public Result<Page<ExamVO>> getExamList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer status) {
        Page<ExamVO> page = examService.getExamList(pageNum, pageSize, courseId, status);
        return Result.success(page);
    }

    @PostMapping
    @Operation(summary = "创建考试")
    public Result<Void> createExam(@Valid @RequestBody ExamRequest request) {
        examService.createExam(request);
        return Result.successMsg("创建成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取考试详情")
    public Result<ExamVO> getExamById(@PathVariable Long id) {
        ExamVO exam = examService.getExamById(id);
        return Result.success(exam);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新考试")
    public Result<Void> updateExam(@PathVariable Long id, @Valid @RequestBody ExamRequest request) {
        examService.updateExam(id, request);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除考试")
    public Result<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return Result.successMsg("删除成功");
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始考试")
    public Result<ExamStartResponseDTO> startExam(@PathVariable Long id) {
        ExamStartResponseDTO result = examService.startExam(id);
        return Result.success(result);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交考试")
    public Result<Void> submitExam(@PathVariable Long id, @Valid @RequestBody ExamSubmitRequest request) {
        examService.submitExam(id, request);
        return Result.successMsg("提交成功");
    }

    @GetMapping("/{id}/analysis")
    @Operation(summary = "考试结果分析")
    public Result<Map<String, Object>> getExamAnalysis(@PathVariable Long id) {
        Map<String, Object> analysis = examService.getExamAnalysis(id);
        return Result.success(analysis);
    }

    @GetMapping("/{id}/questions")
    @Operation(summary = "获取考试题目列表")
    public Result<java.util.List<Map<String, Object>>> getExamQuestions(@PathVariable Long id) {
        java.util.List<Map<String, Object>> questions = examService.getExamQuestions(id);
        return Result.success(questions);
    }

    @PostMapping("/{id}/questions")
    @Operation(summary = "添加题目到考试")
    public Result<Void> addQuestionsToExam(@PathVariable Long id, @RequestBody java.util.List<Map<String, Object>> questions) {
        examService.addQuestionsToExam(id, questions);
        return Result.successMsg("添加成功");
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    @Operation(summary = "从考试中移除题目")
    public Result<Void> removeQuestionFromExam(@PathVariable Long id, @PathVariable Long questionId) {
        examService.removeQuestionFromExam(id, questionId);
        return Result.successMsg("移除成功");
    }

    @GetMapping("/{id}/records")
    @Operation(summary = "获取考试记录列表")
    public Result<Map<String, Object>> getExamRecords(@PathVariable Long id) {
        Map<String, Object> records = examService.getExamRecords(id);
        return Result.success(records);
    }

    @GetMapping("/{id}/records/{studentId}")
    @Operation(summary = "获取学生单条答卷详情")
    public Result<java.util.Map<String, Object>> getStudentRecord(@PathVariable Long id, @PathVariable Long studentId) {
        java.util.Map<String, Object> record = examService.getStudentRecord(id, studentId);
        return Result.success(record);
    }

    @GetMapping("/student/my")
    @Operation(summary = "获取学生的考试列表")
    public Result<java.util.List<Map<String, Object>>> getStudentExams() {
        java.util.List<Map<String, Object>> exams = examService.getStudentExams();
        return Result.success(exams);
    }

    @GetMapping("/test")
    @Operation(summary = "测试API")
    public Result<String> testApi() {
        return Result.success("API测试成功");
    }
}
