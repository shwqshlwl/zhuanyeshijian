package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.dto.CourseRequest;
import com.example.citp.model.vo.CourseDetailVO;
import com.example.citp.model.vo.CourseStudentVO;
import com.example.citp.model.vo.CourseVO;

import java.util.List;

/**
 * 课程服务接口
 */
public interface CourseService {

    /**
     * 分页查询课程列表
     * @param type my-我的课程, all-全部课程
     */
    Page<CourseVO> getCourseList(Integer pageNum, Integer pageSize, String keyword, Integer status, String type, Long currentUserId, Integer userType);

    /**
     * 创建课程
     */
    void createCourse(CourseRequest request);

    /**
     * 获取课程详情（包含关联信息）
     */
    CourseDetailVO getCourseById(Long id);

    /**
     * 获取课程基本信息
     */
    CourseVO getCourseBasicInfo(Long id);

    /**
     * 更新课程
     */
    void updateCourse(Long id, CourseRequest request);

    /**
     * 删除课程
     */
    void deleteCourse(Long id);

    /**
     * 更新课程状态
     */
    void updateCourseStatus(Long id, Integer status);

    /**
     * 获取教师的课程列表
     */
    List<CourseVO> getTeacherCourses();

    /**
     * 获取学生的课程列表（通过班级关联）
     */
    List<CourseVO> getStudentCourses();

    /**
     * 获取课程的选课学生列表
     */
    List<CourseStudentVO> getCourseStudents(Long courseId);

    /**
     * 移除课程的选课学生
     */
    void removeStudent(Long courseId, Long studentId);
}
