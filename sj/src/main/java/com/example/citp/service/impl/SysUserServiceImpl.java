package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.ClassMapper;
import com.example.citp.mapper.SysRoleMapper;
import com.example.citp.mapper.SysUserMapper;
import com.example.citp.mapper.CourseMapper;
import com.example.citp.mapper.CourseClassMapper;
import com.example.citp.mapper.HomeworkMapper;
import com.example.citp.mapper.ExamMapper;
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
    private final CourseMapper courseMapper;
    private final CourseClassMapper courseClassMapper;
    private final HomeworkMapper homeworkMapper;
    private final ExamMapper examMapper;
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

        // 检查教师审核状态
        if (user.getUserType() == 2 && user.getStatus() == 2) {
            throw new BusinessException("您的账号正在审核中，请等待管理员审核通过后再登录");
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
        user.setStatus(2); // 待审核

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
                // 检查学号是否已存在（只查学生类型）
                SysUser existUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStudentNo, studentInfo.getStudentNo())
                        .eq(SysUser::getUserType, 1)); // 必须是学生类型

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

    @Override
    public Page<UserInfoVO> getAllUsers(Integer pageNum, Integer pageSize, String keyword, Integer userType) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 按用户类型筛选
        if (userType != null) {
            wrapper.eq(SysUser::getUserType, userType);
        }

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getStudentNo, keyword)
                    .or().like(SysUser::getTeacherNo, keyword)
                    .or().like(SysUser::getEmail, keyword));
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
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPassword(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 重置为默认密码 123456
        user.setPassword(passwordEncoder.encode("123456"));
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 不允许删除管理员
        if (user.getUserType() == 3) {
            throw new BusinessException("不允许删除管理员账号");
        }

        // 逻辑删除
        sysUserMapper.deleteById(userId);
    }

    @Override
    public com.example.citp.model.vo.AdminStatisticsVO getAdminStatistics() {
        com.example.citp.model.vo.AdminStatisticsVO vo = new com.example.citp.model.vo.AdminStatisticsVO();

        // 统计用户数
        Long totalUsers = sysUserMapper.selectCount(null);
        Long totalTeachers = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserType, 2));
        Long totalStudents = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserType, 1));

        // 统计课程数
        Long totalCourses = courseMapper.selectCount(null);

        // 统计班级数
        Long totalClasses = classMapper.selectCount(null);

        // 统计作业数
        Long totalHomeworks = homeworkMapper.selectCount(null);

        // 统计考试数
        Long totalExams = examMapper.selectCount(null);

        vo.setTotalUsers(totalUsers);
        vo.setTotalTeachers(totalTeachers);
        vo.setTotalStudents(totalStudents);
        vo.setTotalCourses(totalCourses);
        vo.setTotalClasses(totalClasses);
        vo.setTotalHomeworks(totalHomeworks);
        vo.setTotalExams(totalExams);

        return vo;
    }

    @Override
    public com.example.citp.model.vo.StudentStatisticsVO getStudentStatistics() {
        SysUser currentUser = getCurrentUser();
        Long studentId = currentUser.getId();

        // 我的班级数量
        Integer classCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_class WHERE student_id = ?",
                Integer.class, studentId);

        // 我的课程数量（通过班级关联）
        int courseCount = courseClassMapper.countCoursesByStudentId(studentId);

        // 我的作业数量
        Integer homeworkCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT h.id) FROM homework h " +
                "INNER JOIN course_class cc ON h.course_id = cc.course_id " +
                "INNER JOIN student_class sc ON cc.class_id = sc.class_id " +
                "WHERE sc.student_id = ? AND h.deleted = 0",
                Integer.class, studentId);

        // 我的考试数量
        Integer examCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT e.id) FROM exam e " +
                "INNER JOIN course_class cc ON e.course_id = cc.course_id " +
                "INNER JOIN student_class sc ON cc.class_id = sc.class_id " +
                "WHERE sc.student_id = ? AND e.deleted = 0",
                Integer.class, studentId);

        return new com.example.citp.model.vo.StudentStatisticsVO(
                classCount != null ? classCount : 0,
                courseCount,
                homeworkCount != null ? homeworkCount : 0,
                examCount != null ? examCount : 0
        );
    }

    @Override
    public Page<UserInfoVO> getPendingTeachers(Integer pageNum, Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserType, 2) // 教师
                .eq(SysUser::getStatus, 2) // 待审核
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);

        Page<UserInfoVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream()
                .map(u -> BeanUtil.copyProperties(u, UserInfoVO.class))
                .toList());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditTeacher(Long userId, Integer status) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getUserType() != 2) {
            throw new BusinessException("该用户不是教师");
        }

        if (user.getStatus() != 2) {
            throw new BusinessException("该教师不在待审核状态");
        }

        // status: 1-审核通过，0-审核拒绝
        if (status != 0 && status != 1) {
            throw new BusinessException("审核状态无效");
        }

        user.setStatus(status);
        sysUserMapper.updateById(user);
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
