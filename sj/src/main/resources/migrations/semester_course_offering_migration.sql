-- ============================================
-- 学期管理和课程复用功能 - 数据库迁移脚本
-- ============================================

-- 1. 创建学期表
CREATE TABLE IF NOT EXISTS semester (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    semester_name VARCHAR(50) NOT NULL COMMENT '学期名称，如：2024-2025-1',
    semester_code VARCHAR(20) NOT NULL UNIQUE COMMENT '学期代码，如：202401',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年，如：2024-2025',
    term_number TINYINT NOT NULL COMMENT '学期序号：1-春季，2-夏季，3-秋季',
    start_date DATE NOT NULL COMMENT '学期开始日期',
    end_date DATE NOT NULL COMMENT '学期结束日期',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
    is_current TINYINT NOT NULL DEFAULT 0 COMMENT '是否当前学期：0-否，1-是（全局只能有一个）',
    description TEXT COMMENT '学期描述',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_semester_code (semester_code),
    INDEX idx_is_current (is_current),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学期表';

-- 2. 创建开课实例表
CREATE TABLE IF NOT EXISTS course_offering (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL COMMENT '课程模板ID（关联course表）',
    semester_id BIGINT NOT NULL COMMENT '学期ID（关联semester表）',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未开课，1-进行中，2-已结课',
    max_students INT DEFAULT 0 COMMENT '最大选课人数（选修课）',
    current_students INT DEFAULT 0 COMMENT '当前选课人数',
    class_location VARCHAR(100) COMMENT '上课地点',
    class_schedule VARCHAR(200) COMMENT '上课时间安排',
    notes TEXT COMMENT '备注信息',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_semester (course_id, semester_id, deleted),
    INDEX idx_course_id (course_id),
    INDEX idx_semester_id (semester_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开课实例表';

-- 3. 修改现有表，添加开课实例ID字段

-- 3.1 修改 course_class 表（课程-班级关联表）
ALTER TABLE course_class ADD COLUMN offering_id BIGINT NULL COMMENT '开课实例ID' AFTER course_id;
ALTER TABLE course_class ADD INDEX idx_offering_id (offering_id);

-- 3.2 修改 homework 表
ALTER TABLE homework ADD COLUMN offering_id BIGINT NULL COMMENT '开课实例ID' AFTER course_id;
ALTER TABLE homework ADD INDEX idx_offering_id (offering_id);

-- 3.3 修改 exam 表
ALTER TABLE exam ADD COLUMN offering_id BIGINT NULL COMMENT '开课实例ID' AFTER course_id;
ALTER TABLE exam ADD INDEX idx_offering_id (offering_id);

-- 3.4 修改 student_course 表（学生选课表）
ALTER TABLE student_course ADD COLUMN offering_id BIGINT NULL COMMENT '开课实例ID' AFTER course_id;
ALTER TABLE student_course ADD INDEX idx_offering_id (offering_id);

-- 4. 数据迁移：创建默认学期和开课实例

-- 4.1 创建默认学期（2024-2025学年第1学期）
INSERT INTO semester (semester_name, semester_code, academic_year, term_number, start_date, end_date, status, is_current, description)
VALUES ('2024-2025学年第1学期', '202401', '2024-2025', 1, '2024-09-01', '2025-01-15', 1, 1, '默认学期（数据迁移创建）');

-- 4.2 获取默认学期ID（通过变量）
SET @default_semester_id = LAST_INSERT_ID();

-- 4.3 为所有现有课程创建开课实例
INSERT INTO course_offering (course_id, semester_id, teacher_id, status, max_students, current_students)
SELECT
    id as course_id,
    @default_semester_id as semester_id,
    teacher_id,
    status,
    COALESCE(max_students, 0) as max_students,
    COALESCE(current_students, 0) as current_students
FROM course
WHERE deleted = 0;

-- 4.4 更新 course_class 表的 offering_id
UPDATE course_class cc
INNER JOIN course_offering co ON cc.course_id = co.course_id AND co.semester_id = @default_semester_id
SET cc.offering_id = co.id;

-- 4.5 更新 homework 表的 offering_id
UPDATE homework h
INNER JOIN course_offering co ON h.course_id = co.course_id AND co.semester_id = @default_semester_id
SET h.offering_id = co.id
WHERE h.deleted = 0;

-- 4.6 更新 exam 表的 offering_id
UPDATE exam e
INNER JOIN course_offering co ON e.course_id = co.course_id AND co.semester_id = @default_semester_id
SET e.offering_id = co.id
WHERE e.deleted = 0;

-- 4.7 更新 student_course 表的 offering_id
UPDATE student_course sc
INNER JOIN course_offering co ON sc.course_id = co.course_id AND co.semester_id = @default_semester_id
SET sc.offering_id = co.id
WHERE sc.deleted = 0;

-- 5. 添加外键约束（可选，根据需要启用）
-- ALTER TABLE course_offering ADD CONSTRAINT fk_offering_course FOREIGN KEY (course_id) REFERENCES course(id);
-- ALTER TABLE course_offering ADD CONSTRAINT fk_offering_semester FOREIGN KEY (semester_id) REFERENCES semester(id);
-- ALTER TABLE course_class ADD CONSTRAINT fk_course_class_offering FOREIGN KEY (offering_id) REFERENCES course_offering(id);
-- ALTER TABLE homework ADD CONSTRAINT fk_homework_offering FOREIGN KEY (offering_id) REFERENCES course_offering(id);
-- ALTER TABLE exam ADD CONSTRAINT fk_exam_offering FOREIGN KEY (offering_id) REFERENCES course_offering(id);
-- ALTER TABLE student_course ADD CONSTRAINT fk_student_course_offering FOREIGN KEY (offering_id) REFERENCES course_offering(id);

-- 6. 验证数据迁移
SELECT
    '学期数' as item,
    COUNT(*) as count
FROM semester
UNION ALL
SELECT
    '开课实例数' as item,
    COUNT(*) as count
FROM course_offering
UNION ALL
SELECT
    '已迁移的课程-班级关联' as item,
    COUNT(*) as count
FROM course_class WHERE offering_id IS NOT NULL
UNION ALL
SELECT
    '已迁移的作业' as item,
    COUNT(*) as count
FROM homework WHERE offering_id IS NOT NULL
UNION ALL
SELECT
    '已迁移的考试' as item,
    COUNT(*) as count
FROM exam WHERE offering_id IS NOT NULL
UNION ALL
SELECT
    '已迁移的学生选课' as item,
    COUNT(*) as count
FROM student_course WHERE offering_id IS NOT NULL;

-- 注意：
-- 1. 运行此脚本前请备份数据库
-- 2. offering_id 字段暂时允许 NULL，以保持向后兼容
-- 3. 后续新增数据时，应优先使用 offering_id
-- 4. 可以逐步将 course_id 字段标记为废弃
