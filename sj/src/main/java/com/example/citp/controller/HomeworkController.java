package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.dto.HomeworkGradeRequest;
import com.example.citp.model.dto.HomeworkRequest;
import com.example.citp.model.dto.HomeworkSubmitRequest;
import com.example.citp.model.vo.HomeworkDetailVO;
import com.example.citp.model.vo.HomeworkListDetailVO;
import com.example.citp.model.vo.HomeworkSubmitVO;
import com.example.citp.model.vo.HomeworkVO;
import com.example.citp.service.HomeworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 作业控制器
 */
@RestController
@RequestMapping("/homeworks")
@RequiredArgsConstructor
@Tag(name = "作业管理", description = "作业相关接口")
public class HomeworkController {

    private final HomeworkService homeworkService;

    @GetMapping
    @Operation(summary = "分页查询作业列表")
    public Result<Page<HomeworkVO>> getHomeworkList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Integer status) {
        Page<HomeworkVO> page = homeworkService.getHomeworkList(pageNum, pageSize, courseId, classId, status);
        return Result.success(page);
    }

    @GetMapping("/listDetailSubmit")
    @Operation(summary = "分页查询带有提交信息的作业列表")
    public Result<Page<HomeworkListDetailVO>> getHomeworkDetailList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Integer status) {
        System.out.println(courseId);
        System.out.println(classId);
        System.out.println(status);
        Page<HomeworkListDetailVO> page = homeworkService.getHomeworkListDetail(pageNum, pageSize, courseId, classId, status);
        return Result.success(page);
    }

    @PostMapping("/refresh-status")
    public Result<Void> refreshStatus() {
//        System.out.println("刷新数据库作业状态");
        homeworkService.refreshHomeworkStatus();
        return Result.success();
    }
    @PostMapping
    @Operation(summary = "创建作业")
    public Result<Void> createHomework(@Valid @RequestBody HomeworkRequest request) {
//        System.out.println(request);
        homeworkService.createHomework(request);
        homeworkService.refreshHomeworkStatus();
        return Result.successMsg("创建成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取作业详情（包含提交统计）")
    public Result<HomeworkDetailVO> getHomeworkById(@PathVariable Long id) {
        HomeworkDetailVO homework = homeworkService.getHomeworkById(id);
        return Result.success(homework);
    }

    @GetMapping("/{id}/basic")
    @Operation(summary = "获取作业基本信息")
    public Result<HomeworkVO> getHomeworkBasicInfo(@PathVariable Long id) {
        HomeworkVO homework = homeworkService.getHomeworkBasicInfo(id);
        return Result.success(homework);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新作业")
    public Result<Void> updateHomework(@PathVariable Long id, @Valid @RequestBody HomeworkRequest request) {
        homeworkService.updateHomework(id, request);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除作业")
    public Result<Void> deleteHomework(@PathVariable Long id) {
        homeworkService.deleteHomework(id);
        return Result.successMsg("删除成功");
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交作业")
    public Result<Void> submitHomework(@PathVariable Long id, @Valid @RequestBody HomeworkSubmitRequest request) {
        homeworkService.submitHomework(id, request);
        return Result.successMsg("提交成功");
    }

    @GetMapping("/{id}/submissions")
    @Operation(summary = "获取作业提交列表")
    public Result<Page<HomeworkSubmitVO>> getHomeworkSubmissions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Page<HomeworkSubmitVO> page = homeworkService.getHomeworkSubmissions(id, pageNum, pageSize, status);
        return Result.success(page);
    }

    @GetMapping("/{homeworkId}/submissions/{studentId}")
    @Operation(summary = "获取学生的作业提交")
    public Result<HomeworkSubmitVO> getStudentSubmission(
            @PathVariable Long homeworkId,
            @PathVariable Long studentId) {
        HomeworkSubmitVO submission = homeworkService.getStudentSubmission(homeworkId, studentId);
        return Result.success(submission);
    }

    @PutMapping("/{id}/grade")
    @Operation(summary = "批改作业")
    public Result<Void> gradeHomework(
            @PathVariable Long id,
            @RequestParam Long studentId,
            @Valid @RequestBody HomeworkGradeRequest request) {
        homeworkService.gradeHomework(id, studentId, request);
        return Result.successMsg("批改成功");
    }

    @PostMapping("/{id}/ai-analyze")
    @Operation(summary = "AI 解析题目")
    public Result<String> aiAnalyzeHomework(@PathVariable Long id) {
        String result = homeworkService.aiAnalyzeHomework(id);
        return Result.success(result);
    }

    @GetMapping("/student/my")
    @Operation(summary = "获取学生的作业列表")
    public Result<Page<HomeworkVO>> getStudentHomeworks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer status) {
        Page<HomeworkVO> page = homeworkService.getStudentHomeworks(pageNum, pageSize, courseId, status);
        return Result.success(page);
    }
}
