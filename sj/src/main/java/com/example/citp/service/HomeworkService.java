package com.example.citp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.model.dto.HomeworkGradeRequest;
import com.example.citp.model.dto.HomeworkRequest;
import com.example.citp.model.dto.HomeworkSubmitRequest;
import com.example.citp.model.vo.HomeworkDetailVO;
import com.example.citp.model.vo.HomeworkListDetailVO;
import com.example.citp.model.vo.HomeworkSubmitVO;
import com.example.citp.model.vo.HomeworkVO;

/**
 * 作业服务接口
 */
public interface HomeworkService {

    /**
     * 分页查询作业列表
     */
    Page<HomeworkVO> getHomeworkList(Integer pageNum, Integer pageSize, Long courseId, Long classId, Integer status);

    /**
     * 创建作业
     */
    void createHomework(HomeworkRequest request);

    /**
     * 获取作业详情（包含提交统计）
     */
    HomeworkDetailVO getHomeworkById(Long id);

    /**
     * 获取作业基本信息
     */
    HomeworkVO getHomeworkBasicInfo(Long id);

    /**
     * 更新作业
     */
    void updateHomework(Long id, HomeworkRequest request);

    /**
     * 删除作业
     */
    void deleteHomework(Long id);

    /**
     * 提交作业
     */
    void submitHomework(Long homeworkId, HomeworkSubmitRequest request);

    /**
     * 获取作业提交列表
     */
    Page<HomeworkSubmitVO> getHomeworkSubmissions(Long homeworkId, Integer pageNum, Integer pageSize, Integer status);

    /**
     * 获取学生的作业提交
     */
    HomeworkSubmitVO getStudentSubmission(Long homeworkId, Long studentId);

    /**
     * 批改作业
     */
    void gradeHomework(Long homeworkId, Long studentId, HomeworkGradeRequest request);

    /**
     * AI 解析题目
     */
    String aiAnalyzeHomework(Long homeworkId);

    /**
     * 获取学生的作业列表
     */
    Page<HomeworkVO> getStudentHomeworks(Integer pageNum, Integer pageSize, Long courseId, Integer status);
    /**
     * 分页查询带提交比例的作业列表
     */
    Page<HomeworkListDetailVO> getHomeworkListDetail(Integer pageNum, Integer pageSize, Long courseId, Long classId, Integer status);

    /**
     * 更新homework的status
     */
    void refreshHomeworkStatus();
}
