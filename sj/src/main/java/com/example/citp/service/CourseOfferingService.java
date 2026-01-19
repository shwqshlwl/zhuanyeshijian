package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.request.CourseOfferingRequest;
import com.example.citp.model.vo.CourseOfferingVO;
import com.example.citp.model.vo.CourseTemplateVO;

import java.util.List;

/**
 * 开课实例服务接口
 */
public interface CourseOfferingService {

    /**
     * 查询教师的课程模板列表（含指定学期开课状态）
     */
    Page<CourseTemplateVO> getTeacherCourseTemplates(Long teacherId, Long semesterId, Integer pageNum, Integer pageSize, String keyword);

    /**
     * 创建开课实例
     */
    void createOffering(CourseOfferingRequest request, Long currentUserId);

    /**
     * 获取开课实例详情
     */
    CourseOfferingVO getOfferingDetail(Long offeringId, Long currentUserId);

    /**
     * 更新开课实例
     */
    void updateOffering(Long offeringId, CourseOfferingRequest request, Long currentUserId);

    /**
     * 删除开课实例
     */
    void deleteOffering(Long offeringId, Long currentUserId);

    /**
     * 查询学期的开课实例列表
     */
    Page<CourseOfferingVO> getOfferingsBySemester(Long semesterId, Integer pageNum, Integer pageSize, String keyword);

    /**
     * 查询教师的开课实例列表
     */
    Page<CourseOfferingVO> getTeacherOfferings(Long teacherId, Long semesterId, Integer pageNum, Integer pageSize, String keyword);

    /**
     * 检查课程在指定学期是否已开课
     */
    boolean isOfferedInSemester(Long courseId, Long semesterId);
}
