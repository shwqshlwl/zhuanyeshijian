package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.dto.CourseRequest;
import com.example.citp.model.vo.CourseDetailVO;
import com.example.citp.model.vo.CourseVO;
import com.example.citp.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Tag(name = "课程管理", description = "课程相关接口")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "分页查询课程列表")
    public Result<Page<CourseVO>> getCourseList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<CourseVO> page = courseService.getCourseList(pageNum, pageSize, keyword, status);
        return Result.success(page);
    }

    @PostMapping
    @Operation(summary = "创建课程")
    public Result<Void> createCourse(@Valid @RequestBody CourseRequest request) {
        courseService.createCourse(request);
        return Result.successMsg("创建成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情（包含关联信息）")
    public Result<CourseDetailVO> getCourseById(@PathVariable Long id) {
        CourseDetailVO course = courseService.getCourseById(id);
        return Result.success(course);
    }

    @GetMapping("/{id}/basic")
    @Operation(summary = "获取课程基本信息")
    public Result<CourseVO> getCourseBasicInfo(@PathVariable Long id) {
        CourseVO course = courseService.getCourseBasicInfo(id);
        return Result.success(course);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新课程")
    public Result<Void> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        courseService.updateCourse(id, request);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除课程")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.successMsg("删除成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新课程状态")
    public Result<Void> updateCourseStatus(@PathVariable Long id, @RequestParam Integer status) {
        courseService.updateCourseStatus(id, status);
        return Result.successMsg("状态更新成功");
    }

    @GetMapping("/teacher/my")
    @Operation(summary = "获取当前教师的课程列表")
    public Result<List<CourseVO>> getTeacherCourses() {
        List<CourseVO> courses = courseService.getTeacherCourses();
        return Result.success(courses);
    }

    @GetMapping("/student/my")
    @Operation(summary = "获取当前学生的课程列表")
    public Result<List<CourseVO>> getStudentCourses() {
        List<CourseVO> courses = courseService.getStudentCourses();
        return Result.success(courses);
    }
}
