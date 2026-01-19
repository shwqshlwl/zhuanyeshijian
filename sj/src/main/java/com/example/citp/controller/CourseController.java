package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.CourseMapper;
import com.example.citp.mapper.SysUserMapper;
import com.example.citp.model.dto.CourseRequest;
import com.example.citp.model.entity.ClassEntity;
import com.example.citp.model.entity.Course;
import com.example.citp.model.entity.SysUser;
import com.example.citp.model.vo.CourseDetailVO;
import com.example.citp.model.vo.CourseStudentVO;
import com.example.citp.model.vo.CourseVO;
import com.example.citp.service.CourseClassService;
import com.example.citp.service.CourseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final CourseClassService courseClassService;
    private final CourseMapper courseMapper;
    private final SysUserMapper sysUserMapper;

    @GetMapping
    @Operation(summary = "分页查询课程列表")
    public Result<Page<CourseVO>> getCourseList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "my") String type) {
        SysUser currentUser = getCurrentUser();
        Page<CourseVO> page = courseService.getCourseList(pageNum, pageSize, keyword, status, type, currentUser.getId(), currentUser.getUserType());
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
        SysUser currentUser = getCurrentUser();

        // 检查权限
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 只有课程创建者或管理员可以编辑
        if (currentUser.getUserType() != 3 && !course.getTeacherId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权编辑此课程，只有课程创建者可以编辑");
        }

        courseService.updateCourse(id, request);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除课程")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        SysUser currentUser = getCurrentUser();

        // 检查权限
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 只有课程创建者或管理员可以删除
        if (currentUser.getUserType() != 3 && !course.getTeacherId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权删除此课程，只有课程创建者可以删除");
        }

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

    @GetMapping("/{id}/classes")
    @Operation(summary = "获取课程关联的班级列表")
    public Result<List<ClassEntity>> getCourseClasses(@PathVariable Long id) {
        List<ClassEntity> classes = courseClassService.getClassesByCourseId(id);
        return Result.success(classes);
    }

    @GetMapping("/{id}/students")
    @Operation(summary = "获取课程的选课学生列表")
    public Result<List<CourseStudentVO>> getCourseStudents(@PathVariable Long id) {
        List<CourseStudentVO> students = courseService.getCourseStudents(id);
        return Result.success(students);
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @Operation(summary = "移除课程的选课学生")
    public Result<Void> removeStudent(@PathVariable Long id, @PathVariable Long studentId) {
        SysUser currentUser = getCurrentUser();

        // 检查权限 - 只有课程创建者或管理员可以移除学生
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        if (currentUser.getUserType() != 3 && !course.getTeacherId().equals(currentUser.getId())) {
            throw new BusinessException(403, "无权操作，只有课程创建者或管理员可以移除学生");
        }

        courseService.removeStudent(id, studentId);
        return Result.successMsg("移除成功");
    }

    /**
     * 获取当前登录用户
     */
    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            throw new BusinessException(401, "未登录");
        }

        UserDetails userDetails = (UserDetails) principal;
        String username = userDetails.getUsername();

        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return user;
    }
}
