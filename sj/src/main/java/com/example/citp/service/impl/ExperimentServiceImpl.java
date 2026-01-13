package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.CourseMapper;
import com.example.citp.mapper.ExperimentMapper;
import com.example.citp.mapper.ExperimentSubmitMapper;
import com.example.citp.mapper.SysUserMapper;
import com.example.citp.model.dto.ExperimentRequest;
import com.example.citp.model.dto.ExperimentSubmitRequest;
import com.example.citp.model.entity.Course;
import com.example.citp.model.entity.Experiment;
import com.example.citp.model.entity.ExperimentSubmit;
import com.example.citp.model.entity.SysUser;
import com.example.citp.model.vo.ExperimentResultVO;
import com.example.citp.model.vo.ExperimentVO;
import com.example.citp.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 实验服务实现类
 */
@Service
@RequiredArgsConstructor
public class ExperimentServiceImpl implements ExperimentService {

    private final ExperimentMapper experimentMapper;
    private final ExperimentSubmitMapper experimentSubmitMapper;
    private final CourseMapper courseMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public Page<ExperimentVO> getExperimentList(Integer pageNum, Integer pageSize, Long courseId) {
        Page<Experiment> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Experiment> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Experiment::getCourseId, courseId);
        }
        wrapper.orderByDesc(Experiment::getCreateTime);

        Page<Experiment> experimentPage = experimentMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        Page<ExperimentVO> voPage = new Page<>(experimentPage.getCurrent(), experimentPage.getSize(), experimentPage.getTotal());
        voPage.setRecords(experimentPage.getRecords().stream().map(experiment -> {
            ExperimentVO vo = BeanUtil.copyProperties(experiment, ExperimentVO.class);
            
            // 查询课程名称
            Course course = courseMapper.selectById(experiment.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
            
            // 查询教师姓名
            SysUser teacher = sysUserMapper.selectById(experiment.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }
            
            return vo;
        }).toList());

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createExperiment(ExperimentRequest request) {
        SysUser currentUser = getCurrentUser();

        Experiment experiment = BeanUtil.copyProperties(request, Experiment.class);
        experiment.setTeacherId(currentUser.getId());
        experimentMapper.insert(experiment);
    }

    @Override
    public ExperimentVO getExperimentById(Long id) {
        Experiment experiment = experimentMapper.selectById(id);
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }

        ExperimentVO vo = BeanUtil.copyProperties(experiment, ExperimentVO.class);
        
        // 查询课程名称
        Course course = courseMapper.selectById(experiment.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getCourseName());
        }
        
        // 查询教师姓名
        SysUser teacher = sysUserMapper.selectById(experiment.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExperiment(Long id, ExperimentRequest request) {
        Experiment experiment = experimentMapper.selectById(id);
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }

        BeanUtil.copyProperties(request, experiment, "id", "teacherId");
        experimentMapper.updateById(experiment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExperiment(Long id) {
        Experiment experiment = experimentMapper.selectById(id);
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }

        experimentMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExperiment(Long experimentId, ExperimentSubmitRequest request) {
        // 检查实验是否存在
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }

        // 获取当前学生
        SysUser currentUser = getCurrentUser();
        if (currentUser.getUserType() != 1) {
            throw new BusinessException("只有学生可以提交实验");
        }

        // 创建提交记录
        ExperimentSubmit submit = new ExperimentSubmit();
        submit.setExperimentId(experimentId);
        submit.setStudentId(currentUser.getId());
        submit.setCode(request.getCode());
        submit.setLanguage(request.getLanguage());
        submit.setSubmitTime(LocalDateTime.now());
        submit.setStatus(0); // 待评测
        submit.setPassCount(0);
        submit.setTotalCount(0);
        experimentSubmitMapper.insert(submit);

        // TODO: 异步调用代码评测服务
        // 这里可以集成 Judge0、Docker 容器等代码执行环境
    }

    @Override
    public ExperimentResultVO getExperimentResult(Long experimentId) {
        // 获取当前学生
        SysUser currentUser = getCurrentUser();

        // 查询最新提交记录
        ExperimentSubmit submit = experimentSubmitMapper.selectOne(new LambdaQueryWrapper<ExperimentSubmit>()
                .eq(ExperimentSubmit::getExperimentId, experimentId)
                .eq(ExperimentSubmit::getStudentId, currentUser.getId())
                .orderByDesc(ExperimentSubmit::getSubmitTime)
                .last("LIMIT 1"));

        if (submit == null) {
            throw new BusinessException("未找到提交记录");
        }

        ExperimentResultVO vo = new ExperimentResultVO();
        vo.setSubmitId(submit.getId());
        vo.setStatus(submit.getStatus());
        vo.setScore(submit.getScore());
        vo.setPassCount(submit.getPassCount());
        vo.setTotalCount(submit.getTotalCount());
        vo.setExecuteTime(submit.getExecuteTime());
        vo.setMemoryUsed(submit.getMemoryUsed());
        vo.setErrorMessage(submit.getErrorMessage());
        vo.setResultDetail(submit.getResultDetail());
        vo.setSubmitTime(submit.getSubmitTime());

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
