package com.example.citp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.citp.model.entity.Homework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 作业 Mapper
 */
@Mapper
public interface HomeworkMapper extends BaseMapper<Homework> {
    @Update("""
        UPDATE homework
        SET status = CASE
            WHEN start_time > NOW() THEN 0
            WHEN start_time <= NOW() AND end_time >= NOW() THEN 1
            WHEN end_time < NOW() THEN 2
        END
        WHERE deleted = 0
    """)
    void updateHomeworkStatusByTime();
}
