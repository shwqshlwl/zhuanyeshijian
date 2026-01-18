package com.example.citp.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiSessionVO {
    private Long id;
    private String title;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
