package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.dto.*;
import com.example.citp.model.vo.BatchImportResultVO;
import com.example.citp.model.vo.LoginResponse;
import com.example.citp.model.vo.UserInfoVO;
import com.example.citp.model.vo.AdminStatisticsVO;
import com.example.citp.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class SysUserController {

    private final SysUserService sysUserService;

    @PostMapping("/auth/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = sysUserService.login(request);
        return Result.success(response);
    }

    @PostMapping("/auth/register/teacher")
    @Operation(summary = "教师注册")
    public Result<Void> registerTeacher(@Valid @RequestBody TeacherRegisterRequest request) {
        sysUserService.registerTeacher(request);
        return Result.successMsg("注册成功");
    }

    @GetMapping("/user/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfoVO> getCurrentUserInfo() {
        UserInfoVO userInfo = sysUserService.getCurrentUserInfo();
        return Result.success(userInfo);
    }

    @PutMapping("/user/info")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateUserInfo(@RequestBody UserUpdateRequest request) {
        sysUserService.updateUserInfo(request);
        return Result.successMsg("更新成功");
    }

    @PutMapping("/user/password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        sysUserService.changePassword(request);
        return Result.successMsg("密码修改成功");
    }

    @PostMapping("/user/batch-import-students")
    @Operation(summary = "批量导入学生")
    public Result<BatchImportResultVO> batchImportStudents(@Valid @RequestBody BatchImportStudentRequest request) {
        BatchImportResultVO result = sysUserService.batchImportStudents(request);
        return Result.success(result);
    }

    @GetMapping("/user/students")
    @Operation(summary = "获取所有学生列表")
    public Result<Page<UserInfoVO>> getAllStudents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<UserInfoVO> page = sysUserService.getAllStudents(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @GetMapping("/user/students/class/{classId}")
    @Operation(summary = "获取班级学生列表")
    public Result<Page<UserInfoVO>> getStudentsByClassId(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<UserInfoVO> page = sysUserService.getStudentsByClassId(classId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/user/student/{studentNo}")
    @Operation(summary = "根据学号查询学生")
    public Result<UserInfoVO> getStudentByStudentNo(@PathVariable String studentNo) {
        UserInfoVO student = sysUserService.getStudentByStudentNo(studentNo);
        return Result.success(student);
    }

    // ==================== 管理员专用接口 ====================

    @GetMapping("/admin/users")
    @Operation(summary = "获取所有用户列表（管理员专用）")
    public Result<Page<UserInfoVO>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType) {
        Page<UserInfoVO> page = sysUserService.getAllUsers(pageNum, pageSize, keyword, userType);
        return Result.success(page);
    }

    @PutMapping("/admin/users/{id}/status")
    @Operation(summary = "更新用户状态（管理员专用）")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        sysUserService.updateUserStatus(id, status);
        return Result.successMsg("状态更新成功");
    }

    @PutMapping("/admin/users/{id}/reset-password")
    @Operation(summary = "重置用户密码（管理员专用）")
    public Result<Void> resetUserPassword(@PathVariable Long id) {
        sysUserService.resetUserPassword(id);
        return Result.successMsg("密码已重置为 123456");
    }

    @DeleteMapping("/admin/users/{id}")
    @Operation(summary = "删除用户（管理员专用）")
    public Result<Void> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.successMsg("用户删除成功");
    }

    @GetMapping("/admin/statistics")
    @Operation(summary = "获取管理员统计数据（管理员专用）")
    public Result<AdminStatisticsVO> getAdminStatistics() {
        AdminStatisticsVO statistics = sysUserService.getAdminStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/student/statistics")
    @Operation(summary = "获取学生统计数据（学生专用）")
    public Result<com.example.citp.model.vo.StudentStatisticsVO> getStudentStatistics() {
        com.example.citp.model.vo.StudentStatisticsVO statistics = sysUserService.getStudentStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/admin/teachers/pending")
    @Operation(summary = "获取待审核的教师列表（管理员专用）")
    public Result<Page<UserInfoVO>> getPendingTeachers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<UserInfoVO> page = sysUserService.getPendingTeachers(pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/admin/teachers/{id}/audit")
    @Operation(summary = "审核教师注册（管理员专用）")
    public Result<Void> auditTeacher(
            @PathVariable Long id,
            @RequestParam Integer status) {
        sysUserService.auditTeacher(id, status);
        String message = status == 1 ? "审核通过" : "审核拒绝";
        return Result.successMsg(message);
    }
}
