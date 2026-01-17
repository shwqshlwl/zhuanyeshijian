package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.*;
import com.example.citp.model.dto.ExamRequest;
import com.example.citp.model.dto.ExamStartResponseDTO;
import com.example.citp.model.dto.ExamSubmitRequest;
import com.example.citp.model.dto.QuestionDTO;
import com.example.citp.model.entity.*;
import com.example.citp.model.vo.ExamVO;
import com.example.citp.service.ExamService;
import lombok.RequiredArgsConstructor;
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

            // 根据当前时间动态计算考试状态
            LocalDateTime now = LocalDateTime.now();
            Integer dynamicStatus;
            if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
                dynamicStatus = 0; // 未开始
            } else if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
                dynamicStatus = 2; // 已结束
            } else {
                dynamicStatus = 1; // 进行中
            }
            vo.setStatus(dynamicStatus);

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

        // 根据当前时间动态计算考试状态
        LocalDateTime now = LocalDateTime.now();
        Integer dynamicStatus;
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            dynamicStatus = 0; // 未开始
        } else if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            dynamicStatus = 2; // 已结束
        } else {
            dynamicStatus = 1; // 进行中
        }
        vo.setStatus(dynamicStatus);

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
    public ExamStartResponseDTO startExam(Long examId) {
        try {
            System.out.println("DEBUG: startExam 开始执行，考试ID: " + examId);

            // 检查考试是否存在
            Exam exam = examMapper.selectById(examId);
            if (exam == null) {
                throw new BusinessException("考试不存在");
            }

            System.out.println("DEBUG: 考试存在，考试信息: " + exam);

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

        // 使用已有的 getExamQuestions 方法获取题目，然后格式化以适配前端
        System.out.println("DEBUG: 开始获取考试题目，考试ID: " + examId);
        List<Map<String, Object>> rawQuestions = getExamQuestions(examId);
        System.out.println("DEBUG: 考试ID: " + examId + ", 获取到的原始题目数量: " + rawQuestions.size());

        List<QuestionDTO> questions = new ArrayList<>();

        for (Map<String, Object> raw : rawQuestions) {
            QuestionDTO question = new QuestionDTO();
            question.setId((Long) raw.get("question_id"));
            question.setContent(raw.get("content") != null ? (String) raw.get("content") : "");
            question.setScore(raw.get("score") != null ? (Integer) raw.get("score") : 0);
            question.setTypeName(raw.get("typeName") != null ? (String) raw.get("typeName") : "未知题型");

            // 根据题型ID确定题目类型
            Object questionTypeIdObj = raw.get("question_type_id");
            Integer questionTypeId = questionTypeIdObj != null ? ((Number) questionTypeIdObj).intValue() : 0;
            String type = "TEXT"; // 默认文本题
            switch (questionTypeId) {
                case 1:
                    type = "SINGLE"; // 单选题
                    break;
                case 2:
                    type = "MULTIPLE"; // 多选题
                    break;
                case 3:
                    type = "TRUE_FALSE"; // 判断题
                    break;
                case 4:
                    type = "FILL_BLANK"; // 填空题
                    break;
                case 5:
                    type = "SHORT_ANSWER"; // 简答题
                    break;
                case 6:
                    type = "PROGRAMMING"; // 编程题
                    break;
                default:
                    type = "TEXT"; // 其他题型
            }
            question.setType(type);

            // 前端固定显示ABCD选项，后端不需要返回选项数据
            question.setOptions(null);

            questions.add(question);
        }

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

            // 转换 Exam 为 ExamVO
            ExamVO examVO = BeanUtil.copyProperties(exam, ExamVO.class);

            // 根据当前时间动态计算考试状态
            LocalDateTime now1 = LocalDateTime.now();
            Integer dynamicStatus;
            if (exam.getStartTime() != null && now1.isBefore(exam.getStartTime())) {
                dynamicStatus = 0; // 未开始
            } else if (exam.getEndTime() != null && now1.isAfter(exam.getEndTime())) {
                dynamicStatus = 2; // 已结束
            } else {
                dynamicStatus = 1; // 进行中
            }
            examVO.setStatus(dynamicStatus);

            // 查询课程名称
            Course course = courseMapper.selectById(exam.getCourseId());
            if (course != null) {
                examVO.setCourseName(course.getCourseName());
            }

            // 查询班级名称
            if (exam.getClassId() != null) {
                ClassEntity classEntity = classMapper.selectById(exam.getClassId());
                if (classEntity != null) {
                    examVO.setClassName(classEntity.getClassName());
                } else {
                    examVO.setClassName("班级已删除");
                }
            }

            // 查询教师姓名
            SysUser teacher = sysUserMapper.selectById(exam.getTeacherId());
            if (teacher != null) {
                examVO.setTeacherName(teacher.getRealName());
            }

            ExamStartResponseDTO result = new ExamStartResponseDTO(examVO, questions);

            System.out.println("DEBUG: startExam 执行成功，返回题目数量: " + questions.size());
            return result;

        } catch (Exception e) {
            System.err.println("DEBUG: startExam 执行异常: " + e.getMessage());
            e.printStackTrace();
            throw e; // 重新抛出异常
        }
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
            if (question.getCorrectAnswer() != null && studentAnswer != null) {
                String correctAnswer = question.getCorrectAnswer().trim();
                String studentAns = studentAnswer.trim();

                // 对于多选题，需要比较排序后的答案
                boolean isCorrect = false;
                if (question.getQuestionTypeId() == 2) { // 多选题
                    // 将答案按逗号分割，排序后比较
                    String[] correctParts = correctAnswer.split(",");
                    String[] studentParts = studentAns.split(",");
                    Arrays.sort(correctParts);
                    Arrays.sort(studentParts);
                    isCorrect = Arrays.equals(correctParts, studentParts);
                } else {
                    // 其他题型直接比较
                    isCorrect = correctAnswer.equalsIgnoreCase(studentAns);
                }

                if (isCorrect) {
                    totalScore = totalScore.add(BigDecimal.valueOf(questionScore));
                }
            }
        }

        return totalScore;
    }

    @Override
    public java.util.List<Map<String, Object>> getExamQuestions(Long examId) {
        System.out.println("DEBUG: 开始查询考试题目，考试ID: " + examId);
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                    "SELECT eq.id, eq.question_id, eq.question_score as score, eq.question_order, " +
                    "qb.question_content as content, qb.options, qb.correct_answer, qb.analysis, " +
                    "qb.difficulty, qb.question_type_id, qt.type_name as typeName " +
                    "FROM exam_question eq " +
                    "LEFT JOIN question_bank qb ON eq.question_id = qb.id " +
                    "LEFT JOIN question_type qt ON qb.question_type_id = qt.id " +
                    "WHERE eq.exam_id = ? " +
                    "ORDER BY eq.question_order", examId);

            System.out.println("DEBUG: 考试题目查询完成，数量: " + result.size());
            return result;
        } catch (Exception e) {
            System.err.println("DEBUG: 查询考试题目异常: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // 返回空列表避免阻塞
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addQuestionsToExam(Long examId, java.util.List<Map<String, Object>> questions) {
        System.out.println("DEBUG: addQuestionsToExam - 考试ID: " + examId + ", 题目数量: " + questions.size());

        // 检查考试是否存在
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 获取当前最大题目顺序
        Integer maxOrder = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(question_order), 0) FROM exam_question WHERE exam_id = ?",
                Integer.class, examId);

        System.out.println("DEBUG: 当前最大题目顺序: " + maxOrder);

        // 添加题目
        for (Map<String, Object> question : questions) {
            System.out.println("DEBUG: 添加题目数据: " + question);

            Long questionId = Long.valueOf(question.get("questionId").toString());
            Integer score = Integer.valueOf(question.get("score").toString());

            System.out.println("DEBUG: 插入数据 - examId: " + examId + ", questionId: " + questionId + ", score: " + score + ", order: " + (++maxOrder));

            jdbcTemplate.update(
                    "INSERT INTO exam_question (exam_id, question_id, question_score, question_order) VALUES (?, ?, ?, ?)",
                    examId, questionId, score, maxOrder);
        }

        System.out.println("DEBUG: 题目添加完成");
        // 根据当前 exam_question 统计总分并写回 exam 表；及格分按 60% 计算并写回
        try {
            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(question_score), 0) FROM exam_question WHERE exam_id = ?",
                    Integer.class, examId);
            if (total == null) total = 0;
            int pass = (int) Math.round(total * 0.6);
            // 使用实体更新，保持与 Exam.java 字段一致
            exam.setTotalScore(total);
            exam.setPassScore(pass);
            examMapper.updateById(exam);
            System.out.println("DEBUG: 已更新考试总分和及格分（实体更新），total=" + total + ", pass=" + pass);
        } catch (Exception e) {
            System.err.println("DEBUG: 更新考试总分失败: " + e.getMessage());
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

        // 如果考试关联了班级，则应考人数为该班级学生总数（优先使用 student_class 表统计）
        if (exam.getClassId() != null) {
            Integer classTotal = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM student_class WHERE class_id = ?",
                    Integer.class, exam.getClassId());
            if (classTotal != null && !statistics.isEmpty()) {
                statistics.get(0).put("total", classTotal);
            } else if (classTotal != null && statistics.isEmpty()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("total", classTotal);
                stat.put("submitted", 0);
                stat.put("avgScore", null);
                stat.put("maxScore", null);
                stat.put("minScore", null);
                statistics = new ArrayList<>();
                statistics.add(stat);
            }
        }

        // 计算及格率：基于已提交（status=2）学生的得分与考试及格分比较
        if (!statistics.isEmpty()) {
            Map<String, Object> stat = statistics.get(0);
            Integer submitted = stat.get("submitted") instanceof Number ? ((Number) stat.get("submitted")).intValue() : 0;
            int passCount = 0;
            if (submitted > 0) {
                try {
                    Integer pc = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM exam_record WHERE exam_id = ? AND status = 2 AND score >= ?",
                            Integer.class, examId, exam.getPassScore());
                    passCount = pc != null ? pc : 0;
                } catch (Exception ignored) {
                    passCount = 0;
                }
            }
            double passRate = submitted > 0 ? ((double) passCount / submitted) * 100.0 : 0.0;
            stat.put("passRate", passRate);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("statistics", statistics.isEmpty() ? null : statistics.get(0));
        return result;
    }

    @Override
    public java.util.Map<String, Object> getStudentRecord(Long examId, Long studentId) {
        // 验证考试是否存在
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 查询考试记录（学生）
        ExamRecord record = examRecordMapper.selectOne(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getExamId, examId)
                .eq(ExamRecord::getStudentId, studentId));

        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }

        // 解析答案 JSON 为 Map<String,Object>
        java.util.Map<String, Object> answersMap = new HashMap<>();
        if (record.getAnswers() != null && !record.getAnswers().trim().isEmpty()) {
            try {
                answersMap = JSONUtil.toBean(record.getAnswers(), java.util.Map.class);
            } catch (Exception ignored) {
                answersMap = new HashMap<>();
            }
        }

        // 查询题目信息（包含分值）
        List<Map<String, Object>> questions = jdbcTemplate.queryForList(
                "SELECT eq.question_id, eq.question_score, qb.question_content, qb.correct_answer " +
                "FROM exam_question eq INNER JOIN question_bank qb ON eq.question_id = qb.id " +
                "WHERE eq.exam_id = ? ORDER BY eq.question_order", examId);

        List<java.util.Map<String, Object>> answerList = new ArrayList<>();
        for (Map<String, Object> q : questions) {
            Long qid = ((Number) q.get("question_id")).longValue();
            Integer qscore = q.get("question_score") == null ? 0 : ((Number) q.get("question_score")).intValue();
            String content = q.get("question_content") != null ? q.get("question_content").toString() : "";
            String correct = q.get("correct_answer") != null ? q.get("correct_answer").toString() : "";

            String studentAns = answersMap.containsKey(String.valueOf(qid)) ? String.valueOf(answersMap.get(String.valueOf(qid))) :
                    (answersMap.containsKey(qid) ? String.valueOf(answersMap.get(qid)) : null);
            boolean isCorrect = false;
            int getScore = 0;
            if (studentAns != null && correct != null) {
                if (correct.equalsIgnoreCase(studentAns.trim())) {
                    isCorrect = true;
                    getScore = qscore;
                } else {
                    String[] correctParts = correct.split(",");
                    String[] stuParts = studentAns.split(",");
                    Arrays.sort(correctParts);
                    Arrays.sort(stuParts);
                    if (Arrays.equals(correctParts, stuParts)) {
                        isCorrect = true;
                        getScore = qscore;
                    }
                }
            }

            java.util.Map<String, Object> item = new HashMap<>();
            item.put("questionContent", content);
            item.put("questionScore", qscore);
            item.put("studentAnswer", studentAns);
            item.put("correctAnswer", correct);
            item.put("getScore", getScore);
            item.put("isCorrect", isCorrect);
            answerList.add(item);
        }

        java.util.Map<String, Object> result = new HashMap<>();
        SysUser student = sysUserMapper.selectById(studentId);
        result.put("studentName", student != null ? student.getRealName() : "");
        result.put("studentNo", student != null ? student.getStudentNo() : "");
        result.put("score", record.getScore());
        result.put("submitTime", record.getSubmitTime());
        result.put("answers", answerList);
        return result;
    }

    @Override
    public java.util.List<Map<String, Object>> getStudentExams() {
        // 获取当前学生
        SysUser currentUser = getCurrentUser();
        if (currentUser.getUserType() != 1) {
            throw new BusinessException("只有学生可以查看自己的考试");
        }

        // 查询学生的考试记录
        List<ExamRecord> records = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, currentUser.getId())
                        .orderByDesc(ExamRecord::getCreateTime)
        );

        // 获取相关的考试信息
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamRecord record : records) {
            Exam exam = examMapper.selectById(record.getExamId());
            if (exam == null) continue;

            Map<String, Object> examData = new HashMap<>();
            examData.put("id", exam.getId());
            examData.put("examName", exam.getExamName());

            // 查询课程名称
            Course course = courseMapper.selectById(exam.getCourseId());
            examData.put("courseName", course != null ? course.getCourseName() : "");

            examData.put("startTime", exam.getStartTime());
            examData.put("endTime", exam.getEndTime());
            examData.put("duration", exam.getDuration());

            // 考试记录信息
            examData.put("recordId", record.getId());
            examData.put("submitted", record.getStatus() == 2);
            examData.put("score", record.getScore());
            examData.put("submitTime", record.getSubmitTime());
            examData.put("startTime", record.getStartTime());

            result.add(examData);
        }

        // 还需要添加学生可以参加但还没有记录的考试（即未开始的考试）
        // 查询学生所属班级的考试
        List<Map<String, Object>> studentClasses = jdbcTemplate.queryForList(
                "SELECT class_id FROM student_class WHERE student_id = ?",
                currentUser.getId()
        );

        if (!studentClasses.isEmpty()) {
            List<Long> classIds = studentClasses.stream()
                    .map(row -> (Long) row.get("class_id"))
                    .toList();

            List<Exam> availableExams = examMapper.selectList(
                    new LambdaQueryWrapper<Exam>()
                            .and(wrapper -> wrapper.in(Exam::getClassId, classIds)
                                    .or().isNull(Exam::getClassId))
                            .orderByDesc(Exam::getCreateTime)
            );

            for (Exam exam : availableExams) {
                // 检查是否已经有记录
                boolean hasRecord = records.stream()
                        .anyMatch(record -> record.getExamId().equals(exam.getId()));

                if (!hasRecord) {
                    Map<String, Object> examData = new HashMap<>();
                    examData.put("id", exam.getId());
                    examData.put("examName", exam.getExamName());

                    // 查询课程名称
                    Course course = courseMapper.selectById(exam.getCourseId());
                    examData.put("courseName", course != null ? course.getCourseName() : "");

                    examData.put("startTime", exam.getStartTime());
                    examData.put("endTime", exam.getEndTime());
                    examData.put("duration", exam.getDuration());

                    // 未参加考试的标记
                    examData.put("submitted", false);
                    examData.put("score", null);
                    examData.put("submitTime", null);
                    examData.put("startTime", null);

                    result.add(examData);
                }
            }
        }

        return result;
    }
}
