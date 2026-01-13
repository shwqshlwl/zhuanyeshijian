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
}
