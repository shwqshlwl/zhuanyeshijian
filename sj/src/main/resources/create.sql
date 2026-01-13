-- ====================================
-- 课程智慧教学平台数据库表结构
-- ====================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS course_teaching_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE course_teaching_platform;

-- ====================================
-- 用户权限相关表
-- ====================================

-- 用户表
CREATE TABLE sys_user (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                          username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                          password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
                          real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
                          email VARCHAR(100) COMMENT '邮箱',
                          phone VARCHAR(20) COMMENT '手机号',
                          avatar VARCHAR(255) COMMENT '头像URL',
                          user_type TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型：1-学生，2-教师，3-管理员',
                          student_no VARCHAR(50) COMMENT '学号（学生）',
                          teacher_no VARCHAR(50) COMMENT '工号（教师）',
                          status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
                          deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          INDEX idx_username (username),
                          INDEX idx_student_no (student_no),
                          INDEX idx_teacher_no (teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE sys_role (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
                          role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
                          role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
                          description VARCHAR(255) COMMENT '角色描述',
                          deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE sys_user_role (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                               user_id BIGINT NOT NULL COMMENT '用户ID',
                               role_id BIGINT NOT NULL COMMENT '角色ID',
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               UNIQUE KEY uk_user_role (user_id, role_id),
                               INDEX idx_user_id (user_id),
                               INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 菜单表
CREATE TABLE sys_menu (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
                          parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
                          menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
                          menu_code VARCHAR(50) COMMENT '菜单编码',
                          path VARCHAR(200) COMMENT '路由路径',
                          component VARCHAR(200) COMMENT '组件路径',
                          icon VARCHAR(100) COMMENT '菜单图标',
                          menu_type TINYINT NOT NULL COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
                          permission VARCHAR(100) COMMENT '权限标识',
                          sort_order INT DEFAULT 0 COMMENT '排序',
                          visible TINYINT DEFAULT 1 COMMENT '是否可见：0-隐藏，1-显示',
                          deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ====================================
-- 课程相关表
-- ====================================

-- 课程表
CREATE TABLE course (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
                        course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
                        course_code VARCHAR(50) NOT NULL UNIQUE COMMENT '课程编码',
                        teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
                        description TEXT COMMENT '课程描述',
                        cover_image VARCHAR(255) COMMENT '课程封面',
                        syllabus TEXT COMMENT '课程大纲',
                        credit DECIMAL(3,1) COMMENT '学分',
                        total_hours INT COMMENT '总学时',
                        semester VARCHAR(20) COMMENT '学期',
                        status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-未开课，1-进行中，2-已结束',
                        deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        INDEX idx_teacher_id (teacher_id),
                        INDEX idx_course_code (course_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ====================================
-- 班级相关表
-- ====================================

-- 班级表
CREATE TABLE class (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班级ID',
                       class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
                       class_code VARCHAR(50) NOT NULL UNIQUE COMMENT '班级编码',
                       grade VARCHAR(20) COMMENT '年级',
                       major VARCHAR(100) COMMENT '专业',
                       course_id BIGINT COMMENT '关联课程ID',
                       teacher_id BIGINT COMMENT '班主任/任课教师ID',
                       student_count INT DEFAULT 0 COMMENT '学生人数',
                       description TEXT COMMENT '班级描述',
                       deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                       create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                       INDEX idx_course_id (course_id),
                       INDEX idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 学生班级关联表
CREATE TABLE student_class (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                               student_id BIGINT NOT NULL COMMENT '学生ID',
                               class_id BIGINT NOT NULL COMMENT '班级ID',
                               join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               UNIQUE KEY uk_student_class (student_id, class_id),
                               INDEX idx_student_id (student_id),
                               INDEX idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生班级关联表';

-- ====================================
-- 作业相关表
-- ====================================

-- 作业表
CREATE TABLE homework (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '作业ID',
                          homework_title VARCHAR(200) NOT NULL COMMENT '作业标题',
                          course_id BIGINT NOT NULL COMMENT '课程ID',
                          class_id BIGINT COMMENT '班级ID',
                          teacher_id BIGINT NOT NULL COMMENT '发布教师ID',
                          content TEXT COMMENT '作业内容',
                          attachment VARCHAR(500) COMMENT '附件URL',
                          total_score INT DEFAULT 100 COMMENT '总分',
                          start_time DATETIME COMMENT '开始时间',
                          end_time DATETIME COMMENT '截止时间',
                          status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已截止',
                          allow_late TINYINT DEFAULT 0 COMMENT '是否允许迟交：0-不允许，1-允许',
                          deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          INDEX idx_course_id (course_id),
                          INDEX idx_class_id (class_id),
                          INDEX idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业表';

-- 作业提交表
CREATE TABLE homework_submit (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提交ID',
                                 homework_id BIGINT NOT NULL COMMENT '作业ID',
                                 student_id BIGINT NOT NULL COMMENT '学生ID',
                                 content TEXT COMMENT '提交内容',
                                 attachment VARCHAR(500) COMMENT '附件URL',
                                 submit_time DATETIME COMMENT '提交时间',
                                 score INT COMMENT '得分',
                                 comment TEXT COMMENT '教师评语',
                                 status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未提交，1-已提交，2-已批改',
                                 is_late TINYINT DEFAULT 0 COMMENT '是否迟交：0-否，1-是',
                                 grade_time DATETIME COMMENT '批改时间',
                                 grader_id BIGINT COMMENT '批改教师ID',
                                 deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 UNIQUE KEY uk_homework_student (homework_id, student_id),
                                 INDEX idx_homework_id (homework_id),
                                 INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交表';

-- ====================================
-- 题库相关表
-- ====================================

-- 题型表
CREATE TABLE question_type (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题型ID',
                               type_name VARCHAR(50) NOT NULL UNIQUE COMMENT '题型名称',
                               type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '题型编码：single_choice, multiple_choice, true_false, fill_blank, short_answer, programming',
                               description VARCHAR(255) COMMENT '题型描述',
                               deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题型表';

-- 题库表
CREATE TABLE question_bank (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
                               question_type_id BIGINT NOT NULL COMMENT '题型ID',
                               course_id BIGINT COMMENT '关联课程ID',
                               teacher_id BIGINT NOT NULL COMMENT '创建教师ID',
                               question_content TEXT NOT NULL COMMENT '题目内容',
                               options TEXT COMMENT '选项（JSON格式）',
                               correct_answer TEXT COMMENT '正确答案',
                               analysis TEXT COMMENT '答案解析',
                               difficulty TINYINT DEFAULT 1 COMMENT '难度：1-简单，2-中等，3-困难',
                               score INT DEFAULT 5 COMMENT '默认分值',
                               tags VARCHAR(255) COMMENT '标签',
                               usage_count INT DEFAULT 0 COMMENT '使用次数',
                               deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               INDEX idx_question_type_id (question_type_id),
                               INDEX idx_course_id (course_id),
                               INDEX idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- ====================================
-- 考试相关表
-- ====================================

-- 考试表
CREATE TABLE exam (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考试ID',
                      exam_name VARCHAR(200) NOT NULL COMMENT '考试名称',
                      course_id BIGINT NOT NULL COMMENT '课程ID',
                      class_id BIGINT COMMENT '班级ID',
                      teacher_id BIGINT NOT NULL COMMENT '创建教师ID',
                      description TEXT COMMENT '考试说明',
                      total_score INT DEFAULT 100 COMMENT '总分',
                      pass_score INT DEFAULT 60 COMMENT '及格分',
                      duration INT COMMENT '考试时长（分钟）',
                      start_time DATETIME COMMENT '开始时间',
                      end_time DATETIME COMMENT '结束时间',
                      status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
                      exam_type TINYINT DEFAULT 1 COMMENT '考试类型：1-随堂测验，2-期中考试，3-期末考试',
                      is_shuffle TINYINT DEFAULT 0 COMMENT '是否乱序：0-否，1-是',
                      show_answer TINYINT DEFAULT 0 COMMENT '是否显示答案：0-否，1-是',
                      deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      INDEX idx_course_id (course_id),
                      INDEX idx_class_id (class_id),
                      INDEX idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

-- 考试题目关联表
CREATE TABLE exam_question (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                               exam_id BIGINT NOT NULL COMMENT '考试ID',
                               question_id BIGINT NOT NULL COMMENT '题目ID',
                               question_score INT NOT NULL COMMENT '题目分值',
                               question_order INT COMMENT '题目顺序',
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               INDEX idx_exam_id (exam_id),
                               INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试题目关联表';

-- 考试试卷表
CREATE TABLE exam_paper (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '试卷ID',
                            exam_id BIGINT NOT NULL COMMENT '考试ID',
                            student_id BIGINT NOT NULL COMMENT '学生ID',
                            paper_content TEXT COMMENT '试卷内容（JSON格式）',
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            UNIQUE KEY uk_exam_student (exam_id, student_id),
                            INDEX idx_exam_id (exam_id),
                            INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试试卷表';

-- 考试记录表
CREATE TABLE exam_record (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
                             exam_id BIGINT NOT NULL COMMENT '考试ID',
                             student_id BIGINT NOT NULL COMMENT '学生ID',
                             paper_id BIGINT COMMENT '试卷ID',
                             answers TEXT COMMENT '学生答案（JSON格式）',
                             score DECIMAL(5,2) COMMENT '得分',
                             start_time DATETIME COMMENT '开始时间',
                             submit_time DATETIME COMMENT '提交时间',
                             status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-答题中，2-已提交',
                             is_timeout TINYINT DEFAULT 0 COMMENT '是否超时：0-否，1-是',
                             deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             UNIQUE KEY uk_exam_student (exam_id, student_id),
                             INDEX idx_exam_id (exam_id),
                             INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- ====================================
-- 实验相关表
-- ====================================

-- 实验表
CREATE TABLE experiment (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '实验ID',
                            experiment_name VARCHAR(200) NOT NULL COMMENT '实验名称',
                            course_id BIGINT NOT NULL COMMENT '课程ID',
                            teacher_id BIGINT NOT NULL COMMENT '创建教师ID',
                            description TEXT COMMENT '实验描述',
                            requirement TEXT COMMENT '实验要求',
                            template_code TEXT COMMENT '模板代码',
                            test_cases TEXT COMMENT '测试用例（JSON格式）',
                            language VARCHAR(50) COMMENT '编程语言：java, python, c, cpp',
                            time_limit INT DEFAULT 1000 COMMENT '时间限制（毫秒）',
                            memory_limit INT DEFAULT 128 COMMENT '内存限制（MB）',
                            total_score INT DEFAULT 100 COMMENT '总分',
                            start_time DATETIME COMMENT '开始时间',
                            end_time DATETIME COMMENT '截止时间',
                            status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已截止',
                            deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            INDEX idx_course_id (course_id),
                            INDEX idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验表';

-- 实验提交表
CREATE TABLE experiment_submit (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提交ID',
                                   experiment_id BIGINT NOT NULL COMMENT '实验ID',
                                   student_id BIGINT NOT NULL COMMENT '学生ID',
                                   code TEXT COMMENT '提交代码',
                                   language VARCHAR(50) COMMENT '编程语言',
                                   submit_time DATETIME COMMENT '提交时间',
                                   status TINYINT COMMENT '状态：0-待评测，1-评测中，2-通过，3-未通过，4-编译错误，5-运行错误',
                                   score INT COMMENT '得分',
                                   pass_count INT DEFAULT 0 COMMENT '通过测试用例数',
                                   total_count INT DEFAULT 0 COMMENT '总测试用例数',
                                   execute_time INT COMMENT '执行时间（毫秒）',
                                   memory_used INT COMMENT '内存使用（KB）',
                                   error_message TEXT COMMENT '错误信息',
                                   result_detail TEXT COMMENT '详细结果（JSON格式）',
                                   deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
                                   create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   INDEX idx_experiment_id (experiment_id),
                                   INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验提交表';

-- ====================================
-- 初始化数据
-- ====================================

-- 插入默认角色
INSERT INTO sys_role (role_name, role_code, description) VALUES
                                                             ('管理员', 'ROLE_ADMIN', '系统管理员'),
                                                             ('教师', 'ROLE_TEACHER', '教师角色'),
                                                             ('学生', 'ROLE_STUDENT', '学生角色');

-- 插入默认题型
INSERT INTO question_type (type_name, type_code, description) VALUES
                                                                  ('单选题', 'single_choice', '单项选择题'),
                                                                  ('多选题', 'multiple_choice', '多项选择题'),
                                                                  ('判断题', 'true_false', '判断题'),
                                                                  ('填空题', 'fill_blank', '填空题'),
                                                                  ('简答题', 'short_answer', '简答题'),
                                                                  ('编程题', 'programming', '编程题');

-- 插入默认管理员用户（密码：admin123）
INSERT INTO sys_user (username, password, real_name, email, user_type, status) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@example.com', 3, 1);

-- 关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
