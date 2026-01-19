-- 课程-班级多对多关联表
CREATE TABLE IF NOT EXISTS course_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_course_class (course_id, class_id),
    INDEX idx_course_id (course_id),
    INDEX idx_class_id (class_id),
    CONSTRAINT fk_course_class_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_class_class FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程班级关联表';

-- 注意：此表用于实现课程和班级的多对多关系
-- 一个课程可以被多个班级使用
-- 一个班级可以关联多门课程
