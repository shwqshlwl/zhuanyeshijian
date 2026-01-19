package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.ClassMapper;
import com.example.citp.mapper.CourseClassMapper;
import com.example.citp.mapper.CourseMapper;
import com.example.citp.mapper.SysUserMapper;
import com.example.citp.model.dto.ClassRequest;
import com.example.citp.model.entity.ClassEntity;
import com.example.citp.model.entity.Course;
import com.example.citp.model.entity.SysUser;
import com.example.citp.model.vo.*;
import com.example.citp.service.ClassService;
import com.example.citp.service.CourseClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 班级服务实现类
 */
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final CourseClassMapper courseClassMapper;
    private final ClassMapper classMapper;
    private final CourseMapper courseMapper;
    private final SysUserMapper sysUserMapper;
    private final JdbcTemplate jdbcTemplate;
    private final CourseClassService courseClassService;

    @Override
    public Page<ClassVO> getClassList(Integer pageNum, Integer pageSize, String grade, String major) {
        Page<ClassEntity> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<ClassEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(grade)) {
            wrapper.eq(ClassEntity::getGrade, grade);
        }
        if (StringUtils.hasText(major)) {
            wrapper.like(ClassEntity::getMajor, major);
        }
        wrapper.orderByDesc(ClassEntity::getGrade).orderByAsc(ClassEntity::getClassName);

        Page<ClassEntity> classPage = classMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        Page<ClassVO> voPage = new Page<>(classPage.getCurrent(), classPage.getSize(), classPage.getTotal());
        voPage.setRecords(classPage.getRecords().stream().map(this::convertToVO).toList());

        return voPage;
    }

    @Override
    public Map<String, List<ClassVO>> getClassListGroupByGrade() {
        List<ClassEntity> allClasses = classMapper.selectList(new LambdaQueryWrapper<ClassEntity>()
                .orderByDesc(ClassEntity::getGrade).orderByAsc(ClassEntity::getClassName));

        return allClasses.stream()
                .map(this::convertToVO)
                .collect(Collectors.groupingBy(
                        c -> c.getGrade() != null ? c.getGrade() : "未分组",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createClass(ClassRequest request) {
        // 检查班级编码是否已存在
        Long count = classMapper.selectCount(new LambdaQueryWrapper<ClassEntity>()
                .eq(ClassEntity::getClassCode, request.getClassCode()));
        if (count > 0) {
            throw new BusinessException("班级编码已存在");
        }

        // 获取当前用户
        SysUser currentUser = getCurrentUser();

        ClassEntity classEntity = BeanUtil.copyProperties(request, ClassEntity.class);
        classEntity.setTeacherId(currentUser.getId());
        classEntity.setStudentCount(0);
        classMapper.insert(classEntity);

        // 建立课程关联（优先使用courseIds，如果为空则使用courseId）
        List<Long> courseIds = request.getCourseIds();
        if (courseIds == null || courseIds.isEmpty()) {
            if (request.getCourseId() != null) {
                courseIds = Collections.singletonList(request.getCourseId());
            }
        }
        if (courseIds != null && !courseIds.isEmpty()) {
            courseClassService.setClassCourses(classEntity.getId(), courseIds);
        }
    }

    @Override
    public ClassDetailVO getClassById(Long id) {
        ClassEntity classEntity = classMapper.selectById(id);
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }

        ClassDetailVO vo = BeanUtil.copyProperties(classEntity, ClassDetailVO.class);
        
        // 查询课程名称
        if (classEntity.getCourseId() != null) {
            Course course = courseMapper.selectById(classEntity.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
        }
        
        // 查询教师姓名
        if (classEntity.getTeacherId() != null) {
            SysUser teacher = sysUserMapper.selectById(classEntity.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }
        }

        // 查询学生列表（前20个）
        List<Long> studentIds = jdbcTemplate.queryForList(
                "SELECT student_id FROM student_class WHERE class_id = ? LIMIT 20",
                Long.class, id);
        
        if (!studentIds.isEmpty()) {
            List<SysUser> students = sysUserMapper.selectBatchIds(studentIds);
            List<UserInfoVO> studentVOs = students.stream()
                    .map(s -> BeanUtil.copyProperties(s, UserInfoVO.class))
                    .toList();
            vo.setStudents(studentVOs);
        } else {
            vo.setStudents(new ArrayList<>());
        }

        // 查询关联课程列表（多对多关系）
        List<Course> linkedCourses = courseClassService.getCoursesByClassId(id);
        List<CourseVO> courseVOs = linkedCourses.stream()
                .map(course -> {
                    CourseVO courseVO = BeanUtil.copyProperties(course, CourseVO.class);
                    // 填充教师姓名
                    if (course.getTeacherId() != null) {
                        SysUser teacher = sysUserMapper.selectById(course.getTeacherId());
                        if (teacher != null) {
                            courseVO.setTeacherName(teacher.getRealName());
                        }
                    }
                    return courseVO;
                })
                .toList();
        vo.setCourses(courseVOs);

        return vo;
    }

    @Override
    public ClassVO getClassBasicInfo(Long id) {
        ClassEntity classEntity = classMapper.selectById(id);
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }
        return convertToVO(classEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClass(Long id, ClassRequest request) {
        ClassEntity classEntity = classMapper.selectById(id);
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }

        // 检查班级编码是否已被其他班级使用
        Long count = classMapper.selectCount(new LambdaQueryWrapper<ClassEntity>()
                .eq(ClassEntity::getClassCode, request.getClassCode())
                .ne(ClassEntity::getId, id));
        if (count > 0) {
            throw new BusinessException("班级编码已存在");
        }

        BeanUtil.copyProperties(request, classEntity, "id", "teacherId", "studentCount");
        classMapper.updateById(classEntity);

        // 更新课程关联（优先使用courseIds，如果为空则使用courseId）
        List<Long> courseIds = request.getCourseIds();
        if (courseIds == null || courseIds.isEmpty()) {
            if (request.getCourseId() != null) {
                courseIds = Collections.singletonList(request.getCourseId());
            }
        }
        if (courseIds != null) {
            courseClassService.setClassCourses(id, courseIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteClass(Long id) {
        ClassEntity classEntity = classMapper.selectById(id);
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }

        // 删除班级学生关联
        jdbcTemplate.update("DELETE FROM student_class WHERE class_id = ?", id);

        // 删除班级课程关联
        courseClassService.deleteByClassId(id);

        classMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStudentToClass(Long classId, Long studentId) {
        // 检查班级是否存在
        ClassEntity classEntity = classMapper.selectById(classId);
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }

        // 检查学生是否存在
        SysUser student = sysUserMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        } else if (student.getUserType() != 1) {
            throw new BusinessException("用户不是学生");
        }

        // 检查学生是否已在班级中
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_class WHERE student_id = ? AND class_id = ?",
                Integer.class, studentId, classId);
        if (count != null && count > 0) {
            throw new BusinessException("学生已在班级中");
        }

        // 添加学生到班级
        jdbcTemplate.update("INSERT INTO student_class (student_id, class_id) VALUES (?, ?)", 
                studentId, classId);

        // 更新班级学生人数
        updateStudentCount(classEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStudentToClassByStudentNo(Long classId, String studentNo) {
        // 检查班级是否存在
        ClassEntity classEntity = classMapper.selectById(classId);
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }

        // 根据学号查询学生
        SysUser student = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStudentNo, studentNo)
                .eq(SysUser::getUserType, 1));
        if (student == null) {
            throw new BusinessException("学号 " + studentNo + " 对应的学生不存在");
        }

        // 检查学生是否已在班级中
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_class WHERE student_id = ? AND class_id = ?",
                Integer.class, student.getId(), classId);
        if (count != null && count > 0) {
            throw new BusinessException("学生已在班级中");
        }

        // 添加学生到班级
        jdbcTemplate.update("INSERT INTO student_class (student_id, class_id) VALUES (?, ?)", 
                student.getId(), classId);

        // 更新班级学生人数
        updateStudentCount(classEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeStudentFromClass(Long classId, Long studentId) {
        // 检查班级是否存在
        ClassEntity classEntity = classMapper.selectById(classId);
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }

        // 删除学生班级关联
        int rows = jdbcTemplate.update(
                "DELETE FROM student_class WHERE student_id = ? AND class_id = ?",
                studentId, classId);

        if (rows > 0) {
            // 更新班级学生人数
            updateStudentCount(classEntity);
        }
    }

    @Override
    public Page<UserInfoVO> getClassStudents(Long classId, Integer pageNum, Integer pageSize) {
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
    public List<ClassVO> getClassesByCourseId(Long courseId) {
        List<ClassEntity> classes =
                courseClassMapper.selectClassesByCourseId(courseId);

        return classes.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindCourseToClass(Long classId, Long courseId) {
        ClassEntity classEntity = classMapper.selectById(classId);
        if (classEntity == null) {
            throw new BusinessException("班级不存在");
        }

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        classEntity.setCourseId(courseId);
        classMapper.updateById(classEntity);
    }

    /**
     * 转换为 VO
     */
    private ClassVO convertToVO(ClassEntity classEntity) {
        ClassVO vo = BeanUtil.copyProperties(classEntity, ClassVO.class);

        // 查询课程名称（保持向后兼容）
        if (classEntity.getCourseId() != null) {
            Course course = courseMapper.selectById(classEntity.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
        }

        // 查询关联的课程列表（多对多）
        List<Course> courses = courseClassService.getCoursesByClassId(classEntity.getId());
        vo.setCourses(courses);

        // 查询教师姓名
        if (classEntity.getTeacherId() != null) {
            SysUser teacher = sysUserMapper.selectById(classEntity.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }
        }

        return vo;
    }

    /**
     * 更新班级学生人数
     */
    private void updateStudentCount(ClassEntity classEntity) {
        Integer totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_class WHERE class_id = ?",
                Integer.class, classEntity.getId());
        classEntity.setStudentCount(totalCount != null ? totalCount : 0);
        classMapper.updateById(classEntity);
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
