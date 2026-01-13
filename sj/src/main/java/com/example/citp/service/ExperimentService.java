package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.dto.ExperimentRequest;
import com.example.citp.model.dto.ExperimentSubmitRequest;
import com.example.citp.model.vo.ExperimentResultVO;
import com.example.citp.model.vo.ExperimentVO;

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
}
