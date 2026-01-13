-- ====================================
-- 初始化数据
-- ====================================

-- 插入默认角色（忽略重复）
INSERT IGNORE INTO sys_role (id, role_name, role_code, description) VALUES
(1, '管理员', 'ROLE_ADMIN', '系统管理员'),
(2, '教师', 'ROLE_TEACHER', '教师角色'),
(3, '学生', 'ROLE_STUDENT', '学生角色');

-- 插入默认题型（忽略重复）
INSERT IGNORE INTO question_type (id, type_name, type_code, description) VALUES
(1, '单选题', 'single_choice', '单项选择题'),
(2, '多选题', 'multiple_choice', '多项选择题'),
(3, '判断题', 'true_false', '判断题'),
(4, '填空题', 'fill_blank', '填空题'),
(5, '简答题', 'short_answer', '简答题'),
(6, '编程题', 'programming', '编程题');

-- 插入默认管理员用户（密码：123456）
-- BCrypt 加密后的 123456
INSERT INTO sys_user (id, username, password, real_name, email, user_type, status) VALUES
(1, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Zf4a/G/ZqK7.U7AxNqKGe', '系统管理员', 'admin@example.com', 3, 1)
ON DUPLICATE KEY UPDATE password = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36Zf4a/G/ZqK7.U7AxNqKGe';

-- 关联管理员角色（忽略重复）
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);
