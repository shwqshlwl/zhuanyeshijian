package com.example.citp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.common.Result;
import com.example.citp.model.dto.*;
import com.example.citp.model.vo.BatchImportResultVO;
import com.example.citp.model.vo.LoginResponse;
import com.example.citp.model.vo.UserInfoVO;
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
}
