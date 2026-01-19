package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.vo.ElectiveCourseVO;

/**
 * 学生选课服务接口
 */
public interface StudentCourseService {

    /**
     * 获取选课中心课程列表（仅选修课）
     *
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param keyword    搜索关键词
     * @param studentId  学生ID
     * @return 选修课列表
     */
    Page<ElectiveCourseVO> getElectiveCourseList(Integer pageNum, Integer pageSize, String keyword, Long studentId);

    /**
     * 学生选课
     *
     * @param courseId  课程ID
     * @param studentId 学生ID
     */
    void selectCourse(Long courseId, Long studentId);

    /**
     * 学生退课
     *
     * @param courseId  课程ID
     * @param studentId 学生ID
     */
    void dropCourse(Long courseId, Long studentId);

    /**
     * 检查学生是否已选某课程
     *
     * @param courseId  课程ID
     * @param studentId 学生ID
     * @return 是否已选
     */
    boolean hasSelected(Long courseId, Long studentId);
}
