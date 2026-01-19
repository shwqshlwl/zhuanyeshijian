package com.example.citp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.citp.model.entity.CourseOffering;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 开课实例Mapper接口
 */
@Mapper
public interface CourseOfferingMapper extends BaseMapper<CourseOffering> {

    /**
     * 根据课程ID和学期ID查询开课实例
     */
    @Select("SELECT * FROM course_offering WHERE course_id = #{courseId} AND semester_id = #{semesterId} AND deleted = 0")
    CourseOffering selectByCourseAndSemester(Long courseId, Long semesterId);

    /**
     * 查询课程的所有开课实例（按学期倒序）
     */
    @Select("SELECT co.*, s.semester_name, s.academic_year FROM course_offering co " +
            "LEFT JOIN semester s ON co.semester_id = s.id " +
            "WHERE co.course_id = #{courseId} AND co.deleted = 0 " +
            "ORDER BY s.start_date DESC")
    List<CourseOffering> selectOfferingsByCourseId(Long courseId);
}
