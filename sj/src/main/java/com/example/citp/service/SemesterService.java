package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.dto.SemesterRequest;
import com.example.citp.model.vo.SemesterVO;

import java.util.List;

/**
 * 学期服务接口
 */
public interface SemesterService {

    /**
     * 分页查询学期列表
     */
    Page<SemesterVO> getSemesterList(Integer pageNum, Integer pageSize, String keyword, Integer status);

    /**
     * 获取所有学期列表（用于下拉选择）
     */
    List<SemesterVO> getAllSemesters();

    /**
     * 获取当前学期
     */
    SemesterVO getCurrentSemester();

    /**
     * 创建学期
     */
    void createSemester(SemesterRequest request);

    /**
     * 更新学期
     */
    void updateSemester(Long id, SemesterRequest request);

    /**
     * 删除学期
     */
    void deleteSemester(Long id);

    /**
     * 设置当前学期
     */
    void setCurrentSemester(Long id);

    /**
     * 结束学期（将学期状态改为已结束）
     */
    void endSemester(Long id);
}
