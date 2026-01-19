package com.example.citp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.citp.model.entity.ClassEntity;
import com.example.citp.model.entity.Course;
import com.example.citp.model.entity.CourseClass;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 课程-班级关联Mapper
 */
@Mapper
public interface CourseClassMapper extends BaseMapper<CourseClass> {

    /**
     * 根据班级ID查询关联的课程列表
     */
    @Select("SELECT c.* FROM course c " +
            "INNER JOIN course_class cc ON c.id = cc.course_id " +
            "WHERE cc.class_id = #{classId} AND c.deleted = 0 " +
            "ORDER BY c.create_time DESC")
    List<Course> selectCoursesByClassId(@Param("classId") Long classId);

    /**
     * 根据课程ID查询关联的班级列表
     */
    @Select("SELECT cl.* FROM class cl " +
            "INNER JOIN course_class cc ON cl.id = cc.class_id " +
            "WHERE cc.course_id = #{courseId} AND cl.deleted = 0 " +
            "ORDER BY cl.create_time DESC")
    List<ClassEntity> selectClassesByCourseId(@Param("courseId") Long courseId);

    /**
     * 删除班级的所有课程关联
     */
    @Delete("DELETE FROM course_class WHERE class_id = #{classId}")
    int deleteByClassId(@Param("classId") Long classId);

    /**
     * 删除课程的所有班级关联
     */
    @Delete("DELETE FROM course_class WHERE course_id = #{courseId}")
    int deleteByCourseId(@Param("courseId") Long courseId);

    /**
     * 批量插入课程-班级关联
     */
    @Insert("<script>" +
            "INSERT INTO course_class (course_id, class_id) VALUES " +
            "<foreach collection='courseIds' item='courseId' separator=','>" +
            "(#{courseId}, #{classId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("classId") Long classId, @Param("courseIds") List<Long> courseIds);
}
