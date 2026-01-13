package com.example.citp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.citp.model.entity.Exam;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试 Mapper
 */
@Mapper
public interface ExamMapper extends BaseMapper<Exam> {
}
