package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.CourseMapper;
import com.example.citp.mapper.QuestionBankMapper;
import com.example.citp.mapper.SysUserMapper;
import com.example.citp.model.dto.BatchImportQuestionRequest;
import com.example.citp.model.dto.QuestionRequest;
import com.example.citp.model.entity.Course;
import com.example.citp.model.entity.QuestionBank;
import com.example.citp.model.entity.SysUser;
import com.example.citp.model.vo.BatchImportResultVO;
import com.example.citp.model.vo.QuestionVO;
import com.example.citp.service.QuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 题库服务实现类
 */
@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements QuestionBankService {

    private final QuestionBankMapper questionBankMapper;
    private final CourseMapper courseMapper;
    private final SysUserMapper sysUserMapper;
    private final JdbcTemplate jdbcTemplate;

    // 题型映射
    private static final Map<Long, String> QUESTION_TYPE_MAP = new LinkedHashMap<>();
    static {
        QUESTION_TYPE_MAP.put(1L, "单选题");
        QUESTION_TYPE_MAP.put(2L, "多选题");
        QUESTION_TYPE_MAP.put(3L, "判断题");
        QUESTION_TYPE_MAP.put(4L, "填空题");
        QUESTION_TYPE_MAP.put(5L, "简答题");
        QUESTION_TYPE_MAP.put(6L, "编程题");
    }

    @Override
    public Page<QuestionVO> getQuestionList(Integer pageNum, Integer pageSize, Long courseId,
                                             Long questionTypeId, Integer difficulty, String keyword) {
        Page<QuestionBank> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(QuestionBank::getCourseId, courseId);
        }
        if (questionTypeId != null) {
            wrapper.eq(QuestionBank::getQuestionTypeId, questionTypeId);
        }
        if (difficulty != null) {
            wrapper.eq(QuestionBank::getDifficulty, difficulty);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(QuestionBank::getQuestionContent, keyword);
        }
        wrapper.orderByDesc(QuestionBank::getCreateTime);

        Page<QuestionBank> questionPage = questionBankMapper.selectPage(page, wrapper);

        Page<QuestionVO> voPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        voPage.setRecords(questionPage.getRecords().stream().map(this::convertToVO).toList());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createQuestion(QuestionRequest request) {
        SysUser currentUser = getCurrentUser();

        QuestionBank question = BeanUtil.copyProperties(request, QuestionBank.class);
        question.setTeacherId(currentUser.getId());
        question.setUsageCount(0);
        if (question.getDifficulty() == null) {
            question.setDifficulty(1);
        }
        if (question.getScore() == null) {
            question.setScore(10);
        }
        questionBankMapper.insert(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchImportResultVO batchImportQuestions(BatchImportQuestionRequest request) {
        SysUser currentUser = getCurrentUser();

        int successCount = 0;
        int failCount = 0;
        List<String> failReasons = new ArrayList<>();

        for (int i = 0; i < request.getQuestions().size(); i++) {
            QuestionRequest item = request.getQuestions().get(i);
            try {
                QuestionBank question = BeanUtil.copyProperties(item, QuestionBank.class);
                question.setTeacherId(currentUser.getId());
                question.setUsageCount(0);
                if (question.getDifficulty() == null) {
                    question.setDifficulty(1);
                }
                if (question.getScore() == null) {
                    question.setScore(10);
                }
                questionBankMapper.insert(question);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failReasons.add("第" + (i + 1) + "题导入失败：" + e.getMessage());
            }
        }

        return new BatchImportResultVO(successCount, failCount, failReasons);
    }

    @Override
    public QuestionVO getQuestionById(Long id) {
        QuestionBank question = questionBankMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        return convertToVO(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(Long id, QuestionRequest request) {
        QuestionBank question = questionBankMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        BeanUtil.copyProperties(request, question, "id", "teacherId", "usageCount");
        questionBankMapper.updateById(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        QuestionBank question = questionBankMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        // 检查是否被考试使用
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exam_question WHERE question_id = ?",
                Integer.class, id);
        if (count != null && count > 0) {
            throw new BusinessException("题目已被考试使用，无法删除");
        }

        questionBankMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteQuestions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Long id : ids) {
            // 检查是否被考试使用
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM exam_question WHERE question_id = ?",
                    Integer.class, id);
            if (count != null && count > 0) {
                QuestionBank question = questionBankMapper.selectById(id);
                throw new BusinessException("题目【" + (question != null ? question.getQuestionContent().substring(0, Math.min(20, question.getQuestionContent().length())) : id) + "...】已被考试使用，无法删除");
            }
        }

        questionBankMapper.deleteBatchIds(ids);
    }

    @Override
    public List<QuestionVO> randomSelectQuestions(Long courseId, Long questionTypeId,
                                                   Integer difficulty, Integer count) {
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(QuestionBank::getCourseId, courseId);
        }
        if (questionTypeId != null) {
            wrapper.eq(QuestionBank::getQuestionTypeId, questionTypeId);
        }
        if (difficulty != null) {
            wrapper.eq(QuestionBank::getDifficulty, difficulty);
        }

        // 查询所有符合条件的题目
        List<QuestionBank> allQuestions = questionBankMapper.selectList(wrapper);

        if (allQuestions.isEmpty()) {
            return new ArrayList<>();
        }

        // 随机抽取
        Collections.shuffle(allQuestions);
        int selectCount = Math.min(count != null ? count : 10, allQuestions.size());
        List<QuestionBank> selectedQuestions = allQuestions.subList(0, selectCount);

        return selectedQuestions.stream().map(this::convertToVO).toList();
    }

    @Override
    public List<Map<String, Object>> getQuestionTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        for (Map.Entry<Long, String> entry : QUESTION_TYPE_MAP.entrySet()) {
            Map<String, Object> type = new HashMap<>();
            type.put("id", entry.getKey());
            type.put("name", entry.getValue());
            types.add(type);
        }
        return types;
    }

    @Override
    public Map<String, Object> getQuestionStatsByCourse(Long courseId) {
        Map<String, Object> stats = new HashMap<>();

        // 总题目数
        Long totalCount = questionBankMapper.selectCount(new LambdaQueryWrapper<QuestionBank>()
                .eq(courseId != null, QuestionBank::getCourseId, courseId));
        stats.put("totalCount", totalCount);

        // 按题型统计
        List<Map<String, Object>> typeStats = new ArrayList<>();
        for (Map.Entry<Long, String> entry : QUESTION_TYPE_MAP.entrySet()) {
            Long count = questionBankMapper.selectCount(new LambdaQueryWrapper<QuestionBank>()
                    .eq(courseId != null, QuestionBank::getCourseId, courseId)
                    .eq(QuestionBank::getQuestionTypeId, entry.getKey()));
            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("typeId", entry.getKey());
            typeStat.put("typeName", entry.getValue());
            typeStat.put("count", count);
            typeStats.add(typeStat);
        }
        stats.put("typeStats", typeStats);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getQuestionStatsByDifficulty(Long courseId) {
        List<Map<String, Object>> stats = new ArrayList<>();

        String[] difficultyNames = {"简单", "中等", "困难"};
        for (int i = 1; i <= 3; i++) {
            Long count = questionBankMapper.selectCount(new LambdaQueryWrapper<QuestionBank>()
                    .eq(courseId != null, QuestionBank::getCourseId, courseId)
                    .eq(QuestionBank::getDifficulty, i));
            Map<String, Object> stat = new HashMap<>();
            stat.put("difficulty", i);
            stat.put("difficultyName", difficultyNames[i - 1]);
            stat.put("count", count);
            stats.add(stat);
        }

        return stats;
    }

    /**
     * 转换为 VO
     */
    private QuestionVO convertToVO(QuestionBank question) {
        QuestionVO vo = BeanUtil.copyProperties(question, QuestionVO.class);

        // 设置题型名称
        vo.setQuestionTypeName(QUESTION_TYPE_MAP.getOrDefault(question.getQuestionTypeId(), "未知"));

        // 查询课程名称
        if (question.getCourseId() != null) {
            Course course = courseMapper.selectById(question.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
        }

        // 查询教师姓名
        if (question.getTeacherId() != null) {
            SysUser teacher = sysUserMapper.selectById(question.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }
        }

        return vo;
    }

    /**
     * 获取当前登录用户
     */
    private SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            throw new BusinessException(401, "未登录");
        }

        UserDetails userDetails = (UserDetails) principal;
        String username = userDetails.getUsername();

        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return user;
    }
}
