package com.example.citp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 题目DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    /**
     * 题目ID
     */
    private Long id;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 分值
     */
    private Integer score;

    /**
     * 题型名称
     */
    private String typeName;

    /**
     * 题型 (SINGLE/MULTIPLE/TRUE_FALSE/FILL_BLANK/SHORT_ANSWER/PROGRAMMING/TEXT)
     */
    private String type;

    /**
     * 选项列表
     */
    private Object options;
}