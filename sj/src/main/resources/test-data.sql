-- ====================================
-- 测试数据插入脚本
-- ====================================

-- 插入测试教师用户（密码：123456）
INSERT INTO sys_user (username, password, real_name, email, user_type, teacher_no, status) VALUES
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张老师', 'teacher1@example.com', 2, 'T001', 1);

-- 插入测试学生用户（密码：123456）
INSERT INTO sys_user (username, password, real_name, email, user_type, student_no, status) VALUES
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李同学', 'student1@example.com', 1, '2021001', 1),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王同学', 'student2@example.com', 1, '2021002', 1);

-- 插入测试课程
INSERT INTO course (course_name, course_code, teacher_id, description, credit, total_hours, semester, status) VALUES
('Java程序设计', 'CS101', 2, 'Java编程基础课程', 4.0, 64, '2024春季', 1),
('数据结构与算法', 'CS102', 2, '数据结构与算法设计', 4.0, 64, '2024春季', 1);

-- 插入测试班级
INSERT INTO class (class_name, class_code, grade, major, course_id, teacher_id, student_count) VALUES
('计算机2021级1班', 'CS2021-1', '2021', '计算机科学与技术', 1, 2, 2);

-- 关联学生到班级
INSERT INTO student_class (student_id, class_id) VALUES
(3, 1),
(4, 1);

-- 插入测试实验
INSERT INTO experiment (experiment_name, course_id, teacher_id, description, requirement, template_code, language, time_limit, memory_limit, total_score, start_time, end_time, status) VALUES
('Hello World程序', 1, 2, '编写一个简单的Hello World程序', '要求输出"Hello World!"', 'public class Main {\n    public static void main(String[] args) {\n        // 在此编写代码\n    }\n}', 'java', 1000, 128, 100, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);
