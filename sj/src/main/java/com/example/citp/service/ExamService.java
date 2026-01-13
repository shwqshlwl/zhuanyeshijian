package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.dto.ExamRequest;
import com.example.citp.model.dto.ExamSubmitRequest;
import com.example.citp.model.vo.ExamVO;

import java.util.Map;

/**
 * 考试服务接口
 */
public interface ExamService {

    /**
     * 分页查询考试列表
     */
    Page<ExamVO> getExamList(Integer pageNum, Integer pageSize, Long courseId, Integer status);

    /**
     * 创建考试
     */
    void createExam(ExamRequest request);

    /**
     * 获取考试详情
     */
    ExamVO getExamById(Long id);

    /**
     * 更新考试
     */
    void updateExam(Long id, ExamRequest request);

    /**
     * 删除考试
     */
    void deleteExam(Long id);

    /**
     * 开始考试
     */
    Map<String, Object> startExam(Long examId);

    /**
     * 提交考试
     */
    void submitExam(Long examId, ExamSubmitRequest request);

    /**
     * 考试结果分析
     */
    Map<String, Object> getExamAnalysis(Long examId);
}
