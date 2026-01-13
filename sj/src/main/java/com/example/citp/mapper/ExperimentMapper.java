package com.example.citp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.citp.model.entity.Experiment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实验 Mapper
 */
@Mapper
public interface ExperimentMapper extends BaseMapper<Experiment> {
}
