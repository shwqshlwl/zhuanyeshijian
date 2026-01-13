package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.dto.ClassRequest;
import com.example.citp.model.vo.ClassDetailVO;
import com.example.citp.model.vo.ClassVO;
import com.example.citp.model.vo.UserInfoVO;
import com.example.citp.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 班级控制器
 */
@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
@Tag(name = "班级管理", description = "班级相关接口")
public class ClassController {

    private final ClassService classService;

    @GetMapping
    @Operation(summary = "分页查询班级列表")
    public Result<Page<ClassVO>> getClassList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String major) {
        Page<ClassVO> page = classService.getClassList(pageNum, pageSize, grade, major);
        return Result.success(page);
    }

    @GetMapping("/group-by-grade")
    @Operation(summary = "按年级分组获取班级列表")
    public Result<Map<String, List<ClassVO>>> getClassListGroupByGrade() {
        Map<String, List<ClassVO>> result = classService.getClassListGroupByGrade();
        return Result.success(result);
    }

    @PostMapping
    @Operation(summary = "创建班级")
    public Result<Void> createClass(@Valid @RequestBody ClassRequest request) {
        classService.createClass(request);
        return Result.successMsg("创建成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取班级详情（包含学生列表）")
    public Result<ClassDetailVO> getClassById(@PathVariable Long id) {
        ClassDetailVO classDetail = classService.getClassById(id);
        return Result.success(classDetail);
    }

    @GetMapping("/{id}/basic")
    @Operation(summary = "获取班级基本信息")
    public Result<ClassVO> getClassBasicInfo(@PathVariable Long id) {
        ClassVO classVO = classService.getClassBasicInfo(id);
        return Result.success(classVO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新班级")
    public Result<Void> updateClass(@PathVariable Long id, @Valid @RequestBody ClassRequest request) {
        classService.updateClass(id, request);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除班级")
    public Result<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return Result.successMsg("删除成功");
    }

    @PostMapping("/{classId}/students/{studentId}")
    @Operation(summary = "添加学生到班级（通过学生ID）")
    public Result<Void> addStudentToClass(@PathVariable Long classId, @PathVariable Long studentId) {
        classService.addStudentToClass(classId, studentId);
        return Result.successMsg("添加成功");
    }

    @PostMapping("/{classId}/students/by-student-no")
    @Operation(summary = "添加学生到班级（通过学号）")
    public Result<Void> addStudentToClassByStudentNo(@PathVariable Long classId, @RequestParam String studentNo) {
        classService.addStudentToClassByStudentNo(classId, studentNo);
        return Result.successMsg("添加成功");
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    @Operation(summary = "从班级移除学生")
    public Result<Void> removeStudentFromClass(@PathVariable Long classId, @PathVariable Long studentId) {
        classService.removeStudentFromClass(classId, studentId);
        return Result.successMsg("移除成功");
    }

    @GetMapping("/{classId}/students")
    @Operation(summary = "获取班级学生列表")
    public Result<Page<UserInfoVO>> getClassStudents(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<UserInfoVO> page = classService.getClassStudents(classId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "获取课程下的班级列表")
    public Result<List<ClassVO>> getClassesByCourseId(@PathVariable Long courseId) {
        List<ClassVO> classes = classService.getClassesByCourseId(courseId);
        return Result.success(classes);
    }

    @PutMapping("/{classId}/bindCourse/{courseId}")
    @Operation(summary = "关联课程到班级")
    public Result<Void> bindCourseToClass(@PathVariable Long classId, @PathVariable Long courseId) {
        classService.bindCourseToClass(classId, courseId);
        return Result.successMsg("关联成功");
    }
}
