package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.CourseMapper;
import com.example.citp.mapper.StudentCourseMapper;
import com.example.citp.mapper.SysUserMapper;
import com.example.citp.model.entity.Course;
import com.example.citp.model.entity.StudentCourse;
import com.example.citp.model.entity.SysUser;
import com.example.citp.model.vo.ElectiveCourseVO;
import com.example.citp.service.StudentCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生选课服务实现类
 */
@Service
@RequiredArgsConstructor
public class StudentCourseServiceImpl implements StudentCourseService {

    private final CourseMapper courseMapper;
    private final StudentCourseMapper studentCourseMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public Page<ElectiveCourseVO> getElectiveCourseList(Integer pageNum, Integer pageSize, String keyword, Long studentId) {
        Page<Course> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        // 只查询选修课
        wrapper.eq(Course::getCourseType, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getCourseName, keyword)
                    .or()
                    .like(Course::getCourseCode, keyword);
        }

        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> coursePage = courseMapper.selectPage(page, wrapper);

        // 转换为 VO 并设置选课状态
        Page<ElectiveCourseVO> voPage = new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        voPage.setRecords(coursePage.getRecords().stream().map(course -> {
            ElectiveCourseVO vo = BeanUtil.copyProperties(course, ElectiveCourseVO.class);

            // 查询教师姓名
            SysUser teacher = sysUserMapper.selectById(course.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }

            // 查询实际当前选课人数
            Integer actualCount = studentCourseMapper.countByCourseId(course.getId());
            vo.setCurrentStudents(actualCount);

            LocalDateTime now = LocalDateTime.now();

            // 检查是否已选
            boolean isSelected = hasSelected(course.getId(), studentId);
            vo.setIsSelected(isSelected);

            // 判断是否在选课时间内
            boolean inSelectionPeriod = false;
            if (course.getSelectionStartTime() != null && course.getSelectionEndTime() != null) {
                inSelectionPeriod = now.isAfter(course.getSelectionStartTime()) && now.isBefore(course.getSelectionEndTime());
            }

            // 判断是否满员
            boolean isFull = false;
            if (course.getMaxStudents() > 0 && actualCount >= course.getMaxStudents()) {
                isFull = true;
            }

            // 设置是否可选
            boolean canSelect = false;
            String unavailableReason = null;

            if (!inSelectionPeriod) {
                unavailableReason = "不在选课时间内";
            } else if (isFull) {
                unavailableReason = "课程已满员";
            } else if (isSelected) {
                unavailableReason = "已选该课程";
            } else {
                canSelect = true;
            }

            vo.setCanSelect(canSelect);
            vo.setCanDrop(inSelectionPeriod && isSelected);
            vo.setUnavailableReason(unavailableReason);

            return vo;
        }).toList());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectCourse(Long courseId, Long studentId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 检查是否是选修课
        if (course.getCourseType() != 1) {
            throw new BusinessException("该课程不是选修课");
        }

        // 检查是否在选课时间内
        LocalDateTime now = LocalDateTime.now();
        if (course.getSelectionStartTime() == null || course.getSelectionEndTime() == null) {
            throw new BusinessException("该课程未设置选课时间");
        }
        if (now.isBefore(course.getSelectionStartTime())) {
            throw new BusinessException("选课未开始");
        }
        if (now.isAfter(course.getSelectionEndTime())) {
            throw new BusinessException("选课已结束");
        }

        // 检查是否已选
        if (hasSelected(courseId, studentId)) {
            throw new BusinessException("您已选择该课程");
        }

        // 检查是否满员
        Integer currentCount = studentCourseMapper.countByCourseId(courseId);
        if (course.getMaxStudents() > 0 && currentCount >= course.getMaxStudents()) {
            throw new BusinessException("该课程已满员");
        }

        // 创建选课记录
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentId(studentId);
        studentCourse.setCourseId(courseId);
        studentCourse.setCourseType(1); // 选修课
        studentCourse.setSelectionTime(now);
        studentCourse.setStatus(1);
        studentCourseMapper.insert(studentCourse);

        // 更新课程的当前选课人数
        course.setCurrentStudents(currentCount + 1);
        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dropCourse(Long courseId, Long studentId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 检查是否是选修课
        if (course.getCourseType() != 1) {
            throw new BusinessException("该课程不是选修课");
        }

        // 检查是否在选课时间内（退课也要在选课时间内）
        LocalDateTime now = LocalDateTime.now();
        if (course.getSelectionStartTime() == null || course.getSelectionEndTime() == null) {
            throw new BusinessException("该课程未设置选课时间");
        }
        if (now.isBefore(course.getSelectionStartTime())) {
            throw new BusinessException("选课未开始，无法退课");
        }
        if (now.isAfter(course.getSelectionEndTime())) {
            throw new BusinessException("选课已结束，无法退课");
        }

        // 检查是否已选
        StudentCourse studentCourse = studentCourseMapper.selectOne(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getCourseId, courseId)
                .eq(StudentCourse::getStatus, 1));

        if (studentCourse == null) {
            throw new BusinessException("您未选择该课程");
        }

        // 更新选课记录状态为已退课
        studentCourse.setStatus(0);
        studentCourseMapper.updateById(studentCourse);

        // 更新课程的当前选课人数
        Integer currentCount = studentCourseMapper.countByCourseId(courseId);
        course.setCurrentStudents(currentCount);
        courseMapper.updateById(course);
    }

    @Override
    public boolean hasSelected(Long courseId, Long studentId) {
        Long count = studentCourseMapper.selectCount(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getCourseId, courseId)
                .eq(StudentCourse::getStatus, 1));
        return count > 0;
    }
}
