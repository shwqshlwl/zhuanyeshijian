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
import java.util.concurrent.TimeUnit;

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
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final com.example.citp.mapper.ClassMapper classMapper;

    @Override
    public Page<ExperimentVO> getExperimentList(Integer pageNum, Integer pageSize, Long courseId) {
        Page<Experiment> page = new Page<>(pageNum, pageSize);
        
        SysUser currentUser = getCurrentUser();
        
        LambdaQueryWrapper<Experiment> wrapper = new LambdaQueryWrapper<>();
        
        // 学生只能查看自己所选课程的实验
        if (currentUser.getUserType() == 1) { // 学生
            java.util.List<Long> studentCourseIds = getStudentCourseIds(currentUser.getId());
            if (studentCourseIds.isEmpty()) {
                // 学生没有选课，返回空列表
                Page<ExperimentVO> emptyPage = new Page<>(pageNum, pageSize, 0);
                emptyPage.setRecords(new java.util.ArrayList<>());
                return emptyPage;
            }
            wrapper.in(Experiment::getCourseId, studentCourseIds);
        }
        
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

        // 学生权限检查：只能查看自己所选课程的实验
        SysUser currentUser = getCurrentUser();
        if (currentUser.getUserType() == 1) { // 学生
            checkStudentCourseAccess(currentUser.getId(), experiment.getCourseId());
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

        // 学生权限检查：只能提交自己所选课程的实验
        checkStudentCourseAccess(currentUser.getId(), experiment.getCourseId());

        // 在事务中创建提交记录
        Long submitId = createSubmitRecord(experimentId, currentUser.getId(), request);
        
        // 异步评测代码（在事务外执行）
        log.info("开始异步评测，提交ID: {}, 实验ID: {}, 学生ID: {}, 代码长度: {}", 
            submitId, experimentId, currentUser.getId(), request.getCode().length());
        
        CompletableFuture.runAsync(() -> {
            try {
                log.info("评测任务开始执行，提交ID: {}", submitId);
                evaluateSubmission(submitId, experiment, request.getCode(), request.getLanguage());
                log.info("评测任务执行完成，提交ID: {}", submitId);
            } catch (Exception e) {
                log.error("代码评测失败，提交ID: " + submitId, e);
                // 更新提交状态为运行错误
                try {
                    ExperimentSubmit failedSubmit = experimentSubmitMapper.selectById(submitId);
                    if (failedSubmit != null) {
                        failedSubmit.setStatus(5); // 运行错误
                        failedSubmit.setErrorMessage("评测失败: " + e.getMessage());
                        experimentSubmitMapper.updateById(failedSubmit);
                        log.info("已更新提交状态为运行错误，提交ID: {}", submitId);
                    }
                } catch (Exception updateEx) {
                    log.error("更新提交状态失败，提交ID: " + submitId, updateEx);
                }
            }
        }).orTimeout(30, TimeUnit.SECONDS) // 整个评测过程最多30秒
          .exceptionally(ex -> {
              log.error("异步任务执行异常或超时，提交ID: " + submitId, ex);
              // 超时后更新状态
              try {
                  ExperimentSubmit timeoutSubmit = experimentSubmitMapper.selectById(submitId);
                  if (timeoutSubmit != null && timeoutSubmit.getStatus() == 1) {
                      timeoutSubmit.setStatus(5);
                      timeoutSubmit.setErrorMessage("评测超时：评测过程超过30秒");
                      experimentSubmitMapper.updateById(timeoutSubmit);
                      log.info("评测超时，已更新状态，提交ID: {}", submitId);
                  }
              } catch (Exception e) {
                  log.error("更新超时状态失败，提交ID: " + submitId, e);
              }
              return null;
          });
    }
    
    /**
     * 创建提交记录（在独立事务中）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createSubmitRecord(Long experimentId, Long studentId, ExperimentSubmitRequest request) {
        ExperimentSubmit submit = new ExperimentSubmit();
        submit.setExperimentId(experimentId);
        submit.setStudentId(studentId);
        submit.setCode(request.getCode());
        submit.setLanguage(request.getLanguage());
        submit.setSubmitTime(LocalDateTime.now());
        submit.setStatus(1); // 评测中
        submit.setPassCount(0);
        submit.setTotalCount(0);
        experimentSubmitMapper.insert(submit);
        return submit.getId();
    }

    /**
     * 评测提交的代码
     */
    private void evaluateSubmission(Long submitId, Experiment experiment, String code, String language) {
        log.info("开始评测代码，提交ID: {}, 语言: {}", submitId, language);
        
        ExperimentSubmit submit = experimentSubmitMapper.selectById(submitId);
        if (submit == null) {
            log.error("未找到提交记录，提交ID: {}", submitId);
            return;
        }

        try {
            // 解析测试用例
            String testCasesJson = experiment.getTestCases();
            log.info("测试用例JSON: {}", testCasesJson);
            
            if (testCasesJson == null || testCasesJson.isEmpty()) {
                log.info("没有测试用例，只进行编译检查，提交ID: {}", submitId);
                // 没有测试用例，只编译检查
                java.util.Map<String, Object> compileResult = codeExecutionService.compileCode(code, language);
                if ((Boolean) compileResult.get("success")) {
                    submit.setStatus(2); // 通过
                    submit.setScore(experiment.getTotalScore());
                    submit.setPassCount(1);
                    submit.setTotalCount(1);
                    log.info("编译成功，提交ID: {}", submitId);
                } else {
                    submit.setStatus(4); // 编译错误
                    submit.setErrorMessage((String) compileResult.get("error"));
                    log.info("编译失败，提交ID: {}, 错误: {}", submitId, compileResult.get("error"));
                }
                experimentSubmitMapper.updateById(submit);
                return;
            }

            // 解析测试用例（假设是 JSON 数组格式）
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, String>> testCases;
            try {
                testCases = objectMapper.readValue(
                    testCasesJson, 
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, String>>>() {}
                );
                log.info("成功解析测试用例，数量: {}, 提交ID: {}", testCases.size(), submitId);
            } catch (Exception e) {
                log.error("解析测试用例失败，提交ID: " + submitId, e);
                submit.setStatus(5);
                submit.setErrorMessage("测试用例格式错误: " + e.getMessage());
                experimentSubmitMapper.updateById(submit);
                return;
            }

            int totalCount = testCases.size();
            int passCount = 0;
            int totalExecuteTime = 0;
            int maxMemoryUsed = 0;
            java.util.List<java.util.Map<String, Object>> resultDetails = new java.util.ArrayList<>();

            // 运行每个测试用例
            for (int i = 0; i < testCases.size(); i++) {
                log.info("执行测试用例 {}/{}, 提交ID: {}", i + 1, testCases.size(), submitId);
                
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
                
                log.info("测试用例 {} 执行结果: success={}, output={}, error={}", 
                    i + 1, success, actualOutput, error);

                // 比较输出
                boolean passed = success && compareOutput(expectedOutput, actualOutput);
                if (passed) {
                    passCount++;
                }
                
                log.info("测试用例 {} 比较结果: passed={}, 期望输出={}, 实际输出={}", 
                    i + 1, passed, expectedOutput, actualOutput);

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
            
            log.info("评测完成，提交ID: {}, 通过: {}/{}, 得分: {}", submitId, passCount, totalCount, score);

            // 更新提交记录
            submit.setStatus(passCount == totalCount ? 2 : 3); // 2-通过，3-未通过
            submit.setScore(score);
            submit.setPassCount(passCount);
            submit.setTotalCount(totalCount);
            submit.setExecuteTime(totalExecuteTime);
            submit.setMemoryUsed(maxMemoryUsed);
            submit.setResultDetail(objectMapper.writeValueAsString(resultDetails));
            experimentSubmitMapper.updateById(submit);
            
            log.info("已更新提交记录，提交ID: {}, 状态: {}", submitId, submit.getStatus());

        } catch (Exception e) {
            log.error("评测代码失败，提交ID: " + submitId, e);
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
        // 检查实验是否存在
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }

        // 获取当前学生
        SysUser currentUser = getCurrentUser();

        // 学生权限检查：只能查看自己所选课程的实验结果
        if (currentUser.getUserType() == 1) { // 学生
            checkStudentCourseAccess(currentUser.getId(), experiment.getCourseId());
        }

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
        vo.setCode(submit.getCode()); // 添加代码字段
        vo.setLanguage(submit.getLanguage()); // 添加语言字段

        return vo;
    }

    @Override
    public java.util.Map<String, Object> runCode(Long experimentId, String code, String language, String input) {
        // 检查实验是否存在
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }

        // 学生权限检查：只能运行自己所选课程的实验代码
        SysUser currentUser = getCurrentUser();
        if (currentUser.getUserType() == 1) { // 学生
            checkStudentCourseAccess(currentUser.getId(), experiment.getCourseId());
        }

        // 使用真实的代码执行服务
        try {
            Integer timeLimit = experiment.getTimeLimit() != null ? experiment.getTimeLimit() : 5000;
            Integer memoryLimit = experiment.getMemoryLimit() != null ? experiment.getMemoryLimit() : 256;
            
            return codeExecutionService.executeCode(code, language, input, timeLimit, memoryLimit);
        } catch (Exception e) {
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "代码执行失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public java.util.List<ExperimentResultVO> getMySubmissions(Long experimentId) {
        // 检查实验是否存在
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }

        // 获取当前学生
        SysUser currentUser = getCurrentUser();

        // 学生权限检查：只能查看自己所选课程的实验提交历史
        if (currentUser.getUserType() == 1) { // 学生
            checkStudentCourseAccess(currentUser.getId(), experiment.getCourseId());
        }

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
     * 获取学生所选课程的ID列表
     */
    private java.util.List<Long> getStudentCourseIds(Long studentId) {
        // 查询学生所在的班级
        java.util.List<Long> classIds = jdbcTemplate.queryForList(
                "SELECT class_id FROM student_class WHERE student_id = ?",
                Long.class, studentId);

        if (classIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        // 查询班级关联的课程
        java.util.List<com.example.citp.model.entity.ClassEntity> classes = classMapper.selectBatchIds(classIds);
        return classes.stream()
                .filter(c -> c.getCourseId() != null)
                .map(com.example.citp.model.entity.ClassEntity::getCourseId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 检查学生是否有权限访问该课程
     */
    private void checkStudentCourseAccess(Long studentId, Long courseId) {
        java.util.List<Long> studentCourseIds = getStudentCourseIds(studentId);
        if (!studentCourseIds.contains(courseId)) {
            throw new BusinessException("您没有权限访问该课程的实验");
        }
    }

    @Override
    public com.example.citp.model.vo.ExperimentSubmitStatVO getSubmitStatistics(Long experimentId) {
        // 检查实验是否存在
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new BusinessException("实验不存在");
        }

        com.example.citp.model.vo.ExperimentSubmitStatVO statVO = new com.example.citp.model.vo.ExperimentSubmitStatVO();

        // 获取该课程下所有学生
        Long courseId = experiment.getCourseId();
        
        // 查询课程关联的班级
        java.util.List<com.example.citp.model.entity.ClassEntity> classes = classMapper.selectList(
                new LambdaQueryWrapper<com.example.citp.model.entity.ClassEntity>()
                        .eq(com.example.citp.model.entity.ClassEntity::getCourseId, courseId));

        if (classes.isEmpty()) {
            statVO.setTotalStudents(0);
            statVO.setSubmittedCount(0);
            statVO.setUnsubmittedCount(0);
            statVO.setUnsubmittedStudents(new java.util.ArrayList<>());
            return statVO;
        }

        // 获取所有班级ID
        java.util.List<Long> classIds = classes.stream()
                .map(com.example.citp.model.entity.ClassEntity::getId)
                .collect(java.util.stream.Collectors.toList());

        // 查询这些班级的所有学生
        java.util.List<Long> studentIds = jdbcTemplate.queryForList(
                "SELECT student_id FROM student_class WHERE class_id IN (" +
                        classIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) + ")",
                Long.class);

        if (studentIds.isEmpty()) {
            statVO.setTotalStudents(0);
            statVO.setSubmittedCount(0);
            statVO.setUnsubmittedCount(0);
            statVO.setUnsubmittedStudents(new java.util.ArrayList<>());
            return statVO;
        }

        // 去重学生ID
        studentIds = studentIds.stream().distinct().collect(java.util.stream.Collectors.toList());

        // 查询已提交的学生ID
        java.util.List<Long> submittedStudentIds = experimentSubmitMapper.selectList(
                new LambdaQueryWrapper<ExperimentSubmit>()
                        .eq(ExperimentSubmit::getExperimentId, experimentId)
                        .in(ExperimentSubmit::getStudentId, studentIds))
                .stream()
                .map(ExperimentSubmit::getStudentId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        // 计算未提交的学生ID
        java.util.List<Long> unsubmittedStudentIds = studentIds.stream()
                .filter(id -> !submittedStudentIds.contains(id))
                .collect(java.util.stream.Collectors.toList());

        // 查询未提交学生的详细信息
        java.util.List<com.example.citp.model.vo.ExperimentSubmitStatVO.StudentInfo> unsubmittedStudents = new java.util.ArrayList<>();
        if (!unsubmittedStudentIds.isEmpty()) {
            java.util.List<SysUser> students = sysUserMapper.selectBatchIds(unsubmittedStudentIds);
            
            // 创建学生ID到班级的映射
            java.util.Map<Long, String> studentClassMap = new java.util.HashMap<>();
            for (Long classId : classIds) {
                java.util.List<Long> classStudentIds = jdbcTemplate.queryForList(
                        "SELECT student_id FROM student_class WHERE class_id = ?",
                        Long.class, classId);
                
                com.example.citp.model.entity.ClassEntity classEntity = classMapper.selectById(classId);
                String className = classEntity != null ? classEntity.getClassName() : "";
                
                for (Long studentId : classStudentIds) {
                    studentClassMap.put(studentId, className);
                }
            }

            for (SysUser student : students) {
                com.example.citp.model.vo.ExperimentSubmitStatVO.StudentInfo info = 
                        new com.example.citp.model.vo.ExperimentSubmitStatVO.StudentInfo();
                info.setStudentId(student.getId());
                info.setStudentNo(student.getUsername());
                info.setStudentName(student.getRealName());
                info.setClassName(studentClassMap.getOrDefault(student.getId(), ""));
                unsubmittedStudents.add(info);
            }
        }

        statVO.setTotalStudents(studentIds.size());
        statVO.setSubmittedCount(submittedStudentIds.size());
        statVO.setUnsubmittedCount(unsubmittedStudentIds.size());
        statVO.setUnsubmittedStudents(unsubmittedStudents);

        return statVO;
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
