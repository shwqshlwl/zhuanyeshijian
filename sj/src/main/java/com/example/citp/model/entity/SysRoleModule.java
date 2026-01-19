package com.example.citp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色模块关联实体类
 */
@Data
@TableName("sys_role_module")
public class SysRoleModule {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色类型：1学生 2教师 3管理员
     */
    private Integer roleType;

    /**
     * 模块ID
     */
    private Long moduleId;

    /**
     * 是否启用：0禁用 1启用
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
