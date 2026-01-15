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
import com.example.citp.service.CodeExecutionService;
import com.example.citp.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 实验服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentServiceImpl implements ExperimentService {

    private final ExperimentMapper experimentMapper;
    private final ExperimentSubmitMapper experimentSubmitMapper;
    private final CourseMapper courseMapper;
    private final SysUserMapper sysUserMapper;
    private final CodeExecutionService codeExecutionService;

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
        if (experiment.getCourseId() != null) {
            Course course = courseMapper.selectById(experiment.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
        }
        
        // 查询教师姓名
        if (experiment.getTeacherId() != null) {
            SysUser teacher = sysUserMapper.selectById(experiment.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getRealName());
            }
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
        submit.setStatus(1); // 评测中
        submit.setPassCount(0);
        submit.setTotalCount(0);
        experimentSubmitMapper.insert(submit);

        // 异步评测代码
        Long submitId = submit.getId();
        CompletableFuture.runAsync(() -> {
            try {
                evaluateSubmission(submitId, experiment, request.getCode(), request.getLanguage());
            } catch (Exception e) {
                log.error("代码评测失败", e);
                // 更新提交状态为运行错误
                ExperimentSubmit failedSubmit = experimentSubmitMapper.selectById(submitId);
                if (failedSubmit != null) {
                    failedSubmit.setStatus(5); // 运行错误
                    failedSubmit.setErrorMessage("评测失败: " + e.getMessage());
                    experimentSubmitMapper.updateById(failedSubmit);
                }
            }
        });
    }

    /**
     * 评测提交的代码
     */
    private void evaluateSubmission(Long submitId, Experiment experiment, String code, String language) {
        ExperimentSubmit submit = experimentSubmitMapper.selectById(submitId);
        if (submit == null) {
            return;
        }

        try {
            // 解析测试用例
            String testCasesJson = experiment.getTestCases();
            if (testCasesJson == null || testCasesJson.isEmpty()) {
                // 没有测试用例，只编译检查
                java.util.Map<String, Object> compileResult = codeExecutionService.compileCode(code, language);
                if ((Boolean) compileResult.get("success")) {
                    submit.setStatus(2); // 通过
                    submit.setScore(experiment.getTotalScore());
                    submit.setPassCount(1);
                    submit.setTotalCount(1);
                } else {
                    submit.setStatus(4); // 编译错误
                    submit.setErrorMessage((String) compileResult.get("error"));
                }
                experimentSubmitMapper.updateById(submit);
                return;
            }

            // 解析测试用例（假设是 JSON 数组格式）
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, String>> testCases = objectMapper.readValue(
                testCasesJson, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, String>>>() {}
            );

            int totalCount = testCases.size();
            int passCount = 0;
            int totalExecuteTime = 0;
            int maxMemoryUsed = 0;
            java.util.List<java.util.Map<String, Object>> resultDetails = new java.util.ArrayList<>();

            // 运行每个测试用例
            for (int i = 0; i < testCases.size(); i++) {
                java.util.Map<String, String> testCase = testCases.get(i);
                String input = testCase.get("input");
                String expectedOutput = testCase.get("output");

                // 执行代码
                java.util.Map<String, Object> execResult = codeExecutionService.executeCode(
                    code, 
                    language, 
                    input, 
                    experiment.getTimeLimit(), 
                    experiment.getMemoryLimit()
                );

                boolean success = (Boolean) execResult.get("success");
                String actualOutput = (String) execResult.get("output");
                String error = (String) execResult.get("error");

                // 比较输出
                boolean passed = success && compareOutput(expectedOutput, actualOutput);
                if (passed) {
                    passCount++;
                }

                // 记录详细结果
                java.util.Map<String, Object> detail = new java.util.HashMap<>();
                detail.put("testCase", i + 1);
                detail.put("passed", passed);
                detail.put("input", input);
                detail.put("expected", expectedOutput);
                detail.put("actual", actualOutput);
                detail.put("error", error);
                resultDetails.add(detail);

                // 如果编译错误或运行错误，提前结束
                if (!success && error != null && !error.isEmpty()) {
                    submit.setStatus(error.contains("编译") ? 4 : 5);
                    submit.setErrorMessage(error);
                    submit.setPassCount(passCount);
                    submit.setTotalCount(totalCount);
                    submit.setResultDetail(objectMapper.writeValueAsString(resultDetails));
                    experimentSubmitMapper.updateById(submit);
                    return;
                }
            }

            // 计算得分
            int score = (int) Math.round((double) passCount / totalCount * experiment.getTotalScore());

            // 更新提交记录
            submit.setStatus(passCount == totalCount ? 2 : 3); // 2-通过，3-未通过
            submit.setScore(score);
            submit.setPassCount(passCount);
            submit.setTotalCount(totalCount);
            submit.setExecuteTime(totalExecuteTime);
            submit.setMemoryUsed(maxMemoryUsed);
            submit.setResultDetail(objectMapper.writeValueAsString(resultDetails));
            experimentSubmitMapper.updateById(submit);

        } catch (Exception e) {
            log.error("评测代码失败", e);
            submit.setStatus(5); // 运行错误
            submit.setErrorMessage("评测失败: " + e.getMessage());
            experimentSubmitMapper.updateById(submit);
        }
    }

    /**
     * 比较输出结果（忽略行尾空格和多余空行）
     */
    private boolean compareOutput(String expected, String actual) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }

        // 标准化输出：去除行尾空格，去除末尾空行
        String normalizedExpected = normalizeOutput(expected);
        String normalizedActual = normalizeOutput(actual);

        return normalizedExpected.equals(normalizedActual);
    }

    /**
     * 标准化输出
     */
    private String normalizeOutput(String output) {
        if (output == null) {
            return "";
        }
        
        String[] lines = output.split("\n");
        java.util.List<String> normalizedLines = new java.util.ArrayList<>();
        
        for (String line : lines) {
            normalizedLines.add(line.stripTrailing());
        }
        
        // 移除末尾的空行
        while (!normalizedLines.isEmpty() && normalizedLines.get(normalizedLines.size() - 1).isEmpty()) {
            normalizedLines.remove(normalizedLines.size() - 1);
        }
        
        return String.join("\n", normalizedLines);
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

    @Override
    public java.util.Map<String, Object> runCode(Long experimentId, String code, String language) {
        // 检查实验是否存在
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }

        // 使用真实的代码执行服务
        try {
            Integer timeLimit = experiment.getTimeLimit() != null ? experiment.getTimeLimit() : 5000;
            Integer memoryLimit = experiment.getMemoryLimit() != null ? experiment.getMemoryLimit() : 256;
            
            return codeExecutionService.executeCode(code, language, null, timeLimit, memoryLimit);
        } catch (Exception e) {
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "代码执行失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public java.util.List<ExperimentResultVO> getMySubmissions(Long experimentId) {
        // 获取当前学生
        SysUser currentUser = getCurrentUser();

        // 查询所有提交记录
        java.util.List<ExperimentSubmit> submits = experimentSubmitMapper.selectList(
                new LambdaQueryWrapper<ExperimentSubmit>()
                        .eq(ExperimentSubmit::getExperimentId, experimentId)
                        .eq(ExperimentSubmit::getStudentId, currentUser.getId())
                        .orderByDesc(ExperimentSubmit::getSubmitTime));

        // 如果没有提交记录，返回空列表而不是抛出异常
        if (submits == null || submits.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        return submits.stream().map(submit -> {
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
            vo.setCode(submit.getCode());
            return vo;
        }).toList();
    }

    @Override
    public Page<ExperimentResultVO> getSubmissions(Long experimentId, Integer pageNum, Integer pageSize) {
        Page<ExperimentSubmit> page = new Page<>(pageNum, pageSize);
        
        // 查询所有学生的最新提交
        Page<ExperimentSubmit> submitPage = experimentSubmitMapper.selectPage(page,
                new LambdaQueryWrapper<ExperimentSubmit>()
                        .eq(ExperimentSubmit::getExperimentId, experimentId)
                        .orderByDesc(ExperimentSubmit::getSubmitTime));

        Page<ExperimentResultVO> voPage = new Page<>(submitPage.getCurrent(), submitPage.getSize(), submitPage.getTotal());
        
        if (submitPage.getRecords() == null || submitPage.getRecords().isEmpty()) {
            voPage.setRecords(new java.util.ArrayList<>());
            return voPage;
        }
        
        voPage.setRecords(submitPage.getRecords().stream().map(submit -> {
            ExperimentResultVO vo = new ExperimentResultVO();
            vo.setSubmitId(submit.getId());
            vo.setStudentId(submit.getStudentId());
            vo.setStatus(submit.getStatus());
            vo.setScore(submit.getScore());
            vo.setPassCount(submit.getPassCount());
            vo.setTotalCount(submit.getTotalCount());
            vo.setExecuteTime(submit.getExecuteTime());
            vo.setMemoryUsed(submit.getMemoryUsed());
            vo.setSubmitTime(submit.getSubmitTime());
            
            // 查询学生信息
            if (submit.getStudentId() != null) {
                SysUser student = sysUserMapper.selectById(submit.getStudentId());
                if (student != null) {
                    vo.setStudentName(student.getRealName());
                    vo.setStudentNo(student.getUsername());
                }
            }
            
            return vo;
        }).toList());

        return voPage;
    }

    /**
     * 获取学生的实验提交详情（教师查看）
     */
    public ExperimentResultVO getStudentSubmission(Long experimentId, Long studentId) {
        // 查询学生最新提交
        ExperimentSubmit submit = experimentSubmitMapper.selectOne(
                new LambdaQueryWrapper<ExperimentSubmit>()
                        .eq(ExperimentSubmit::getExperimentId, experimentId)
                        .eq(ExperimentSubmit::getStudentId, studentId)
                        .orderByDesc(ExperimentSubmit::getSubmitTime)
                        .last("LIMIT 1"));

        if (submit == null) {
            throw new BusinessException("未找到提交记录");
        }

        ExperimentResultVO vo = new ExperimentResultVO();
        vo.setSubmitId(submit.getId());
        vo.setStudentId(submit.getStudentId());
        vo.setStatus(submit.getStatus());
        vo.setScore(submit.getScore());
        vo.setPassCount(submit.getPassCount());
        vo.setTotalCount(submit.getTotalCount());
        vo.setExecuteTime(submit.getExecuteTime());
        vo.setMemoryUsed(submit.getMemoryUsed());
        vo.setErrorMessage(submit.getErrorMessage());
        vo.setResultDetail(submit.getResultDetail());
        vo.setSubmitTime(submit.getSubmitTime());
        vo.setCode(submit.getCode());

        // 查询学生信息
        if (submit.getStudentId() != null) {
            SysUser student = sysUserMapper.selectById(submit.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getRealName());
                vo.setStudentNo(student.getUsername());
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
