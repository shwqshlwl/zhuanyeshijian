package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.*;
import com.example.citp.model.dto.CourseRequest;
import com.example.citp.model.entity.*;
import com.example.citp.model.vo.*;
import com.example.citp.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程服务实现类
 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final SysUserMapper sysUserMapper;
    private final ClassMapper classMapper;
    private final HomeworkMapper homeworkMapper;
    private final ExamMapper examMapper;
    private final CourseClassMapper courseClassMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<CourseVO> getCourseList(Integer pageNum, Integer pageSize, String keyword, Integer status, String type, Long currentUserId, Integer userType) {
        Page<Course> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        // 如果是"我的课程"，只查询当前用户创建的课程
        if ("my".equals(type) && currentUserId != null) {
            wrapper.eq(Course::getTeacherId, currentUserId);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getCourseName, keyword)
                    .or()
                    .like(Course::getCourseCode, keyword);
        }
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }
        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> coursePage = courseMapper.selectPage(page, wrapper);

        // 转换为 VO并设置权限标识
        Page<CourseVO> voPage = new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        voPage.setRecords(coursePage.getRecords().stream().map(course -> {
            CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class);
            // 查询教师姓名
            SysUser teacher = sysUserMapper.selectById(course.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }

            // 设置权限标识
            boolean isOwner = currentUserId != null && course.getTeacherId().equals(currentUserId);
            boolean isAdmin = userType != null && userType == 3;
            vo.setIsOwner(isOwner);
            vo.setCanEdit(isOwner || isAdmin);
            vo.setCanDelete(isOwner || isAdmin);

            // 查询关联班级数量（仅必修课）
            if (course.getCourseType() != null && course.getCourseType() == 0) {
                Long classCount = courseClassMapper.selectCount(new LambdaQueryWrapper<CourseClass>()
                        .eq(CourseClass::getCourseId, course.getId()));
                vo.setClassCount(classCount.intValue());
            }

            return vo;
        }).toList());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCourse(CourseRequest request) {
        // 检查课程编码是否已存在
        Long count = courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseCode, request.getCourseCode()));
        if (count > 0) {
            throw new BusinessException("课程编码已存在");
        }

        // 获取当前用户
        SysUser currentUser = getCurrentUser();

        Course course = BeanUtil.copyProperties(request, Course.class);
        course.setTeacherId(currentUser.getId());
        if (course.getStatus() == null) {
            course.setStatus(0); // 默认未开课
        }
        courseMapper.insert(course);
    }

    @Override
    public CourseDetailVO getCourseById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        CourseDetailVO vo = BeanUtil.copyProperties(course, CourseDetailVO.class);
        
        // 查询教师姓名
        SysUser teacher = sysUserMapper.selectById(course.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }

        // 查询关联班级
        List<ClassEntity> classes = classMapper.selectList(new LambdaQueryWrapper<ClassEntity>()
                .eq(ClassEntity::getCourseId, id));
        List<ClassVO> classVOs = classes.stream().map(c -> {
            ClassVO classVO = BeanUtil.copyProperties(c, ClassVO.class);
            if (c.getTeacherId() != null) {
                SysUser classTeacher = sysUserMapper.selectById(c.getTeacherId());
                if (classTeacher != null) {
                    classVO.setTeacherName(classTeacher.getRealName());
                }
            }
            return classVO;
        }).toList();
        vo.setClasses(classVOs);

        // 计算学生总数
        int studentCount = classes.stream()
                .mapToInt(c -> c.getStudentCount() != null ? c.getStudentCount() : 0)
                .sum();
        vo.setStudentCount(studentCount);

        // 查询作业列表（最近10条）
        List<Homework> homeworks = homeworkMapper.selectList(new LambdaQueryWrapper<Homework>()
                .eq(Homework::getCourseId, id)
                .orderByDesc(Homework::getCreateTime)
                .last("LIMIT 10"));
        List<HomeworkVO> homeworkVOs = homeworks.stream().map(h -> {
            HomeworkVO hvo = BeanUtil.copyProperties(h, HomeworkVO.class);
            hvo.setCourseName(course.getCourseName());
            if (h.getClassId() != null) {
                ClassEntity classEntity = classMapper.selectById(h.getClassId());
                if (classEntity != null) {
                    hvo.setClassName(classEntity.getClassName());
                }
            }
            return hvo;
        }).toList();
        vo.setHomeworks(homeworkVOs);

        // 查询考试列表（最近10条）
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getCourseId, id)
                .orderByDesc(Exam::getCreateTime)
                .last("LIMIT 10"));
        List<ExamVO> examVOs = exams.stream().map(e -> {
            ExamVO evo = BeanUtil.copyProperties(e, ExamVO.class);
            evo.setCourseName(course.getCourseName());
            if (e.getClassId() != null) {
                ClassEntity classEntity = classMapper.selectById(e.getClassId());
                if (classEntity != null) {
                    evo.setClassName(classEntity.getClassName());
                }
            }
            return evo;
        }).toList();
        vo.setExams(examVOs);

        return vo;
    }

    @Override
    public CourseVO getCourseBasicInfo(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class);
        SysUser teacher = sysUserMapper.selectById(course.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Long id, CourseRequest request) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 检查课程编码是否已被其他课程使用
        Long count = courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseCode, request.getCourseCode())
                .ne(Course::getId, id));
        if (count > 0) {
            throw new BusinessException("课程编码已存在");
        }

        BeanUtil.copyProperties(request, course, "id", "teacherId");
        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        courseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseStatus(Long id, Integer status) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        course.setStatus(status);
        courseMapper.updateById(course);
    }

    @Override
    public List<CourseVO> getTeacherCourses() {
        SysUser currentUser = getCurrentUser();
        
        List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, currentUser.getId())
                .orderByDesc(Course::getCreateTime));

        return courses.stream().map(course -> {
            CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class);
            vo.setTeacherName(currentUser.getRealName());
            return vo;
        }).toList();
    }

    @Override
    public List<CourseVO> getStudentCourses() {
        SysUser currentUser = getCurrentUser();

        // 1. 查询学生所在的班级ID列表
        List<Long> classIds = jdbcTemplate.queryForList(
                "SELECT class_id FROM student_class WHERE student_id = ?",
                Long.class, currentUser.getId());

        List<CourseVO> allCourses = new ArrayList<>();

        // 2. 通过 course_class 表查询这些班级关联的所有必修课
        if (!classIds.isEmpty()) {
            List<Course> requiredCourses = courseClassMapper.selectCoursesByClassIds(classIds);

            for (Course course : requiredCourses) {
                CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class);
                SysUser teacher = sysUserMapper.selectById(course.getTeacherId());
                if (teacher != null) {
                    vo.setTeacherName(teacher.getRealName());
                }
                // 设置课程类型为必修课
                if (vo.getCourseType() == null) {
                    vo.setCourseType(0);
                }
                allCourses.add(vo);
            }
        }

        // 3. 从 student_course 表查询学生选择的选修课
        List<StudentCourse> studentCourses = studentCourseMapper.selectList(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId, currentUser.getId())
                .eq(StudentCourse::getStatus, 1)
                .eq(StudentCourse::getCourseType, 1)); // 只查询选修课

        for (StudentCourse sc : studentCourses) {
            Course course = courseMapper.selectById(sc.getCourseId());
            if (course != null) {
                CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class);
                SysUser teacher = sysUserMapper.selectById(course.getTeacherId());
                if (teacher != null) {
                    vo.setTeacherName(teacher.getRealName());
                }
                allCourses.add(vo);
            }
        }

        return allCourses;
    }

    @Override
    public List<CourseStudentVO> getCourseStudents(Long courseId) {
        // 查询该课程的所有选课记录
        List<StudentCourse> studentCourses = studentCourseMapper.selectList(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getCourseId, courseId)
                .eq(StudentCourse::getStatus, 1)
                .orderByDesc(StudentCourse::getSelectionTime));

        return studentCourses.stream().map(sc -> {
            CourseStudentVO vo = new CourseStudentVO();
            vo.setStudentId(sc.getStudentId());
            vo.setCourseType(sc.getCourseType());
            vo.setSelectionTime(sc.getSelectionTime());
            vo.setStatus(sc.getStatus());

            // 查询学生信息
            SysUser student = sysUserMapper.selectById(sc.getStudentId());
            if (student != null) {
                vo.setStudentNumber(student.getUsername());
                vo.setStudentName(student.getRealName());
            }

            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeStudent(Long courseId, Long studentId) {
        // 查询课程
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 查询选课记录
        StudentCourse studentCourse = studentCourseMapper.selectOne(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getCourseId, courseId)
                .eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getStatus, 1));

        if (studentCourse == null) {
            throw new BusinessException("该学生未选择此课程");
        }

        // 更新选课记录状态为已退课
        studentCourse.setStatus(0);
        studentCourseMapper.updateById(studentCourse);

        // 如果是选修课，更新课程的当前选课人数
        if (course.getCourseType() == 1) {
            Integer currentCount = studentCourseMapper.countByCourseId(courseId);
            course.setCurrentStudents(currentCount);
            courseMapper.updateById(course);
        }
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
