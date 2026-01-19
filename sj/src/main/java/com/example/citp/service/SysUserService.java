package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.dto.*;
import com.example.citp.model.vo.BatchImportResultVO;
import com.example.citp.model.vo.LoginResponse;
import com.example.citp.model.vo.UserInfoVO;

/**
 * 用户服务接口
 */
public interface SysUserService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 教师注册
     */
    void registerTeacher(TeacherRegisterRequest request);

    /**
     * 获取当前用户信息
     */
    UserInfoVO getCurrentUserInfo();

    /**
     * 更新用户信息
     */
    void updateUserInfo(UserUpdateRequest request);

    /**
     * 修改密码
     */
    void changePassword(PasswordChangeRequest request);

    /**
     * 批量导入学生
     */
    BatchImportResultVO batchImportStudents(BatchImportStudentRequest request);

    /**
     * 获取学生列表（按班级）
     */
    Page<UserInfoVO> getStudentsByClassId(Long classId, Integer pageNum, Integer pageSize);

    /**
     * 获取所有学生列表
     */
    Page<UserInfoVO> getAllStudents(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 根据学号查询学生
     */
    UserInfoVO getStudentByStudentNo(String studentNo);

    /**
     * 获取所有用户列表（管理员专用）
     */
    Page<UserInfoVO> getAllUsers(Integer pageNum, Integer pageSize, String keyword, Integer userType);

    /**
     * 更新用户状态（管理员专用）
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 重置用户密码（管理员专用）
     */
    void resetUserPassword(Long userId);

    /**
     * 删除用户（管理员专用）
     */
    void deleteUser(Long userId);

    /**
     * 获取管理员统计数据
     */
    com.example.citp.model.vo.AdminStatisticsVO getAdminStatistics();

    /**
     * 获取学生统计数据
     */
    com.example.citp.model.vo.StudentStatisticsVO getStudentStatistics();

    /**
     * 获取待审核的教师列表
     */
    Page<UserInfoVO> getPendingTeachers(Integer pageNum, Integer pageSize);

    /**
     * 审核教师注册
     */
    void auditTeacher(Long userId, Integer status);
}
