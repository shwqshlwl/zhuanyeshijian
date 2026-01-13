package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.dto.ExperimentRequest;
import com.example.citp.model.dto.ExperimentSubmitRequest;
import com.example.citp.model.vo.ExperimentResultVO;
import com.example.citp.model.vo.ExperimentVO;
import com.example.citp.service.ExperimentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 实验控制器
 */
@RestController
@RequestMapping("/experiments")
@RequiredArgsConstructor
@Tag(name = "实验管理", description = "实验相关接口")
public class ExperimentController {

    private final ExperimentService experimentService;

    @GetMapping
    @Operation(summary = "分页查询实验列表")
    public Result<Page<ExperimentVO>> getExperimentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId) {
        Page<ExperimentVO> page = experimentService.getExperimentList(pageNum, pageSize, courseId);
        return Result.success(page);
    }

    @PostMapping
    @Operation(summary = "创建实验")
    public Result<Void> createExperiment(@Valid @RequestBody ExperimentRequest request) {
        experimentService.createExperiment(request);
        return Result.successMsg("创建成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取实验详情")
    public Result<ExperimentVO> getExperimentById(@PathVariable Long id) {
        ExperimentVO experiment = experimentService.getExperimentById(id);
        return Result.success(experiment);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新实验")
    public Result<Void> updateExperiment(@PathVariable Long id, @Valid @RequestBody ExperimentRequest request) {
        experimentService.updateExperiment(id, request);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除实验")
    public Result<Void> deleteExperiment(@PathVariable Long id) {
        experimentService.deleteExperiment(id);
        return Result.successMsg("删除成功");
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交实验代码")
    public Result<Void> submitExperiment(@PathVariable Long id, @Valid @RequestBody ExperimentSubmitRequest request) {
        experimentService.submitExperiment(id, request);
        return Result.successMsg("提交成功");
    }

    @GetMapping("/{id}/result")
    @Operation(summary = "获取实验结果")
    public Result<ExperimentResultVO> getExperimentResult(@PathVariable Long id) {
        ExperimentResultVO result = experimentService.getExperimentResult(id);
        return Result.success(result);
    }
}
