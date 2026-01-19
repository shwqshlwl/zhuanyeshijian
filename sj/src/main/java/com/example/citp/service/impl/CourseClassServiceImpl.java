package com.example.citp.service.impl;

import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.CourseClassMapper;
import com.example.citp.model.entity.ClassEntity;
import com.example.citp.model.entity.Course;
import com.example.citp.service.CourseClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程-班级关联服务实现类
 */
@Service
@RequiredArgsConstructor
public class CourseClassServiceImpl implements CourseClassService {

    private final CourseClassMapper courseClassMapper;

    @Override
    public List<Course> getCoursesByClassId(Long classId) {
        if (classId == null) {
            return new ArrayList<>();
        }
        return courseClassMapper.selectCoursesByClassId(classId);
    }

    @Override
    public List<ClassEntity> getClassesByCourseId(Long courseId) {
        if (courseId == null) {
            return new ArrayList<>();
        }
        return courseClassMapper.selectClassesByCourseId(courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setClassCourses(Long classId, List<Long> courseIds) {
        if (classId == null) {
            throw new BusinessException("班级ID不能为空");
        }

        // 删除旧关联
        courseClassMapper.deleteByClassId(classId);

        // 建立新关联
        if (courseIds != null && !courseIds.isEmpty()) {
            courseClassMapper.batchInsert(classId, courseIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByClassId(Long classId) {
        if (classId != null) {
            courseClassMapper.deleteByClassId(classId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCourseId(Long courseId) {
        if (courseId != null) {
            courseClassMapper.deleteByCourseId(courseId);
        }
    }
}
