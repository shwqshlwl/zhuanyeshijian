package com.example.citp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.citp.model.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试记录 Mapper
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {
}
