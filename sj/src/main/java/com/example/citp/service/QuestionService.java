package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.dto.QuestionRequest;
import com.example.citp.model.entity.QuestionType;
import com.example.citp.model.vo.QuestionVO;

import java.util.List;

/**
 * 题库服务接口
 */
public interface QuestionService {

    /**
     * 获取所有题型
     */
    List<QuestionType> getAllQuestionTypes();

    /**
     * 分页查询题目列表
     */
    Page<QuestionVO> getQuestionList(Integer pageNum, Integer pageSize, Long questionTypeId, Long courseId);

    /**
     * 创建题目
     */
    void createQuestion(QuestionRequest request);

    /**
     * 获取题目详情
     */
    QuestionVO getQuestionById(Long id);

    /**
     * 更新题目
     */
    void updateQuestion(Long id, QuestionRequest request);

    /**
     * 删除题目
     */
    void deleteQuestion(Long id);

    /**
     * 批量删除题目
     */
    void batchDeleteQuestions(List<Long> ids);
}
