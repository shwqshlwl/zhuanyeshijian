package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.dto.SemesterRequest;
import com.example.citp.model.vo.SemesterVO;
import com.example.citp.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学期控制器
 */
@RestController
@RequestMapping("/semesters")
@RequiredArgsConstructor
@Tag(name = "学期管理", description = "学期相关接口")
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    @Operation(summary = "分页查询学期列表")
    public Result<Page<SemesterVO>> getSemesterList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<SemesterVO> page = semesterService.getSemesterList(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有学期（用于下拉选择）")
    public Result<List<SemesterVO>> getAllSemesters() {
        List<SemesterVO> semesters = semesterService.getAllSemesters();
        return Result.success(semesters);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前学期")
    public Result<SemesterVO> getCurrentSemester() {
        SemesterVO semester = semesterService.getCurrentSemester();
        return Result.success(semester);
    }

    @PostMapping
    @Operation(summary = "创建学期")
    public Result<Void> createSemester(@Valid @RequestBody SemesterRequest request) {
        semesterService.createSemester(request);
        return Result.successMsg("创建成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新学期")
    public Result<Void> updateSemester(@PathVariable Long id, @Valid @RequestBody SemesterRequest request) {
        semesterService.updateSemester(id, request);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除学期")
    public Result<Void> deleteSemester(@PathVariable Long id) {
        semesterService.deleteSemester(id);
        return Result.successMsg("删除成功");
    }

    @PutMapping("/{id}/set-current")
    @Operation(summary = "设置为当前学期")
    public Result<Void> setCurrentSemester(@PathVariable Long id) {
        semesterService.setCurrentSemester(id);
        return Result.successMsg("设置成功");
    }

    @PutMapping("/{id}/end")
    @Operation(summary = "结束学期")
    public Result<Void> endSemester(@PathVariable Long id) {
        semesterService.endSemester(id);
        return Result.successMsg("学期已结束");
    }
}
