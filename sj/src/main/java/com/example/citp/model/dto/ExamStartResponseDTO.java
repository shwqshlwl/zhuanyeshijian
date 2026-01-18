package com.example.citp.model.dto;

import com.example.citp.model.vo.ExamVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 考试开始响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamStartResponseDTO {

    /**
     * 考试信息
     */
    private ExamVO exam;

    /**
     * 题目列表
     */
    private List<QuestionDTO> questions;
}