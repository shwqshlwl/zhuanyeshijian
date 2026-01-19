package com.example.citp.service;

import com.example.citp.model.entity.ClassEntity;
import com.example.citp.model.entity.Course;

import java.util.List;

/**
 * 课程-班级关联服务接口
 */
public interface CourseClassService {

    /**
     * 获取班级关联的所有课程
     */
    List<Course> getCoursesByClassId(Long classId);

    /**
     * 获取课程关联的所有班级
     */
    List<ClassEntity> getClassesByCourseId(Long courseId);

    /**
     * 设置班级关联的课程（先删除旧关联，再建立新关联）
     */
    void setClassCourses(Long classId, List<Long> courseIds);

    /**
     * 删除班级的所有课程关联
     */
    void deleteByClassId(Long classId);

    /**
     * 删除课程的所有班级关联
     */
    void deleteByCourseId(Long courseId);
}
