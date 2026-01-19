package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.request.CourseOfferingRequest;
import com.example.citp.model.vo.CourseOfferingVO;
import com.example.citp.model.vo.CourseTemplateVO;
import com.example.citp.service.CourseOfferingService;
import com.example.citp.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 开课实例控制器
 */
@RestController
@RequestMapping("/course-offerings")
@RequiredArgsConstructor
@Tag(name = "开课实例管理", description = "开课实例相关接口")
public class CourseOfferingController {

    private final CourseOfferingService courseOfferingService;

    @GetMapping("/teacher/templates")
    @Operation(summary = "查询教师的课程模板列表（含指定学期开课状态）")
    public Result<Page<CourseTemplateVO>> getTeacherCourseTemplates(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Long teacherId = SecurityUtils.getUserId();
        Page<CourseTemplateVO> page = courseOfferingService.getTeacherCourseTemplates(
                teacherId, semesterId, pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PostMapping
    @Operation(summary = "创建开课实例")
    public Result<Void> createOffering(@Valid @RequestBody CourseOfferingRequest request) {
        Long currentUserId = SecurityUtils.getUserId();
        courseOfferingService.createOffering(request, currentUserId);
        return Result.successMsg("开课成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取开课实例详情")
    public Result<CourseOfferingVO> getOfferingDetail(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getUserId();
        CourseOfferingVO offering = courseOfferingService.getOfferingDetail(id, currentUserId);
        return Result.success(offering);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新开课实例")
    public Result<Void> updateOffering(@PathVariable Long id, @Valid @RequestBody CourseOfferingRequest request) {
        Long currentUserId = SecurityUtils.getUserId();
        courseOfferingService.updateOffering(id, request, currentUserId);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除开课实例")
    public Result<Void> deleteOffering(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getUserId();
        courseOfferingService.deleteOffering(id, currentUserId);
        return Result.successMsg("删除成功");
    }

    @GetMapping("/semester/{semesterId}")
    @Operation(summary = "查询学期的开课实例列表")
    public Result<Page<CourseOfferingVO>> getOfferingsBySemester(
            @PathVariable Long semesterId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<CourseOfferingVO> page = courseOfferingService.getOfferingsBySemester(
                semesterId, pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @GetMapping("/teacher/offerings")
    @Operation(summary = "查询教师的开课实例列表")
    public Result<Page<CourseOfferingVO>> getTeacherOfferings(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Long teacherId = SecurityUtils.getUserId();
        Page<CourseOfferingVO> page = courseOfferingService.getTeacherOfferings(
                teacherId, semesterId, pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @GetMapping("/check")
    @Operation(summary = "检查课程在指定学期是否已开课")
    public Result<Boolean> isOfferedInSemester(
            @RequestParam Long courseId,
            @RequestParam Long semesterId) {
        boolean isOffered = courseOfferingService.isOfferedInSemester(courseId, semesterId);
        return Result.success(isOffered);
    }
}
