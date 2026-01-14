-- ====================================
-- 实验测试数据（包含测试用例）
-- ====================================

-- 插入一个简单的加法实验（Java）
INSERT INTO experiment (
    experiment_name, 
    course_id, 
    teacher_id, 
    description, 
    requirement, 
    template_code, 
    test_cases, 
    language, 
    time_limit, 
    memory_limit, 
    total_score, 
    start_time, 
    end_time, 
    status
) VALUES (
    '两数相加',
    1,
    2,
    '编写一个程序，读取两个整数并输出它们的和',
    '从标准输入读取两个整数（每行一个），计算它们的和并输出',
    'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // 读取第一个数
        int a = scanner.nextInt();
        // 读取第二个数
        int b = scanner.nextInt();
        // 输出和
        System.out.println(a + b);
        scanner.close();
    }
}',
    '[
        {"input": "5\\n3\\n", "output": "8"},
        {"input": "10\\n20\\n", "output": "30"},
        {"input": "100\\n200\\n", "output": "300"},
        {"input": "-5\\n5\\n", "output": "0"},
        {"input": "0\\n0\\n", "output": "0"}
    ]',
    'java',
    5000,
    256,
    100,
    NOW(),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    1
);

-- 插入一个 Hello World 实验（Python）
INSERT INTO experiment (
    experiment_name, 
    course_id, 
    teacher_id, 
    description, 
    requirement, 
    template_code, 
    test_cases, 
    language, 
    time_limit, 
    memory_limit, 
    total_score, 
    start_time, 
    end_time, 
    status
) VALUES (
    'Hello World (Python)',
    1,
    2,
    '编写一个输出 Hello World 的 Python 程序',
    '输出 "Hello World!"',
    '# 在此编写代码
print("Hello World!")',
    '[
        {"input": "", "output": "Hello World!"}
    ]',
    'python',
    3000,
    128,
    100,
    NOW(),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    1
);

-- 插入一个数组求和实验（C++）
INSERT INTO experiment (
    experiment_name, 
    course_id, 
    teacher_id, 
    description, 
    requirement, 
    template_code, 
    test_cases, 
    language, 
    time_limit, 
    memory_limit, 
    total_score, 
    start_time, 
    end_time, 
    status
) VALUES (
    '数组求和',
    1,
    2,
    '编写一个程序，读取 n 个整数并输出它们的和',
    '第一行输入整数 n，第二行输入 n 个整数（空格分隔），输出它们的和',
    '#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    
    int sum = 0;
    for (int i = 0; i < n; i++) {
        int num;
        cin >> num;
        sum += num;
    }
    
    cout << sum << endl;
    return 0;
}',
    '[
        {"input": "3\\n1 2 3\\n", "output": "6"},
        {"input": "5\\n10 20 30 40 50\\n", "output": "150"},
        {"input": "1\\n100\\n", "output": "100"},
        {"input": "4\\n-5 5 -10 10\\n", "output": "0"}
    ]',
    'cpp',
    5000,
    256,
    100,
    NOW(),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    1
);

-- 插入一个斐波那契数列实验（JavaScript）
INSERT INTO experiment (
    experiment_name, 
    course_id, 
    teacher_id, 
    description, 
    requirement, 
    template_code, 
    test_cases, 
    language, 
    time_limit, 
    memory_limit, 
    total_score, 
    start_time, 
    end_time, 
    status
) VALUES (
    '斐波那契数列',
    1,
    2,
    '编写一个程序，计算斐波那契数列的第 n 项',
    '输入一个整数 n (n >= 0)，输出斐波那契数列的第 n 项（F(0)=0, F(1)=1）',
    'const readline = require(''readline'');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

rl.on(''line'', (line) => {
    const n = parseInt(line);
    
    function fibonacci(n) {
        if (n <= 1) return n;
        let a = 0, b = 1;
        for (let i = 2; i <= n; i++) {
            let temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }
    
    console.log(fibonacci(n));
    rl.close();
});',
    '[
        {"input": "0\\n", "output": "0"},
        {"input": "1\\n", "output": "1"},
        {"input": "5\\n", "output": "5"},
        {"input": "10\\n", "output": "55"},
        {"input": "15\\n", "output": "610"}
    ]',
    'javascript',
    5000,
    256,
    100,
    NOW(),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    1
);

-- 插入一个判断奇偶数实验（C）
INSERT INTO experiment (
    experiment_name, 
    course_id, 
    teacher_id, 
    description, 
    requirement, 
    template_code, 
    test_cases, 
    language, 
    time_limit, 
    memory_limit, 
    total_score, 
    start_time, 
    end_time, 
    status
) VALUES (
    '判断奇偶数',
    1,
    2,
    '编写一个程序，判断输入的整数是奇数还是偶数',
    '输入一个整数，如果是偶数输出 "even"，如果是奇数输出 "odd"',
    '#include <stdio.h>

int main() {
    int n;
    scanf("%d", &n);
    
    if (n % 2 == 0) {
        printf("even\\n");
    } else {
        printf("odd\\n");
    }
    
    return 0;
}',
    '[
        {"input": "2\\n", "output": "even"},
        {"input": "3\\n", "output": "odd"},
        {"input": "0\\n", "output": "even"},
        {"input": "100\\n", "output": "even"},
        {"input": "99\\n", "output": "odd"}
    ]',
    'c',
    3000,
    128,
    100,
    NOW(),
    DATE_ADD(NOW(), INTERVAL 30 DAY),
    1
);
