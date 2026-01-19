package com.example.citp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.citp.exception.BusinessException;
import com.example.citp.mapper.CourseOfferingMapper;
import com.example.citp.mapper.SemesterMapper;
import com.example.citp.model.dto.SemesterRequest;
import com.example.citp.model.entity.CourseOffering;
import com.example.citp.model.entity.Semester;
import com.example.citp.model.vo.SemesterVO;
import com.example.citp.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 学期服务实现类
 */
@Service
@RequiredArgsConstructor
public class SemesterServiceImpl implements SemesterService {

    private final SemesterMapper semesterMapper;
    private final CourseOfferingMapper courseOfferingMapper;

    @Override
    public Page<SemesterVO> getSemesterList(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        Page<Semester> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Semester> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Semester::getSemesterName, keyword)
                    .or()
                    .like(Semester::getSemesterCode, keyword);
        }
        if (status != null) {
            wrapper.eq(Semester::getStatus, status);
        }
        wrapper.orderByDesc(Semester::getStartDate);

        Page<Semester> semesterPage = semesterMapper.selectPage(page, wrapper);

        // 转换为 VO
        Page<SemesterVO> voPage = new Page<>(semesterPage.getCurrent(), semesterPage.getSize(), semesterPage.getTotal());
        voPage.setRecords(semesterPage.getRecords().stream().map(semester -> {
            SemesterVO vo = BeanUtil.copyProperties(semester, SemesterVO.class);
            // 查询该学期的开课数量
            Long count = courseOfferingMapper.selectCount(new LambdaQueryWrapper<CourseOffering>()
                    .eq(CourseOffering::getSemesterId, semester.getId()));
            vo.setOfferingCount(count.intValue());
            return vo;
        }).toList());

        return voPage;
    }

    @Override
    public List<SemesterVO> getAllSemesters() {
        List<Semester> semesters = semesterMapper.selectList(new LambdaQueryWrapper<Semester>()
                .orderByDesc(Semester::getStartDate));
        return semesters.stream()
                .map(semester -> BeanUtil.copyProperties(semester, SemesterVO.class))
                .toList();
    }

    @Override
    public SemesterVO getCurrentSemester() {
        Semester semester = semesterMapper.selectOne(new LambdaQueryWrapper<Semester>()
                .eq(Semester::getIsCurrent, 1));
        if (semester == null) {
            throw new BusinessException("当前没有激活的学期");
        }
        return BeanUtil.copyProperties(semester, SemesterVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSemester(SemesterRequest request) {
        // 检查学期代码是否已存在
        Long count = semesterMapper.selectCount(new LambdaQueryWrapper<Semester>()
                .eq(Semester::getSemesterCode, request.getSemesterCode()));
        if (count > 0) {
            throw new BusinessException("学期代码已存在");
        }

        // 验证日期范围
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }

        Semester semester = BeanUtil.copyProperties(request, Semester.class);
        // 根据当前日期自动设置状态
        LocalDate now = LocalDate.now();
        if (now.isBefore(semester.getStartDate())) {
            semester.setStatus(0); // 未开始
        } else if (now.isAfter(semester.getEndDate())) {
            semester.setStatus(2); // 已结束
        } else {
            semester.setStatus(1); // 进行中
        }
        semester.setIsCurrent(0); // 默认不是当前学期

        semesterMapper.insert(semester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSemester(Long id, SemesterRequest request) {
        Semester semester = semesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException("学期不存在");
        }

        // 检查学期代码是否被其他学期使用
        Long count = semesterMapper.selectCount(new LambdaQueryWrapper<Semester>()
                .eq(Semester::getSemesterCode, request.getSemesterCode())
                .ne(Semester::getId, id));
        if (count > 0) {
            throw new BusinessException("学期代码已存在");
        }

        // 验证日期范围
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }

        BeanUtil.copyProperties(request, semester, "id", "status", "isCurrent");
        semesterMapper.updateById(semester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSemester(Long id) {
        Semester semester = semesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException("学期不存在");
        }

        // 检查是否是当前学期
        if (semester.getIsCurrent() == 1) {
            throw new BusinessException("不能删除当前学期");
        }

        // 检查是否有开课实例
        Long count = courseOfferingMapper.selectCount(new LambdaQueryWrapper<CourseOffering>()
                .eq(CourseOffering::getSemesterId, id));
        if (count > 0) {
            throw new BusinessException("该学期已有开课记录，无法删除");
        }

        semesterMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setCurrentSemester(Long id) {
        Semester semester = semesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException("学期不存在");
        }

        // 将所有学期的 isCurrent 设为 0
        semesterMapper.update(null, new LambdaUpdateWrapper<Semester>()
                .set(Semester::getIsCurrent, 0));

        // 将指定学期的 isCurrent 设为 1
        semester.setIsCurrent(1);
        semesterMapper.updateById(semester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endSemester(Long id) {
        Semester semester = semesterMapper.selectById(id);
        if (semester == null) {
            throw new BusinessException("学期不存在");
        }

        semester.setStatus(2); // 设为已结束
        semesterMapper.updateById(semester);

        // 将该学期的所有开课实例状态改为已结课
        courseOfferingMapper.update(null, new LambdaUpdateWrapper<CourseOffering>()
                .eq(CourseOffering::getSemesterId, id)
                .set(CourseOffering::getStatus, 2));
    }
}
