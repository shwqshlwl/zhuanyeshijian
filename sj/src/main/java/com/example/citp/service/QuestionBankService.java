package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.dto.BatchImportQuestionRequest;
import com.example.citp.model.dto.QuestionRequest;
import com.example.citp.model.vo.BatchImportResultVO;
import com.example.citp.model.vo.QuestionVO;

import java.util.List;
import java.util.Map;

/**
 * 题库服务接口
 */
public interface QuestionBankService {

    /**
     * 分页查询题目列表
     */
    Page<QuestionVO> getQuestionList(Integer pageNum, Integer pageSize, Long courseId, 
                                      Long questionTypeId, Integer difficulty, String keyword);

    /**
     * 创建题目
     */
    void createQuestion(QuestionRequest request);

    /**
     * 批量导入题目
     */
    BatchImportResultVO batchImportQuestions(BatchImportQuestionRequest request);

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

    /**
     * 随机抽取题目
     */
    List<QuestionVO> randomSelectQuestions(Long courseId, Long questionTypeId, 
                                            Integer difficulty, Integer count);

    /**
     * 获取题型列表
     */
    List<Map<String, Object>> getQuestionTypes();

    /**
     * 按课程统计题目数量
     */
    Map<String, Object> getQuestionStatsByCourse(Long courseId);

    /**
     * 按难度统计题目数量
     */
    List<Map<String, Object>> getQuestionStatsByDifficulty(Long courseId);
}
