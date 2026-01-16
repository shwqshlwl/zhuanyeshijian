package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.*;
import com.example.citp.model.dto.HomeworkGradeRequest;
import com.example.citp.model.dto.HomeworkRequest;
import com.example.citp.model.dto.HomeworkSubmitRequest;
import com.example.citp.model.entity.*;
import com.example.citp.model.vo.HomeworkDetailVO;
import com.example.citp.model.vo.HomeworkListDetailVO;
import com.example.citp.model.vo.HomeworkSubmitVO;
import com.example.citp.model.vo.HomeworkVO;
import com.example.citp.service.HomeworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 作业服务实现类
 */
@Service
@RequiredArgsConstructor
public class HomeworkServiceImpl implements HomeworkService {

    private final HomeworkMapper homeworkMapper;
    private final HomeworkSubmitMapper homeworkSubmitMapper;
    private final CourseMapper courseMapper;
    private final ClassMapper classMapper;
    private final SysUserMapper sysUserMapper;
    private final JdbcTemplate jdbcTemplate;
    @Override
    public void refreshHomeworkStatus() {
        homeworkMapper.updateHomeworkStatusByTime();
    }
    @Override
    public Page<HomeworkVO> getHomeworkList(Integer pageNum, Integer pageSize, Long courseId, Long classId, Integer status) {
        Page<Homework> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Homework::getCourseId, courseId);
        }
        if (classId != null) {
            wrapper.eq(Homework::getClassId, classId);
        }
        if (status != null) {
            wrapper.eq(Homework::getStatus, status);
        }
        wrapper.orderByDesc(Homework::getCreateTime);

        Page<Homework> homeworkPage = homeworkMapper.selectPage(page, wrapper);

        // 转换为 VO
        Page<HomeworkVO> voPage = new Page<>(homeworkPage.getCurrent(), homeworkPage.getSize(), homeworkPage.getTotal());
        voPage.setRecords(homeworkPage.getRecords().stream().map(this::convertToVO).toList());

        return voPage;
    }

    @Override
    public Page<HomeworkListDetailVO> getHomeworkListDetail(Integer pageNum, Integer pageSize, Long courseId, Long classId, Integer status) {
        Page<Homework> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();

        Page<Homework> homeworkPage = homeworkMapper.selectPage(page, wrapper);



        // 转换为 VO
        Page<HomeworkListDetailVO> voPage = new Page<>(homeworkPage.getCurrent(), homeworkPage.getSize(), homeworkPage.getTotal());
        voPage.setRecords(homeworkPage.getRecords().stream().map(this::convertToHomeworkListDetailVO).toList());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createHomework(HomeworkRequest request) {
        SysUser currentUser = getCurrentUser();

        Homework homework = BeanUtil.copyProperties(request, Homework.class);
        homework.setTeacherId(currentUser.getId());
        if (homework.getStatus() == null) {
            homework.setStatus(1); // 默认已发布
        }
        if (homework.getTotalScore() == null) {
            homework.setTotalScore(100);
        }
        homeworkMapper.insert(homework);
    }

    @Override
    public HomeworkDetailVO getHomeworkById(Long id) {
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }

        HomeworkDetailVO vo = BeanUtil.copyProperties(homework, HomeworkDetailVO.class);
        
        // 查询课程名称
        Course course = courseMapper.selectById(homework.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getCourseName());
        }
        
        // 查询班级名称
        if (homework.getClassId() != null) {
            ClassEntity classEntity = classMapper.selectById(homework.getClassId());
            if (classEntity != null) {
                vo.setClassName(classEntity.getClassName());
            } else {
                // 班级记录不存在，设置默认名称
                vo.setClassName("班级已删除");
            }
        }
        
        // 查询教师姓名
        SysUser teacher = sysUserMapper.selectById(homework.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }

        // 统计提交情况
        Long submittedCount = homeworkSubmitMapper.selectCount(new LambdaQueryWrapper<HomeworkSubmit>()
                .eq(HomeworkSubmit::getHomeworkId, id)
                .ge(HomeworkSubmit::getStatus, 1));
        vo.setSubmittedCount(submittedCount.intValue());

        Long gradedCount = homeworkSubmitMapper.selectCount(new LambdaQueryWrapper<HomeworkSubmit>()
                .eq(HomeworkSubmit::getHomeworkId, id)
                .eq(HomeworkSubmit::getStatus, 2));
        vo.setGradedCount(gradedCount.intValue());

        // 计算总人数（班级学生数）
        if (homework.getClassId() != null) {
            Integer totalCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM student_class WHERE class_id = ?",
                    Integer.class, homework.getClassId());
            vo.setTotalCount(totalCount != null ? totalCount : 0);
        } else {
            vo.setTotalCount(0);
        }

        // 获取当前用户
        SysUser currentUser = getCurrentUserOrNull();
        if (currentUser != null && currentUser.getUserType() == 1) {
            // 学生视角：获取自己的提交
            HomeworkSubmit mySubmit = homeworkSubmitMapper.selectOne(new LambdaQueryWrapper<HomeworkSubmit>()
                    .eq(HomeworkSubmit::getHomeworkId, id)
                    .eq(HomeworkSubmit::getStudentId, currentUser.getId()));
            if (mySubmit != null) {
                vo.setMySubmission(convertSubmitToVO(mySubmit));
            }
        } else if (currentUser != null && currentUser.getUserType() == 2) {
            // 教师视角：获取提交列表（前10条）
            List<HomeworkSubmit> submissions = homeworkSubmitMapper.selectList(new LambdaQueryWrapper<HomeworkSubmit>()
                    .eq(HomeworkSubmit::getHomeworkId, id)
                    .orderByDesc(HomeworkSubmit::getSubmitTime)
                    .last("LIMIT 10"));
            vo.setSubmissions(submissions.stream().map(this::convertSubmitToVO).toList());
        }

        return vo;
    }

    @Override
    public HomeworkVO getHomeworkBasicInfo(Long id) {
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }
        return convertToVO(homework);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHomework(Long id, HomeworkRequest request) {
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }

        BeanUtil.copyProperties(request, homework, "id", "teacherId");
        homeworkMapper.updateById(homework);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHomework(Long id) {
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }

        homeworkMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitHomework(Long homeworkId, HomeworkSubmitRequest request) {
        // 检查作业是否存在
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }

        // 获取当前学生
        SysUser currentUser = getCurrentUser();
        if (currentUser.getUserType() != 3) {
            throw new BusinessException("只有学生可以提交作业");
        }

        // 检查是否已提交
        HomeworkSubmit existSubmit = homeworkSubmitMapper.selectOne(new LambdaQueryWrapper<HomeworkSubmit>()
                .eq(HomeworkSubmit::getHomeworkId, homeworkId)
                .eq(HomeworkSubmit::getStudentId, currentUser.getId()));

        LocalDateTime now = LocalDateTime.now();
        boolean isLate = homework.getEndTime() != null && now.isAfter(homework.getEndTime());

        // 检查是否允许迟交
        if (isLate && (homework.getAllowLate() == null || homework.getAllowLate() == 0)) {
            throw new BusinessException("作业已截止，不允许迟交");
        }

        if (existSubmit != null) {
            // 更新提交
            existSubmit.setContent(request.getContent());
            existSubmit.setAttachment(request.getAttachment());
            existSubmit.setSubmitTime(now);
            existSubmit.setIsLate(isLate ? 1 : 0);
            existSubmit.setStatus(1);
            homeworkSubmitMapper.updateById(existSubmit);
        } else {
            // 新建提交
            HomeworkSubmit submit = new HomeworkSubmit();
            submit.setHomeworkId(homeworkId);
            submit.setStudentId(currentUser.getId());
            submit.setContent(request.getContent());
            submit.setAttachment(request.getAttachment());
            submit.setSubmitTime(now);
            submit.setIsLate(isLate ? 1 : 0);
            submit.setStatus(1);
            homeworkSubmitMapper.insert(submit);
        }
    }

    @Override
    public Page<HomeworkSubmitVO> getHomeworkSubmissions(Long homeworkId, Integer pageNum, Integer pageSize, Integer status) {
        Page<HomeworkSubmit> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<HomeworkSubmit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmit::getHomeworkId, homeworkId);
        if (status != null) {
            wrapper.eq(HomeworkSubmit::getStatus, status);
        }
        wrapper.orderByDesc(HomeworkSubmit::getSubmitTime);

        Page<HomeworkSubmit> submitPage = homeworkSubmitMapper.selectPage(page, wrapper);

        Page<HomeworkSubmitVO> voPage = new Page<>(submitPage.getCurrent(), submitPage.getSize(), submitPage.getTotal());
        voPage.setRecords(submitPage.getRecords().stream().map(this::convertSubmitToVO).toList());

        return voPage;
    }

    @Override
    public HomeworkSubmitVO getStudentSubmission(Long homeworkId, Long studentId) {
        HomeworkSubmit submit = homeworkSubmitMapper.selectOne(new LambdaQueryWrapper<HomeworkSubmit>()
                .eq(HomeworkSubmit::getHomeworkId, homeworkId)
                .eq(HomeworkSubmit::getStudentId, studentId));

        if (submit == null) {
            return null;
        }

        return convertSubmitToVO(submit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void gradeHomework(Long homeworkId, Long studentId, HomeworkGradeRequest request) {
        // 获取当前教师
        SysUser currentUser = getCurrentUser();

        // 查询提交记录
        HomeworkSubmit submit = homeworkSubmitMapper.selectOne(new LambdaQueryWrapper<HomeworkSubmit>()
                .eq(HomeworkSubmit::getHomeworkId, homeworkId)
                .eq(HomeworkSubmit::getStudentId, studentId));

        if (submit == null) {
            throw new BusinessException("提交记录不存在");
        }

        // 批改作业
        submit.setScore(request.getScore());
        submit.setComment(request.getComment());
        submit.setGradeTime(LocalDateTime.now());
        submit.setGraderId(currentUser.getId());
        submit.setStatus(2);
        homeworkSubmitMapper.updateById(submit);
    }

    @Override
    public String aiAnalyzeHomework(Long homeworkId) {
        // TODO: 集成 AI 服务进行题目解析
        return "AI 解析功能开发中";
    }

    @Override
    public Page<HomeworkVO> getStudentHomeworks(Integer pageNum, Integer pageSize, Long courseId, Integer status) {
        SysUser currentUser = getCurrentUser();
        
        // 获取学生所在班级
        List<Long> classIds = jdbcTemplate.queryForList(
                "SELECT class_id FROM student_class WHERE student_id = ?",
                Long.class, currentUser.getId());

        if (classIds.isEmpty()) {
            Page<HomeworkVO> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setRecords(new ArrayList<>());
            emptyPage.setTotal(0);
            return emptyPage;
        }

        Page<Homework> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Homework::getClassId, classIds);
        if (courseId != null) {
            wrapper.eq(Homework::getCourseId, courseId);
        }
        if (status != null) {
            wrapper.eq(Homework::getStatus, status);
        }
        wrapper.orderByDesc(Homework::getCreateTime);

        Page<Homework> homeworkPage = homeworkMapper.selectPage(page, wrapper);

        Page<HomeworkVO> voPage = new Page<>(homeworkPage.getCurrent(), homeworkPage.getSize(), homeworkPage.getTotal());
        voPage.setRecords(homeworkPage.getRecords().stream().map(this::convertToVO).toList());

        return voPage;
    }

    /**
     * 转换为 VO
     */
    private HomeworkVO convertToVO(Homework homework) {
        HomeworkVO vo = BeanUtil.copyProperties(homework, HomeworkVO.class);
        
        // 查询课程名称
        Course course = courseMapper.selectById(homework.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getCourseName());
        }
        
        // 查询班级名称
        if (homework.getClassId() != null) {
            ClassEntity classEntity = classMapper.selectById(homework.getClassId());
            if (classEntity != null) {
                vo.setClassName(classEntity.getClassName());
            } else {
                // 班级记录不存在，设置默认名称
                vo.setClassName("班级已删除");
            }
        }
        
        // 查询教师姓名
        SysUser teacher = sysUserMapper.selectById(homework.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }
        
        return vo;
    }

    /**
     * 转换为 HomeworkListDetailVO
     * @param homework
     * @return
     */
    private HomeworkListDetailVO convertToHomeworkListDetailVO(Homework homework) {
        HomeworkListDetailVO vo = BeanUtil.copyProperties(homework, HomeworkListDetailVO.class);
        // 统计提交情况
        Long submittedCount = homeworkSubmitMapper.selectCount(new LambdaQueryWrapper<HomeworkSubmit>()
                .eq(HomeworkSubmit::getHomeworkId, homework.getId())
                .ge(HomeworkSubmit::getStatus, 1));
        vo.setSubmittedCount(submittedCount.intValue());

        // 计算总人数（班级学生数）
        if (homework.getClassId() != null) {
            Integer totalCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM student_class WHERE class_id = ?",
                    Integer.class, homework.getClassId());
            vo.setTotalCount(totalCount != null ? totalCount : 0);
        } else {
            vo.setTotalCount(0);
        }

        return vo;
    }

    /**
     * 转换提交为 VO
     */
    private HomeworkSubmitVO convertSubmitToVO(HomeworkSubmit submit) {
        HomeworkSubmitVO vo = BeanUtil.copyProperties(submit, HomeworkSubmitVO.class);
        
        // 查询学生信息
        SysUser student = sysUserMapper.selectById(submit.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getRealName());
            vo.setStudentNo(student.getStudentNo());
        }
        
        // 查询批改教师
        if (submit.getGraderId() != null) {
            SysUser grader = sysUserMapper.selectById(submit.getGraderId());
            if (grader != null) {
                vo.setGraderName(grader.getRealName());
            }
        }
        
        return vo;
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

    /**
     * 获取当前登录用户（可能为空）
     */
    private SysUser getCurrentUserOrNull() {
        try {
            return getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }


}
