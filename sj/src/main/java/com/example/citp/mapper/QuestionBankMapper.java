package com.example.citp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.citp.model.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库 Mapper
 */
@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {
}
