package com.example.citp.service;

import java.util.Map;

/**
 * 代码执行服务接口
 */
public interface CodeExecutionService {

    /**
     * 执行代码
     * 
     * @param code 代码内容
     * @param language 编程语言
     * @param input 输入数据
     * @param timeLimit 时间限制（毫秒）
     * @param memoryLimit 内存限制（MB）
     * @return 执行结果
     */
    Map<String, Object> executeCode(String code, String language, String input, Integer timeLimit, Integer memoryLimit);

    /**
     * 编译代码
     * 
     * @param code 代码内容
     * @param language 编程语言
     * @return 编译结果
     */
    Map<String, Object> compileCode(String code, String language);
}
