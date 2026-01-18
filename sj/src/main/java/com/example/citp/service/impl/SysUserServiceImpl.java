package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.ClassMapper;
import com.example.citp.mapper.SysRoleMapper;
import com.example.citp.mapper.SysUserMapper;
import com.example.citp.model.dto.*;
import com.example.citp.model.entity.ClassEntity;
import com.example.citp.model.entity.SysUser;
import com.example.citp.model.vo.BatchImportResultVO;
import com.example.citp.model.vo.LoginResponse;
import com.example.citp.model.vo.UserInfoVO;
import com.example.citp.service.SysUserService;
import com.example.citp.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final ClassMapper classMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 构建用户信息
        UserInfoVO userInfo = BeanUtil.copyProperties(user, UserInfoVO.class);

        return new LoginResponse(token, userInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerTeacher(TeacherRegisterRequest request) {
        // 检查用户名是否已存在
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查工号是否已存在
        count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTeacherNo, request.getTeacherNo()));
        if (count > 0) {
            throw new BusinessException("工号已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setTeacherNo(request.getTeacherNo());
        user.setUserType(2); // 教师
        user.setStatus(1);

        sysUserMapper.insert(user);

        // 关联教师角色
        jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, 2)", user.getId());
    }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        SysUser user = getCurrentUser();
        return BeanUtil.copyProperties(user, UserInfoVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserUpdateRequest request) {
        SysUser user = getCurrentUser();

        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }

        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(PasswordChangeRequest request) {
        SysUser user = getCurrentUser();

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchImportResultVO batchImportStudents(BatchImportStudentRequest request) {
        // 验证班级是否存在
        ClassEntity classEntity = classMapper.selectById(request.getClassId());
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }

        int successCount = 0;
        int failCount = 0;
        List<String> failDetails = new ArrayList<>();

        for (BatchImportStudentRequest.StudentInfo studentInfo : request.getStudents()) {
            try {
                // 检查学号是否已存在
                SysUser existUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStudentNo, studentInfo.getStudentNo()));

                Long studentId;
                if (existUser != null) {
                    // 学生已存在，直接使用
                    studentId = existUser.getId();
                } else {
                    // 创建新学生账号
                    SysUser student = new SysUser();
                    student.setUsername(studentInfo.getStudentNo()); // 默认用户名为学号
                    student.setPassword(passwordEncoder.encode("123456")); // 默认密码
                    student.setRealName(studentInfo.getRealName());
                    student.setStudentNo(studentInfo.getStudentNo());
                    student.setEmail(studentInfo.getEmail());
                    student.setPhone(studentInfo.getPhone());
                    student.setUserType(1); // 学生
                    student.setStatus(1);

                    sysUserMapper.insert(student);
                    studentId = student.getId();

                    // 关联学生角色
                    jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) VALUES (?, 3)", studentId);
                }

                // 检查学生是否已在班级中
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM student_class WHERE student_id = ? AND class_id = ?",
                        Integer.class, studentId, request.getClassId());

                if (count == null || count == 0) {
                    // 添加学生到班级
                    jdbcTemplate.update("INSERT INTO student_class (student_id, class_id) VALUES (?, ?)",
                            studentId, request.getClassId());
                }

                successCount++;
            } catch (Exception e) {
                failCount++;
                failDetails.add(String.format("学号 %s: %s", studentInfo.getStudentNo(), e.getMessage()));
            }
        }

        // 更新班级学生人数
        Integer totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_class WHERE class_id = ?",
                Integer.class, request.getClassId());
        classEntity.setStudentCount(totalCount != null ? totalCount : 0);
        classMapper.updateById(classEntity);

        return new BatchImportResultVO(successCount, failCount, String.join("; ", failDetails));
    }

    @Override
    public Page<UserInfoVO> getStudentsByClassId(Long classId, Integer pageNum, Integer pageSize) {
        Page<UserInfoVO> page = new Page<>(pageNum, pageSize);

        // 查询班级中的学生ID列表
        List<Long> studentIds = jdbcTemplate.queryForList(
                "SELECT student_id FROM student_class WHERE class_id = ? LIMIT ? OFFSET ?",
                Long.class, classId, pageSize, (pageNum - 1) * pageSize);

        if (studentIds.isEmpty()) {
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            return page;
        }

        // 查询学生信息
        List<SysUser> students = sysUserMapper.selectBatchIds(studentIds);
        List<UserInfoVO> voList = students.stream()
                .map(s -> BeanUtil.copyProperties(s, UserInfoVO.class))
                .toList();

        // 查询总数
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_class WHERE class_id = ?",
                Integer.class, classId);

        page.setRecords(voList);
        page.setTotal(total != null ? total : 0);
        return page;
    }

    @Override
    public Page<UserInfoVO> getAllStudents(Integer pageNum, Integer pageSize, String keyword) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserType, 1); // 只查询学生
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getStudentNo, keyword)
                    .or().like(SysUser::getUsername, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);

        Page<UserInfoVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream()
                .map(u -> BeanUtil.copyProperties(u, UserInfoVO.class))
                .toList());

        return voPage;
    }

    @Override
    public UserInfoVO getStudentByStudentNo(String studentNo) {
        SysUser student = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStudentNo, studentNo)
                .eq(SysUser::getUserType, 1));//学生应该是3啊

        if (student == null) {
            return null;
        }

        return BeanUtil.copyProperties(student, UserInfoVO.class);
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
