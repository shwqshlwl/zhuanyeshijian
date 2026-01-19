package com.example.citp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.citp.model.entity.StudentCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 学生选课 Mapper
 */
@Mapper
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {

    /**
     * 查询课程当前选课人数
     */
    @Select("SELECT COUNT(*) FROM student_course WHERE course_id = #{courseId} AND status = 1 AND deleted = 0")
    Integer countByCourseId(@Param("courseId") Long courseId);
}
