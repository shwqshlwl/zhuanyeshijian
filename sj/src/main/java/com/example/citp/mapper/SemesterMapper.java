package com.example.citp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.citp.model.entity.Semester;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学期Mapper接口
 */
@Mapper
public interface SemesterMapper extends BaseMapper<Semester> {
}
