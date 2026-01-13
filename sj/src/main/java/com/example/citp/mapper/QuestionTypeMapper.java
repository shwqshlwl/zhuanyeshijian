package com.example.citp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.citp.model.entity.QuestionType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题型 Mapper
 */
@Mapper
public interface QuestionTypeMapper extends BaseMapper<QuestionType> {
}
