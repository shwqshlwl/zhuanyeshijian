package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.dto.ClassRequest;
import com.example.citp.model.vo.ClassDetailVO;
import com.example.citp.model.vo.ClassVO;
import com.example.citp.model.vo.UserInfoVO;

import java.util.List;
import java.util.Map;

/**
 * 班级服务接口
 */
public interface ClassService {

    /**
     * 分页查询班级列表
     */
    Page<ClassVO> getClassList(Integer pageNum, Integer pageSize, String grade, String major);

    /**
     * 按年级分组获取班级列表
     */
    Map<String, List<ClassVO>> getClassListGroupByGrade();

    /**
     * 创建班级
     */
    void createClass(ClassRequest request);

    /**
     * 获取班级详情（包含学生列表）
     */
    ClassDetailVO getClassById(Long id);

    /**
     * 获取班级基本信息
     */
    ClassVO getClassBasicInfo(Long id);

    /**
     * 更新班级
     */
    void updateClass(Long id, ClassRequest request);

    /**
     * 删除班级
     */
    void deleteClass(Long id);

    /**
     * 添加学生到班级
     */
    void addStudentToClass(Long classId, Long studentId);

    /**
     * 通过学号添加学生到班级
     */
    void addStudentToClassByStudentNo(Long classId, String studentNo);

    /**
     * 从班级移除学生
     */
    void removeStudentFromClass(Long classId, Long studentId);

    /**
     * 获取班级学生列表
     */
    Page<UserInfoVO> getClassStudents(Long classId, Integer pageNum, Integer pageSize);

    /**
     * 获取课程下的班级列表
     */
    List<ClassVO> getClassesByCourseId(Long courseId);

    /**
     * 关联课程到班级
     */
    void bindCourseToClass(Long classId, Long courseId);
}
