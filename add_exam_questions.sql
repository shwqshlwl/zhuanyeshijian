-- Add questions to exam ID 4 (javaweb exam)
-- These questions are from course 2 (数据结构与算法)

INSERT INTO exam_question (exam_id, question_id, question_score, question_order) VALUES
(4, 6, 8, 1),  -- 栈的两个基本操作是 ____ 和 ____ 。 (fill_blank)
(4, 7, 8, 2),  -- 数据结构中，栈的两个基本操作是 ____ 和 ____ 。 (fill_blank)
(4, 8, 10, 3), -- 以下哪些排序算法是稳定的？ (multiple_choice)
(4, 9, 30, 4), -- 实现一个栈的数据结构 (programming)
(4, 10, 30, 5); -- 编写一个冒泡排序算法 (programming)

-- Update exam total score and pass score
UPDATE exam SET total_score = 86, pass_score = 50 WHERE id = 4;