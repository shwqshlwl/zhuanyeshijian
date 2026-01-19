package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.*;
import com.example.citp.model.entity.*;
import com.example.citp.model.request.CourseOfferingRequest;
import com.example.citp.model.vo.CourseOfferingVO;
import com.example.citp.model.vo.CourseTemplateVO;
import com.example.citp.service.CourseOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 开课实例服务实现类
 */
@Service
@RequiredArgsConstructor
public class CourseOfferingServiceImpl implements CourseOfferingService {

    private final CourseOfferingMapper courseOfferingMapper;
    private final CourseMapper courseMapper;
    private final SemesterMapper semesterMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public Page<CourseTemplateVO> getTeacherCourseTemplates(Long teacherId, Long semesterId, Integer pageNum, Integer pageSize, String keyword) {
        // 查询教师的所有课程模板
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getTeacherId, teacherId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Course::getCourseName, keyword)
                    .or()
                    .like(Course::getCourseCode, keyword));
        }
        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> coursePage = courseMapper.selectPage(page, wrapper);

        // 查询这些课程在指定学期的开课情况
        List<Long> courseIds = coursePage.getRecords().stream()
                .map(Course::getId)
                .toList();

        Map<Long, CourseOffering> offeringMap = null;
        if (!courseIds.isEmpty() && semesterId != null) {
            List<CourseOffering> offerings = courseOfferingMapper.selectList(
                    new LambdaQueryWrapper<CourseOffering>()
                            .in(CourseOffering::getCourseId, courseIds)
                            .eq(CourseOffering::getSemesterId, semesterId)
            );
            offeringMap = offerings.stream()
                    .collect(Collectors.toMap(CourseOffering::getCourseId, offering -> offering, (o1, o2) -> o1));
        }

        // 查询每个课程的历史开课次数
        Map<Long, Integer> offeringCountMap = null;
        if (!courseIds.isEmpty()) {
            Map<Long, CourseOffering> finalOfferingMap = offeringMap;
            offeringCountMap = courseIds.stream()
                    .collect(Collectors.toMap(
                            courseId -> courseId,
                            courseId -> {
                                Long count = courseOfferingMapper.selectCount(
                                        new LambdaQueryWrapper<CourseOffering>()
                                                .eq(CourseOffering::getCourseId, courseId)
                                );
                                return count.intValue();
                            }
                    ));
        }

        // 获取教师信息
        SysUser teacher = sysUserMapper.selectById(teacherId);
        String teacherName = teacher != null ? teacher.getUsername() : "";

        // 转换为 VO
        Map<Long, CourseOffering> finalOfferingMap1 = offeringMap;
        Map<Long, Integer> finalOfferingCountMap = offeringCountMap;
        Page<CourseTemplateVO> voPage = new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        voPage.setRecords(coursePage.getRecords().stream().map(course -> {
            CourseTemplateVO vo = BeanUtil.copyProperties(course, CourseTemplateVO.class);
            vo.setTeacherName(teacherName);

            // 设置本学期开课信息
            if (finalOfferingMap1 != null && finalOfferingMap1.containsKey(course.getId())) {
                CourseOffering offering = finalOfferingMap1.get(course.getId());
                vo.setIsOfferedThisSemester(true);
                vo.setOfferingId(offering.getId());
                vo.setOfferingStatus(offering.getStatus());
                vo.setOfferingTeacherId(offering.getTeacherId());

                // 获取授课教师姓名
                if (offering.getTeacherId() != null) {
                    SysUser offeringTeacher = sysUserMapper.selectById(offering.getTeacherId());
                    vo.setOfferingTeacherName(offeringTeacher != null ? offeringTeacher.getUsername() : "");
                }
            } else {
                vo.setIsOfferedThisSemester(false);
            }

            // 设置历史开课次数
            if (finalOfferingCountMap != null && finalOfferingCountMap.containsKey(course.getId())) {
                vo.setOfferingCount(finalOfferingCountMap.get(course.getId()));
            } else {
                vo.setOfferingCount(0);
            }

            return vo;
        }).toList());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOffering(CourseOfferingRequest request, Long currentUserId) {
        // 验证课程是否存在
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 验证学期是否存在
        Semester semester = semesterMapper.selectById(request.getSemesterId());
        if (semester == null) {
            throw new BusinessException("学期不存在");
        }

        // 验证教师是否存在
        SysUser teacher = sysUserMapper.selectById(request.getTeacherId());
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }

        // 权限检查：只有课程创建者或管理员可以开课
        if (!course.getTeacherId().equals(currentUserId)) {
            SysUser currentUser = sysUserMapper.selectById(currentUserId);
            if (currentUser == null || currentUser.getUserType() != 3) { // 3 表示管理员
                throw new BusinessException("只有课程创建者或管理员可以创建开课实例");
            }
        }

        // 检查该课程在该学期是否已开课
        CourseOffering existingOffering = courseOfferingMapper.selectByCourseAndSemester(
                request.getCourseId(), request.getSemesterId());
        if (existingOffering != null) {
            throw new BusinessException("该课程在该学期已经开课");
        }

        // 创建开课实例
        CourseOffering offering = BeanUtil.copyProperties(request, CourseOffering.class);
        if (offering.getStatus() == null) {
            offering.setStatus(0); // 默认未开课
        }
        offering.setCurrentStudents(0); // 初始选课人数为0

        courseOfferingMapper.insert(offering);
    }

    @Override
    public CourseOfferingVO getOfferingDetail(Long offeringId, Long currentUserId) {
        CourseOffering offering = courseOfferingMapper.selectById(offeringId);
        if (offering == null) {
            throw new BusinessException("开课实例不存在");
        }

        CourseOfferingVO vo = BeanUtil.copyProperties(offering, CourseOfferingVO.class);

        // 填充课程信息
        Course course = courseMapper.selectById(offering.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getCourseName());
            vo.setCourseCode(course.getCourseCode());
            vo.setCredit(course.getCredit());
            vo.setCourseType(course.getCourseType());
        }

        // 填充学期信息
        Semester semester = semesterMapper.selectById(offering.getSemesterId());
        if (semester != null) {
            vo.setSemesterName(semester.getSemesterName());
            vo.setAcademicYear(semester.getAcademicYear());
        }

        // 填充教师信息
        if (offering.getTeacherId() != null) {
            SysUser teacher = sysUserMapper.selectById(offering.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getUsername());
            }
        }

        // 统计关联班级数量
        // TODO: 这里需要根据实际的课程班级关系表来统计
        vo.setClassCount(0);

        // 权限设置
        boolean isTeacher = offering.getTeacherId().equals(currentUserId);
        boolean isCreator = course != null && course.getTeacherId().equals(currentUserId);
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        boolean isAdmin = currentUser != null && currentUser.getUserType() == 3;

        vo.setCanEdit(isTeacher || isCreator || isAdmin);
        vo.setCanDelete((isTeacher || isCreator || isAdmin) && offering.getCurrentStudents() == 0);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOffering(Long offeringId, CourseOfferingRequest request, Long currentUserId) {
        CourseOffering offering = courseOfferingMapper.selectById(offeringId);
        if (offering == null) {
            throw new BusinessException("开课实例不存在");
        }

        // 权限检查
        Course course = courseMapper.selectById(offering.getCourseId());
        boolean isTeacher = offering.getTeacherId().equals(currentUserId);
        boolean isCreator = course != null && course.getTeacherId().equals(currentUserId);
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        boolean isAdmin = currentUser != null && currentUser.getUserType() == 3;

        if (!isTeacher && !isCreator && !isAdmin) {
            throw new BusinessException("无权限修改此开课实例");
        }

        // 更新信息
        BeanUtil.copyProperties(request, offering, "id", "courseId", "semesterId", "currentStudents", "createTime");
        courseOfferingMapper.updateById(offering);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOffering(Long offeringId, Long currentUserId) {
        CourseOffering offering = courseOfferingMapper.selectById(offeringId);
        if (offering == null) {
            throw new BusinessException("开课实例不存在");
        }

        // 权限检查
        Course course = courseMapper.selectById(offering.getCourseId());
        boolean isTeacher = offering.getTeacherId().equals(currentUserId);
        boolean isCreator = course != null && course.getTeacherId().equals(currentUserId);
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        boolean isAdmin = currentUser != null && currentUser.getUserType() == 3;

        if (!isTeacher && !isCreator && !isAdmin) {
            throw new BusinessException("无权限删除此开课实例");
        }

        // 检查是否有学生选课
        if (offering.getCurrentStudents() > 0) {
            throw new BusinessException("已有学生选课，无法删除");
        }

        // TODO: 检查是否有关联的班级、作业、考试等

        courseOfferingMapper.deleteById(offeringId);
    }

    @Override
    public Page<CourseOfferingVO> getOfferingsBySemester(Long semesterId, Integer pageNum, Integer pageSize, String keyword) {
        Page<CourseOffering> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<CourseOffering> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseOffering::getSemesterId, semesterId);
        wrapper.orderByDesc(CourseOffering::getCreateTime);

        Page<CourseOffering> offeringPage = courseOfferingMapper.selectPage(page, wrapper);

        Page<CourseOfferingVO> voPage = new Page<>(offeringPage.getCurrent(), offeringPage.getSize(), offeringPage.getTotal());
        voPage.setRecords(offeringPage.getRecords().stream().map(this::convertToVO).toList());

        return voPage;
    }

    @Override
    public Page<CourseOfferingVO> getTeacherOfferings(Long teacherId, Long semesterId, Integer pageNum, Integer pageSize, String keyword) {
        Page<CourseOffering> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<CourseOffering> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseOffering::getTeacherId, teacherId);
        if (semesterId != null) {
            wrapper.eq(CourseOffering::getSemesterId, semesterId);
        }
        wrapper.orderByDesc(CourseOffering::getCreateTime);

        Page<CourseOffering> offeringPage = courseOfferingMapper.selectPage(page, wrapper);

        Page<CourseOfferingVO> voPage = new Page<>(offeringPage.getCurrent(), offeringPage.getSize(), offeringPage.getTotal());
        voPage.setRecords(offeringPage.getRecords().stream().map(this::convertToVO).toList());

        return voPage;
    }

    @Override
    public boolean isOfferedInSemester(Long courseId, Long semesterId) {
        CourseOffering offering = courseOfferingMapper.selectByCourseAndSemester(courseId, semesterId);
        return offering != null;
    }

    /**
     * 将 CourseOffering 转换为 CourseOfferingVO
     */
    private CourseOfferingVO convertToVO(CourseOffering offering) {
        CourseOfferingVO vo = BeanUtil.copyProperties(offering, CourseOfferingVO.class);

        // 填充课程信息
        Course course = courseMapper.selectById(offering.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getCourseName());
            vo.setCourseCode(course.getCourseCode());
            vo.setCredit(course.getCredit());
            vo.setCourseType(course.getCourseType());
        }

        // 填充学期信息
        Semester semester = semesterMapper.selectById(offering.getSemesterId());
        if (semester != null) {
            vo.setSemesterName(semester.getSemesterName());
            vo.setAcademicYear(semester.getAcademicYear());
        }

        // 填充教师信息
        if (offering.getTeacherId() != null) {
            SysUser teacher = sysUserMapper.selectById(offering.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getUsername());
            }
        }

        return vo;
    }
}
