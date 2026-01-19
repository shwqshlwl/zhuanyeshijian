package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.dto.ExperimentRequest;
import com.example.citp.model.dto.ExperimentSubmitRequest;
import com.example.citp.model.vo.ExperimentResultVO;
import com.example.citp.model.vo.ExperimentVO;

import java.util.List;
import java.util.Map;

/**
 * 实验服务接口
 */
public interface ExperimentService {

    /**
     * 分页查询实验列表
     */
    Page<ExperimentVO> getExperimentList(Integer pageNum, Integer pageSize, Long courseId);

    /**
     * 创建实验
     */
    void createExperiment(ExperimentRequest request);

    /**
     * 获取实验详情
     */
    ExperimentVO getExperimentById(Long id);

    /**
     * 更新实验
     */
    void updateExperiment(Long id, ExperimentRequest request);

    /**
     * 删除实验
     */
    void deleteExperiment(Long id);

    /**
     * 提交实验代码
     */
    void submitExperiment(Long experimentId, ExperimentSubmitRequest request);

    /**
     * 获取实验结果
     */
    ExperimentResultVO getExperimentResult(Long experimentId);

    /**
     * 运行测试代码
     */
    Map<String, Object> runCode(Long experimentId, String code, String language, String input);

    /**
     * 获取我的提交历史
     */
    List<ExperimentResultVO> getMySubmissions(Long experimentId);

    /**
     * 获取学生提交列表（教师）
     */
    Page<ExperimentResultVO> getSubmissions(Long experimentId, Integer pageNum, Integer pageSize);

    /**
     * 获取学生的实验提交详情（教师查看）
     */
    ExperimentResultVO getStudentSubmission(Long experimentId, Long studentId);

    /**
     * 获取实验提交统计（教师）
     */
    com.example.citp.model.vo.ExperimentSubmitStatVO getSubmitStatistics(Long experimentId);
}
