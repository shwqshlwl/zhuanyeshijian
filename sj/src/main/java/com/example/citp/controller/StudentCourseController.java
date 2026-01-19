package com.example.citp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.SysUserMapper;
import com.example.citp.model.entity.SysUser;
import com.example.citp.model.vo.ElectiveCourseVO;
import com.example.citp.service.StudentCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 学生选课控制器
 */
@RestController
@RequestMapping("/student-courses")
@RequiredArgsConstructor
@Tag(name = "学生选课", description = "学生选课相关接口")
public class StudentCourseController {

    private final StudentCourseService studentCourseService;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/elective")
    @Operation(summary = "获取选课中心课程列表")
    public Result<Page<ElectiveCourseVO>> getElectiveCourseList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        SysUser currentUser = getCurrentUser();

        // 只有学生才能访问选课中心
        if (currentUser.getUserType() != 1) {
            throw new BusinessException(403, "只有学生可以访问选课中心");
        }

        Page<ElectiveCourseVO> page = studentCourseService.getElectiveCourseList(pageNum, pageSize, keyword, currentUser.getId());
        return Result.success(page);
    }

    @PostMapping("/select/{courseId}")
    @Operation(summary = "选课")
    public Result<Void> selectCourse(@PathVariable Long courseId) {
        SysUser currentUser = getCurrentUser();

        // 只有学生才能选课
        if (currentUser.getUserType() != 1) {
            throw new BusinessException(403, "只有学生可以选课");
        }

        studentCourseService.selectCourse(courseId, currentUser.getId());
        return Result.successMsg("选课成功");
    }

    @DeleteMapping("/drop/{courseId}")
    @Operation(summary = "退课")
    public Result<Void> dropCourse(@PathVariable Long courseId) {
        SysUser currentUser = getCurrentUser();

        // 只有学生才能退课
        if (currentUser.getUserType() != 1) {
            throw new BusinessException(403, "只有学生可以退课");
        }

        studentCourseService.dropCourse(courseId, currentUser.getId());
        return Result.successMsg("退课成功");
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
