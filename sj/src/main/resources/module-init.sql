-- 模块管理表
CREATE TABLE IF NOT EXISTS sys_module (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模块ID',
    module_key VARCHAR(50) NOT NULL UNIQUE COMMENT '模块标识',
    module_name VARCHAR(50) NOT NULL COMMENT '模块名称',
    icon VARCHAR(50) COMMENT '图标',
    path VARCHAR(100) COMMENT '路由路径',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统模块表';

-- 角色模块关联表
CREATE TABLE IF NOT EXISTS sys_role_module (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_type INT NOT NULL COMMENT '角色类型：1学生 2教师 3管理员',
    module_id BIGINT NOT NULL COMMENT '模块ID',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_module (role_type, module_id),
    INDEX idx_role_type (role_type),
    INDEX idx_module_id (module_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色模块关联表';

-- 初始化模块数据
INSERT INTO sys_module (module_key, module_name, icon, path, sort_order) VALUES
('dashboard', '首页', 'HomeFilled', '/dashboard', 1),
('courses', '课程管理', 'Reading', '/courses', 2),
('my-courses', '我的课程', 'Reading', '/my-courses', 3),
('classes', '班级管理', 'School', '/classes', 4),
('homeworks', '作业管理', 'EditPen', '/homeworks', 5),
('my-homeworks', '我的作业', 'EditPen', '/my-homeworks', 6),
('exams', '考试管理', 'Document', '/exams', 7),
('my-exams', '我的考试', 'Document', '/my-exams', 8),
('questions', '题库管理', 'Collection', '/questions', 9),
('experiments', '实验管理', 'Monitor', '/experiments', 10),
('my-experiments', '我的实验', 'Monitor', '/my-experiments', 11),
('batch-import', '批量导入学生', 'Upload', '/user-batch-import', 12),
('admin-users', '用户管理', 'UserFilled', '/admin/users', 13),
('admin-modules', '模块管理', 'Menu', '/admin/modules', 14),
('ai-chat', 'AI助教', 'ChatDotRound', '/ai', 15);

-- 初始化管理员权限（管理员拥有所有权限）
INSERT INTO sys_role_module (role_type, module_id, enabled)
SELECT 3, id, 1 FROM sys_module;

-- 初始化教师权限
INSERT INTO sys_role_module (role_type, module_id, enabled)
SELECT 2, id, 1 FROM sys_module
WHERE module_key IN ('dashboard', 'courses', 'classes', 'homeworks', 'exams', 'questions', 'experiments', 'batch-import', 'ai-chat');

-- 初始化学生权限
INSERT INTO sys_role_module (role_type, module_id, enabled)
SELECT 1, id, 1 FROM sys_module
WHERE module_key IN ('dashboard', 'my-courses', 'my-homeworks', 'my-exams', 'my-experiments', 'ai-chat');
