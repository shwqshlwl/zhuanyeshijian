package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.*;
import com.example.citp.model.dto.ExamRequest;
import com.example.citp.model.dto.ExamSubmitRequest;
import com.example.citp.model.entity.*;
import com.example.citp.model.vo.ExamVO;
import com.example.citp.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 考试服务实现类
 */
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;
    private final ExamRecordMapper examRecordMapper;
    private final CourseMapper courseMapper;
    private final ClassMapper classMapper;
    private final SysUserMapper sysUserMapper;
    private final QuestionBankMapper questionBankMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<ExamVO> getExamList(Integer pageNum, Integer pageSize, Long courseId, Integer status) {
        Page<Exam> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Exam::getCourseId, courseId);
        }
        if (status != null) {
            wrapper.eq(Exam::getStatus, status);
        }
        wrapper.orderByDesc(Exam::getCreateTime);

        Page<Exam> examPage = examMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        Page<ExamVO> voPage = new Page<>(examPage.getCurrent(), examPage.getSize(), examPage.getTotal());
        voPage.setRecords(examPage.getRecords().stream().map(exam -> {
            ExamVO vo = BeanUtil.copyProperties(exam, ExamVO.class);
            
            // 查询课程名称
            Course course = courseMapper.selectById(exam.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
            
            // 查询班级名称
            if (exam.getClassId() != null) {
                ClassEntity classEntity = classMapper.selectById(exam.getClassId());
                if (classEntity != null) {
                    vo.setClassName(classEntity.getClassName());
                }
            }
            
            // 查询教师姓名
            SysUser teacher = sysUserMapper.selectById(exam.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }
            
            return vo;
        }).toList());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createExam(ExamRequest request) {
        SysUser currentUser = getCurrentUser();

        Exam exam = BeanUtil.copyProperties(request, Exam.class);
        exam.setTeacherId(currentUser.getId());
        examMapper.insert(exam);

        // 保存考试题目关联
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            for (ExamRequest.ExamQuestionItem item : request.getQuestions()) {
                jdbcTemplate.update(
                        "INSERT INTO exam_question (exam_id, question_id, question_score, question_order) VALUES (?, ?, ?, ?)",
                        exam.getId(), item.getQuestionId(), item.getQuestionScore(), item.getQuestionOrder());
            }
        }
    }

    @Override
    public ExamVO getExamById(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        ExamVO vo = BeanUtil.copyProperties(exam, ExamVO.class);

        // 查询课程名称
        Course course = courseMapper.selectById(exam.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getCourseName());
        }

        // 查询班级名称
        if (exam.getClassId() != null) {
            ClassEntity classEntity = classMapper.selectById(exam.getClassId());
            if (classEntity != null) {
                vo.setClassName(classEntity.getClassName());
            } else {
                // 班级记录不存在，设置默认名称
                vo.setClassName("班级已删除");
            }
        }
        
        // 查询教师姓名
        SysUser teacher = sysUserMapper.selectById(exam.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExam(Long id, ExamRequest request) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        BeanUtil.copyProperties(request, exam, "id", "teacherId");
        examMapper.updateById(exam);

        // 更新考试题目关联
        jdbcTemplate.update("DELETE FROM exam_question WHERE exam_id = ?", id);
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            for (ExamRequest.ExamQuestionItem item : request.getQuestions()) {
                jdbcTemplate.update(
                        "INSERT INTO exam_question (exam_id, question_id, question_score, question_order) VALUES (?, ?, ?, ?)",
                        id, item.getQuestionId(), item.getQuestionScore(), item.getQuestionOrder());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        examMapper.deleteById(id);
        jdbcTemplate.update("DELETE FROM exam_question WHERE exam_id = ?", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startExam(Long examId) {
        // 检查考试是否存在
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 检查考试时间
        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            throw new BusinessException("考试尚未开始");
        }
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            throw new BusinessException("考试已结束");
        }

        // 获取当前学生
        SysUser currentUser = getCurrentUser();
        if (currentUser.getUserType() != 1) {
            throw new BusinessException("只有学生可以参加考试");
        }

        // 检查是否已参加
        ExamRecord existRecord = examRecordMapper.selectOne(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getExamId, examId)
                .eq(ExamRecord::getStudentId, currentUser.getId()));

        if (existRecord != null && existRecord.getStatus() == 2) {
            throw new BusinessException("已提交考试，不能重复参加");
        }

        // 查询考试题目
        List<Map<String, Object>> questions = jdbcTemplate.queryForList(
                "SELECT eq.question_id, eq.question_score, eq.question_order, " +
                "qb.question_content, qb.options, qb.question_type_id " +
                "FROM exam_question eq " +
                "INNER JOIN question_bank qb ON eq.question_id = qb.id " +
                "WHERE eq.exam_id = ? ORDER BY eq.question_order", examId);

        // 创建或更新考试记录
        if (existRecord == null) {
            ExamRecord record = new ExamRecord();
            record.setExamId(examId);
            record.setStudentId(currentUser.getId());
            record.setStartTime(now);
            record.setStatus(1);
            record.setIsTimeout(0);
            examRecordMapper.insert(record);
        } else {
            existRecord.setStartTime(now);
            existRecord.setStatus(1);
            examRecordMapper.updateById(existRecord);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("questions", questions);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExam(Long examId, ExamSubmitRequest request) {
        // 获取当前学生
        SysUser currentUser = getCurrentUser();

        // 查询考试记录
        ExamRecord record = examRecordMapper.selectOne(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getExamId, examId)
                .eq(ExamRecord::getStudentId, currentUser.getId()));

        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }

        if (record.getStatus() == 2) {
            throw new BusinessException("已提交考试，不能重复提交");
        }

        // 计算得分
        BigDecimal totalScore = calculateScore(examId, request.getAnswers());

        // 更新考试记录
        record.setAnswers(JSONUtil.toJsonStr(request.getAnswers()));
        record.setScore(totalScore);
        record.setSubmitTime(LocalDateTime.now());
        record.setStatus(2);
        examRecordMapper.updateById(record);
    }

    @Override
    public Map<String, Object> getExamAnalysis(Long examId) {
        // 查询考试信息
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 查询考试记录统计
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT COUNT(*) as total, " +
                "AVG(score) as avgScore, " +
                "MAX(score) as maxScore, " +
                "MIN(score) as minScore " +
                "FROM exam_record WHERE exam_id = ? AND status = 2", examId);

        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("statistics", records.isEmpty() ? null : records.get(0));
        return result;
    }

    /**
     * 计算考试得分
     */
    private BigDecimal calculateScore(Long examId, Map<Long, String> answers) {
        if (answers == null || answers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalScore = BigDecimal.ZERO;

        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            String studentAnswer = entry.getValue();

            // 查询题目信息
            QuestionBank question = questionBankMapper.selectById(questionId);
            if (question == null) {
                continue;
            }

            // 查询题目分值
            Integer questionScore = jdbcTemplate.queryForObject(
                    "SELECT question_score FROM exam_question WHERE exam_id = ? AND question_id = ?",
                    Integer.class, examId, questionId);

            if (questionScore == null) {
                continue;
            }

            // 判断答案是否正确
            if (question.getCorrectAnswer() != null && 
                question.getCorrectAnswer().trim().equalsIgnoreCase(studentAnswer.trim())) {
                totalScore = totalScore.add(BigDecimal.valueOf(questionScore));
            }
        }

        return totalScore;
    }

    @Override
    public java.util.List<Map<String, Object>> getExamQuestions(Long examId) {
        return jdbcTemplate.queryForList(
                "SELECT eq.id, eq.question_id, eq.question_score as score, eq.question_order, " +
                "qb.question_content as content, qb.options, qb.correct_answer, qb.analysis, " +
                "qb.difficulty, qb.question_type_id, qt.type_name as typeName " +
                "FROM exam_question eq " +
                "INNER JOIN question_bank qb ON eq.question_id = qb.id " +
                "INNER JOIN question_type qt ON qb.question_type_id = qt.id " +
                "WHERE eq.exam_id = ? " +
                "ORDER BY eq.question_order", examId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addQuestionsToExam(Long examId, java.util.List<Map<String, Object>> questions) {
        // 检查考试是否存在
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 获取当前最大题目顺序
        Integer maxOrder = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(question_order), 0) FROM exam_question WHERE exam_id = ?",
                Integer.class, examId);

        // 添加题目
        for (Map<String, Object> question : questions) {
            Long questionId = Long.valueOf(question.get("questionId").toString());
            Integer score = Integer.valueOf(question.get("score").toString());

            jdbcTemplate.update(
                    "INSERT INTO exam_question (exam_id, question_id, question_score, question_order) VALUES (?, ?, ?, ?)",
                    examId, questionId, score, ++maxOrder);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeQuestionFromExam(Long examId, Long questionId) {
        jdbcTemplate.update(
                "DELETE FROM exam_question WHERE exam_id = ? AND id = ?",
                examId, questionId);
    }

    /**
     * 获取当前登录用户
     */
    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return user;
    }

    @Override
    public Map<String, Object> getExamRecords(Long examId) {
        // 验证考试是否存在
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 查询所有考试记录
        List<ExamRecord> records = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .orderByDesc(ExamRecord::getSubmitTime)
        );

        // 查询统计信息
        List<Map<String, Object>> statistics = jdbcTemplate.queryForList(
                "SELECT COUNT(*) as total, " +
                "COUNT(CASE WHEN status = 2 THEN 1 END) as submitted, " +
                "AVG(CASE WHEN status = 2 THEN score END) as avgScore, " +
                "MAX(CASE WHEN status = 2 THEN score END) as maxScore, " +
                "MIN(CASE WHEN status = 2 THEN score END) as minScore " +
                "FROM exam_record WHERE exam_id = ?", examId);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("statistics", statistics.isEmpty() ? null : statistics.get(0));
        return result;
    }
}
