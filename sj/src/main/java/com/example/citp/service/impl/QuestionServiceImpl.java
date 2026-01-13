package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.CourseMapper;
import com.example.citp.mapper.QuestionBankMapper;
import com.example.citp.mapper.QuestionTypeMapper;
import com.example.citp.mapper.SysUserMapper;
import com.example.citp.model.dto.QuestionRequest;
import com.example.citp.model.entity.Course;
import com.example.citp.model.entity.QuestionBank;
import com.example.citp.model.entity.QuestionType;
import com.example.citp.model.entity.SysUser;
import com.example.citp.model.vo.QuestionVO;
import com.example.citp.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 题库服务实现类
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionBankMapper questionBankMapper;
    private final QuestionTypeMapper questionTypeMapper;
    private final CourseMapper courseMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<QuestionType> getAllQuestionTypes() {
        return questionTypeMapper.selectList(null);
    }

    @Override
    public Page<QuestionVO> getQuestionList(Integer pageNum, Integer pageSize, Long questionTypeId, Long courseId) {
        Page<QuestionBank> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        if (questionTypeId != null) {
            wrapper.eq(QuestionBank::getQuestionTypeId, questionTypeId);
        }
        if (courseId != null) {
            wrapper.eq(QuestionBank::getCourseId, courseId);
        }
        wrapper.orderByDesc(QuestionBank::getCreateTime);

        Page<QuestionBank> questionPage = questionBankMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        Page<QuestionVO> voPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        voPage.setRecords(questionPage.getRecords().stream().map(question -> {
            QuestionVO vo = BeanUtil.copyProperties(question, QuestionVO.class);
            
            // 查询题型名称
            QuestionType type = questionTypeMapper.selectById(question.getQuestionTypeId());
            if (type != null) {
                vo.setQuestionTypeName(type.getTypeName());
            }
            
            // 查询课程名称
            if (question.getCourseId() != null) {
                Course course = courseMapper.selectById(question.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getCourseName());
                }
            }
            
            // 查询教师姓名
            SysUser teacher = sysUserMapper.selectById(question.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }
            
            return vo;
        }).toList());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createQuestion(QuestionRequest request) {
        SysUser currentUser = getCurrentUser();

        QuestionBank question = BeanUtil.copyProperties(request, QuestionBank.class);
        question.setTeacherId(currentUser.getId());
        question.setUsageCount(0);
        questionBankMapper.insert(question);
    }

    @Override
    public QuestionVO getQuestionById(Long id) {
        QuestionBank question = questionBankMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        QuestionVO vo = BeanUtil.copyProperties(question, QuestionVO.class);
        
        // 查询题型名称
        QuestionType type = questionTypeMapper.selectById(question.getQuestionTypeId());
        if (type != null) {
            vo.setQuestionTypeName(type.getTypeName());
        }
        
        // 查询课程名称
        if (question.getCourseId() != null) {
            Course course = courseMapper.selectById(question.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
        }
        
        // 查询教师姓名
        SysUser teacher = sysUserMapper.selectById(question.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }

        return vo;
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

        questionBankMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteQuestions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的题目");
        }

        questionBankMapper.deleteBatchIds(ids);
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
}
