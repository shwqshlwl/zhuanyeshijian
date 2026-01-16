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

-- ====================================
-- 题库测试数据
-- ====================================

-- 插入单选题
INSERT INTO question_bank (question_type_id, course_id, teacher_id, question_content, options, correct_answer, analysis, difficulty, score, tags) VALUES
-- Java程序设计单选题
(1, 1, 2, '以下哪个关键字用于声明Java类？', '["class", "public", "void", "static"]', 'A', 'class是Java中用于声明类的关键字。', 1, 5, 'Java基础,关键字'),
(1, 1, 2, '在Java中，哪个数据类型用于存储整数？', '["int", "float", "char", "boolean"]', 'A', 'int是Java中的整数数据类型。', 1, 5, 'Java基础,数据类型'),
(1, 1, 2, '以下哪个不是Java的访问修饰符？', '["public", "private", "protected", "internal"]', 'D', 'Java的访问修饰符只有public、private、protected和默认（包访问），没有internal。', 2, 5, 'Java基础,访问修饰符'),
(1, 1, 2, '在Java中，字符串连接使用哪个运算符？', '["+", "-", "*", "/"]', 'A', 'Java中使用+运算符进行字符串连接。', 1, 5, 'Java基础,运算符'),
(1, 1, 2, '以下哪个方法用于获取字符串的长度？', '["length()", "size()", "count()", "getLength()"]', 'A', 'String类的length()方法用于获取字符串的长度。', 1, 5, 'Java基础,字符串'),
-- 数据结构与算法单选题
(1, 2, 2, '以下哪种数据结构是后进先出（LIFO）的？', '["队列", "栈", "数组", "链表"]', 'B', '栈是后进先出的数据结构。', 1, 5, '数据结构,栈'),
(1, 2, 2, '快速排序的时间复杂度是？', '["O(n)", "O(nlogn)", "O(n²)", "O(logn)"]', 'B', '快速排序的平均时间复杂度是O(nlogn)。', 3, 10, '算法,排序,时间复杂度');

-- 插入多选题
INSERT INTO question_bank (question_type_id, course_id, teacher_id, question_content, options, correct_answer, analysis, difficulty, score, tags) VALUES
(2, 1, 2, '以下哪些是Java的基本数据类型？', '["int", "String", "boolean", "double", "Object"]', 'A,C,D', 'Java的基本数据类型包括int、boolean、double等，String和Object是引用类型。', 1, 8, 'Java基础,数据类型'),
(2, 1, 2, '以下哪些语句可以正确创建数组？', '["int[] arr = new int[5];", "int arr[] = {1,2,3};", "int[] arr = new int[]{1,2,3};", "int arr = new int[5];"]', 'A,B,C', '选项A、B、C都是正确的数组创建方式，选项D缺少[]。', 2, 8, 'Java基础,数组'),
(2, 2, 2, '以下哪些排序算法是稳定的？', '["冒泡排序", "快速排序", "归并排序", "选择排序"]', 'A,C', '冒泡排序和归并排序是稳定的排序算法。', 3, 10, '算法,排序,稳定性');

-- 插入判断题
INSERT INTO question_bank (question_type_id, course_id, teacher_id, question_content, correct_answer, analysis, difficulty, score, tags) VALUES
(3, 1, 2, 'Java中的数组长度是固定的，一旦创建不能改变。', '1', 'Java数组的长度在创建时确定，不能动态改变长度。', 1, 5, 'Java基础,数组'),
(3, 1, 2, '在Java中，所有的对象都存储在堆内存中。', '0', 'Java中基本数据类型的变量存储在栈中，对象引用在栈中，对象本身在堆中。', 2, 5, 'Java基础,内存'),
(3, 1, 2, 'Java支持多重继承。', '0', 'Java不支持类的多重继承，但支持接口的多重实现。', 2, 5, 'Java基础,继承'),
(3, 2, 2, '栈是一种先进后出的数据结构。', '1', '栈的特点是后进先出（LIFO）。', 1, 5, '数据结构,栈'),
(3, 2, 2, '二分查找要求数据必须是有序的。', '1', '二分查找的前提是数据必须有序。', 2, 5, '算法,查找');

-- 插入填空题
INSERT INTO question_bank (question_type_id, course_id, teacher_id, question_content, correct_answer, analysis, difficulty, score, tags) VALUES
(4, 1, 2, '在Java中，main方法的签名是：public static void ____(String[] args)', 'main', 'Java程序的入口方法必须是public static void main(String[] args)。', 1, 5, 'Java基础,main方法'),
(4, 1, 2, 'Java中的注释符号//用于 ____ 注释。', '单行', '//用于单行注释，/* */用于多行注释。', 1, 5, 'Java基础,注释'),
(4, 1, 2, '在Java中，创建对象使用关键字 ____ 。', 'new', '使用new关键字在堆内存中创建对象。', 1, 5, 'Java基础,对象创建'),
(4, 2, 2, '数据结构中，栈的两个基本操作是 ____ 和 ____ 。', '入栈,出栈', '栈的基本操作是push（入栈）和pop（出栈）。', 2, 8, '数据结构,栈'),
(4, 2, 2, '时间复杂度O(n²)的排序算法有 ____ 排序和 ____ 排序。', '冒泡,选择', '冒泡排序和选择排序的时间复杂度都是O(n²)。', 2, 8, '算法,排序');

-- 插入简答题
INSERT INTO question_bank (question_type_id, course_id, teacher_id, question_content, correct_answer, analysis, difficulty, score, tags) VALUES
(5, 1, 2, '简述Java中的三大特性是什么？', '封装、继承、多态', 'Java的三大特性是：封装（Encapsulation）、继承（Inheritance）和多态（Polymorphism）。封装隐藏了对象的内部实现细节，只对外提供访问接口；继承允许子类继承父类的属性和方法；多态允许同一个方法在不同对象上有不同的表现。', 2, 15, 'Java基础,面向对象'),
(5, 1, 2, '什么是方法重载？请举例说明。', '方法重载是指在同一个类中，方法名相同但参数列表不同的多个方法。', '方法重载允许同一个类中有多个同名方法，但参数列表必须不同（参数个数、类型或顺序）。例如：public void print(int x)和public void print(String s)就是方法重载。', 2, 15, 'Java基础,方法重载'),
(5, 2, 2, '什么是时间复杂度和空间复杂度？', '时间复杂度表示算法执行所需的时间，空间复杂度表示算法执行所需的内存空间。', '时间复杂度用大O表示法描述算法运行时间的增长趋势，如O(1)、O(n)、O(n²)等。空间复杂度描述算法在运行过程中临时占用存储空间的大小。', 2, 15, '算法,复杂度'),
(5, 2, 2, '简述栈和队列的区别。', '栈是后进先出，队列是先进先出。', '栈（Stack）：后进先出（LIFO），主要操作是push和pop。队列（Queue）：先进先出（FIFO），主要操作是enqueue和dequeue。', 2, 15, '数据结构,栈,队列');

-- 插入编程题
INSERT INTO question_bank (question_type_id, course_id, teacher_id, question_content, correct_answer, analysis, difficulty, score, tags) VALUES
(6, 1, 2, '编写一个Java程序，计算1到100的和。', 'public class Main {
    public static void main(String[] args) {
        int sum = 0;
        for(int i = 1; i <= 100; i++) {
            sum += i;
        }
        System.out.println("1到100的和是：" + sum);
    }
}', '这道题考察基本的循环和累加操作。可以使用for循环或while循环实现。', 1, 20, 'Java基础,循环'),
(6, 1, 2, '编写一个Java程序，判断一个数是否为素数。', 'public class Main {
    public static void main(String[] args) {
        int num = 17; // 可以改为其他数字测试
        boolean isPrime = true;
        
        if(num <= 1) {
            isPrime = false;
        } else {
            for(int i = 2; i <= Math.sqrt(num); i++) {
                if(num % i == 0) {
                    isPrime = false;
                    break;
                }
            }
        }
        
        System.out.println(num + (isPrime ? "是" : "不是") + "素数");
    }
}', '素数判断需要检查从2到sqrt(n)的所有整数是否能整除n。', 2, 25, 'Java基础,算法,素数'),
(6, 2, 2, '实现一个栈的数据结构，要求包含push、pop和isEmpty方法。', 'public class MyStack {
    private int[] arr;
    private int top;
    private int capacity;
    
    public MyStack(int capacity) {
        this.capacity = capacity;
        this.arr = new int[capacity];
        this.top = -1;
    }
    
    public void push(int x) {
        if(top == capacity - 1) {
            System.out.println("栈已满");
            return;
        }
        arr[++top] = x;
    }
    
    public int pop() {
        if(isEmpty()) {
            System.out.println("栈为空");
            return -1;
        }
        return arr[top--];
    }
    
    public boolean isEmpty() {
        return top == -1;
    }
}', '栈的实现需要维护一个数组和top指针，push时top++，pop时top--。', 2, 30, '数据结构,栈,Java'),
(6, 2, 2, '编写一个冒泡排序算法。', 'public class Main {
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for(int i = 0; i < n - 1; i++) {
            for(int j = 0; j < n - i - 1; j++) {
                if(arr[j] > arr[j + 1]) {
                    // 交换arr[j]和arr[j+1]
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
    
    public static void main(String[] args) {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        bubbleSort(arr);
        System.out.println("排序后的数组：");
        for(int num : arr) {
            System.out.print(num + " ");
        }
    }
}', '冒泡排序通过相邻元素比较和交换来排序，每轮将最大元素移到末尾。', 2, 30, '算法,排序,冒泡排序');