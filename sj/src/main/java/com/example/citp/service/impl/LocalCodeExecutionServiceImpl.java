package com.example.citp.service.impl;

import com.example.citp.service.CodeExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * 本地代码执行服务实现
 * 注意：此实现适用于开发环境，生产环境建议使用 Docker 容器隔离
 */
@Slf4j
@Service
public class LocalCodeExecutionServiceImpl implements CodeExecutionService {

    // 临时文件目录
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "code-execution";
    
    // 超时时间（秒）
    private static final int DEFAULT_TIMEOUT = 5;

    public LocalCodeExecutionServiceImpl() {
        // 创建临时目录
        try {
            Files.createDirectories(Paths.get(TEMP_DIR));
        } catch (IOException e) {
            log.error("创建临时目录失败", e);
        }
    }

    @Override
    public Map<String, Object> executeCode(String code, String language, String input, Integer timeLimit, Integer memoryLimit) {
        Map<String, Object> result = new HashMap<>();
        String workDir = TEMP_DIR + File.separator + UUID.randomUUID();

        try {
            // 创建工作目录
            Files.createDirectories(Paths.get(workDir));
            
            // 设置超时时间（秒）
            int timeout = timeLimit != null ? timeLimit / 1000 : DEFAULT_TIMEOUT;

            // 根据语言执行代码
            switch (language.toLowerCase()) {
                case "java":
                    return executeJava(code, input, timeout, workDir);
                case "python":
                    return executePython(code, input, timeout, workDir);
                case "cpp":
                case "c++":
                    return executeCpp(code, input, timeout, workDir);
                case "c":
                    return executeC(code, input, timeout, workDir);
                case "javascript":
                case "js":
                    return executeJavaScript(code, input, timeout, workDir);
                default:
                    result.put("success", false);
                    result.put("error", "不支持的编程语言: " + language);
                    return result;
            }
        } catch (Exception e) {
            log.error("代码执行失败", e);
            result.put("success", false);
            result.put("error", "执行失败: " + e.getMessage());
            return result;
        } finally {
            // 清理临时文件
            cleanupDirectory(workDir);
        }
    }

    @Override
    public Map<String, Object> compileCode(String code, String language) {
        Map<String, Object> result = new HashMap<>();
        String workDir = TEMP_DIR + File.separator + UUID.randomUUID();

        try {
            Files.createDirectories(Paths.get(workDir));

            switch (language.toLowerCase()) {
                case "java":
                    return compileJava(code, workDir);
                case "cpp":
                case "c++":
                    return compileCpp(code, workDir);
                case "c":
                    return compileC(code, workDir);
                default:
                    result.put("success", true);
                    result.put("message", "该语言无需编译");
                    return result;
            }
        } catch (Exception e) {
            log.error("代码编译失败", e);
            result.put("success", false);
            result.put("error", "编译失败: " + e.getMessage());
            return result;
        } finally {
            cleanupDirectory(workDir);
        }
    }

    /**
     * 执行 Java 代码
     */
    private Map<String, Object> executeJava(String code, String input, int timeoutSeconds, String workDir) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 提取类名
            String className = extractJavaClassName(code);
            if (className == null) {
                className = "Main";
            }

            // 写入源文件
            String sourceFile = workDir + File.separator + className + ".java";
            Files.writeString(Paths.get(sourceFile), code, StandardCharsets.UTF_8);

            // 编译
            Process compileProcess = Runtime.getRuntime().exec("javac -encoding UTF-8 " + sourceFile);
            int compileCode = compileProcess.waitFor();
            String compileError = readStream(compileProcess.getErrorStream());

            if (compileCode != 0) {
                result.put("success", false);
                result.put("error", "编译错误:\n" + compileError);
                return result;
            }

            // 运行（指定 UTF-8 编码）
            ProcessBuilder runBuilder = new ProcessBuilder(
                "java", "-Dfile.encoding=UTF-8", "-Xmx256m", "-cp", workDir, className
            );
            runBuilder.directory(new File(workDir));
            
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            Process runProcess = runBuilder.start();
            long pid = runProcess.pid();

            // 写入输入
            if (input != null && !input.isEmpty()) {
                try (OutputStream stdin = runProcess.getOutputStream()) {
                    stdin.write(input.getBytes(StandardCharsets.UTF_8));
                    stdin.flush();
                }
            }

            // 使用线程池等待执行完成（带超时）
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> runProcess.waitFor());
            
            // 监控内存使用（在另一个线程中）
            long maxMemory = 0;
            ExecutorService memoryMonitor = Executors.newSingleThreadExecutor();
            Future<Long> memoryFuture = memoryMonitor.submit(() -> {
                long max = 0;
                try {
                    while (runProcess.isAlive()) {
                        long currentMemory = getProcessMemory(pid);
                        if (currentMemory > max) {
                            max = currentMemory;
                        }
                        Thread.sleep(50); // 每50ms检查一次
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
                return max;
            });
            
            int runCode;
            String output = "";
            String error = "";

            try {
                runCode = future.get(timeoutSeconds, TimeUnit.SECONDS);
                output = readStream(runProcess.getInputStream());
                error = readStream(runProcess.getErrorStream());
                
                // 获取最大内存使用
                try {
                    maxMemory = memoryFuture.get(100, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    // 如果获取失败，使用0
                    maxMemory = 0;
                }
            } catch (TimeoutException e) {
                runProcess.destroy();
                result.put("success", false);
                result.put("error", "执行超时：程序超过 " + timeoutSeconds + " 秒未结束，可能存在死循环");
                return result;
            } finally {
                executor.shutdown();
                memoryMonitor.shutdownNow();
            }

            // 计算执行时间
            long endTime = System.currentTimeMillis();
            int executeTime = (int) (endTime - startTime);

            result.put("success", runCode == 0);
            result.put("output", output);
            result.put("error", error.isEmpty() ? null : error);
            result.put("exitCode", runCode);
            result.put("executeTime", executeTime); // 毫秒
            result.put("memoryUsed", (int) (maxMemory / 1024)); // 转换为 KB

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "执行异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 执行 Python 代码
     */
    private Map<String, Object> executePython(String code, String input, int timeoutSeconds, String workDir) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 尝试多个 Python 命令
            String[] pythonCommands = {"python", "python3", "py"};
            String pythonCmd = null;
            
            for (String cmd : pythonCommands) {
                if (isCommandAvailable(cmd)) {
                    pythonCmd = cmd;
                    log.info("找到可用的 Python 命令: {}", cmd);
                    break;
                }
            }

            if (pythonCmd == null) {
                result.put("success", false);
                result.put("error", "Python 未安装或未添加到 PATH。\n\n请安装 Python：\n1. 下载：https://www.python.org/downloads/\n2. 安装时勾选 'Add Python to PATH'\n3. 重启 IDE 和后端服务\n\n提示：如果已安装 Python，请确保命令 'python'、'python3' 或 'py' 可在命令行中使用");
                return result;
            }

            // 写入源文件（使用 UTF-8 编码）
            String sourceFile = workDir + File.separator + "main.py";
            Files.writeString(Paths.get(sourceFile), code, StandardCharsets.UTF_8);

            // 运行（设置 UTF-8 环境）
            ProcessBuilder pb = new ProcessBuilder(pythonCmd, "-u", sourceFile);
            pb.directory(new File(workDir));
            // 设置环境变量，确保 Python 使用 UTF-8
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            Process runProcess = pb.start();
            long pid = runProcess.pid();

            // 写入输入
            if (input != null && !input.isEmpty()) {
                try (OutputStream stdin = runProcess.getOutputStream()) {
                    stdin.write(input.getBytes(StandardCharsets.UTF_8));
                    stdin.flush();
                }
            }

            // 使用线程池等待执行完成（带超时）
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> runProcess.waitFor());
            
            // 监控内存使用
            long maxMemory = 0;
            ExecutorService memoryMonitor = Executors.newSingleThreadExecutor();
            Future<Long> memoryFuture = memoryMonitor.submit(() -> {
                long max = 0;
                try {
                    while (runProcess.isAlive()) {
                        long currentMemory = getProcessMemory(pid);
                        if (currentMemory > max) {
                            max = currentMemory;
                        }
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
                return max;
            });
            
            int runCode;
            String output = "";
            String error = "";

            try {
                runCode = future.get(timeoutSeconds, TimeUnit.SECONDS);
                output = readStream(runProcess.getInputStream());
                error = readStream(runProcess.getErrorStream());
                
                // 获取最大内存使用
                try {
                    maxMemory = memoryFuture.get(100, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    maxMemory = 0;
                }
            } catch (TimeoutException e) {
                runProcess.destroy();
                result.put("success", false);
                result.put("error", "执行超时：程序超过 " + timeoutSeconds + " 秒未结束，可能存在死循环");
                return result;
            } finally {
                executor.shutdown();
                memoryMonitor.shutdownNow();
            }

            // 计算执行时间
            long endTime = System.currentTimeMillis();
            int executeTime = (int) (endTime - startTime);

            result.put("success", runCode == 0);
            result.put("output", output);
            result.put("error", error.isEmpty() ? null : error);
            result.put("exitCode", runCode);
            result.put("executeTime", executeTime);
            result.put("memoryUsed", (int) (maxMemory / 1024));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "执行异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 执行 C++ 代码
     */
    private Map<String, Object> executeCpp(String code, String input, int timeoutSeconds, String workDir) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查 g++ 是否可用
            if (!isCommandAvailable("g++")) {
                result.put("success", false);
                result.put("error", "C++ 编译器 (g++) 未安装。\n\n请安装 MinGW-w64：\n1. 下载：https://sourceforge.net/projects/mingw-w64/\n2. 安装到 C:\\mingw64\n3. 添加 C:\\mingw64\\bin 到系统 PATH\n4. 重启后端服务");
                return result;
            }

            // 写入源文件
            String sourceFile = workDir + File.separator + "main.cpp";
            String execFile = workDir + File.separator + "main.exe";
            Files.writeString(Paths.get(sourceFile), code, StandardCharsets.UTF_8);

            // 编译
            Process compileProcess = Runtime.getRuntime().exec(new String[]{"g++", "-o", execFile, sourceFile});
            int compileCode = compileProcess.waitFor();
            String compileError = readStream(compileProcess.getErrorStream());

            if (compileCode != 0) {
                result.put("success", false);
                result.put("error", "编译错误:\n" + compileError);
                return result;
            }

            // 记录开始时间
            long startTime = System.currentTimeMillis();
            Process runProcess = Runtime.getRuntime().exec(execFile);
            long pid = runProcess.pid();

            // 写入输入
            if (input != null && !input.isEmpty()) {
                try (OutputStream stdin = runProcess.getOutputStream()) {
                    stdin.write(input.getBytes(StandardCharsets.UTF_8));
                    stdin.flush();
                }
            }

            // 使用线程池等待执行完成（带超时）
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> runProcess.waitFor());
            
            // 监控内存使用
            long maxMemory = 0;
            ExecutorService memoryMonitor = Executors.newSingleThreadExecutor();
            Future<Long> memoryFuture = memoryMonitor.submit(() -> {
                long max = 0;
                try {
                    while (runProcess.isAlive()) {
                        long currentMemory = getProcessMemory(pid);
                        if (currentMemory > max) {
                            max = currentMemory;
                        }
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
                return max;
            });
            
            int runCode;
            String output = "";
            String error = "";

            try {
                runCode = future.get(timeoutSeconds, TimeUnit.SECONDS);
                output = readStream(runProcess.getInputStream());
                error = readStream(runProcess.getErrorStream());
                
                // 获取最大内存使用
                try {
                    maxMemory = memoryFuture.get(100, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    maxMemory = 0;
                }
            } catch (TimeoutException e) {
                runProcess.destroy();
                result.put("success", false);
                result.put("error", "执行超时：程序超过 " + timeoutSeconds + " 秒未结束，可能存在死循环");
                return result;
            } finally {
                executor.shutdown();
                memoryMonitor.shutdownNow();
            }

            // 计算执行时间
            long endTime = System.currentTimeMillis();
            int executeTime = (int) (endTime - startTime);

            result.put("success", runCode == 0);
            result.put("output", output);
            result.put("error", error.isEmpty() ? null : error);
            result.put("exitCode", runCode);
            result.put("executeTime", executeTime);
            result.put("memoryUsed", (int) (maxMemory / 1024));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "执行异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 执行 C 代码
     */
    private Map<String, Object> executeC(String code, String input, int timeoutSeconds, String workDir) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查 gcc 是否可用
            if (!isCommandAvailable("gcc")) {
                result.put("success", false);
                result.put("error", "C 编译器 (gcc) 未安装。\n\n请安装 MinGW-w64：\n1. 下载：https://sourceforge.net/projects/mingw-w64/\n2. 安装到 C:\\mingw64\n3. 添加 C:\\mingw64\\bin 到系统 PATH\n4. 重启后端服务");
                return result;
            }

            // 写入源文件
            String sourceFile = workDir + File.separator + "main.c";
            String execFile = workDir + File.separator + "main.exe";
            Files.writeString(Paths.get(sourceFile), code, StandardCharsets.UTF_8);

            // 编译
            Process compileProcess = Runtime.getRuntime().exec(new String[]{"gcc", "-o", execFile, sourceFile});
            int compileCode = compileProcess.waitFor();
            String compileError = readStream(compileProcess.getErrorStream());

            if (compileCode != 0) {
                result.put("success", false);
                result.put("error", "编译错误:\n" + compileError);
                return result;
            }

            // 记录开始时间
            long startTime = System.currentTimeMillis();
            Process runProcess = Runtime.getRuntime().exec(execFile);
            long pid = runProcess.pid();

            // 写入输入
            if (input != null && !input.isEmpty()) {
                try (OutputStream stdin = runProcess.getOutputStream()) {
                    stdin.write(input.getBytes(StandardCharsets.UTF_8));
                    stdin.flush();
                }
            }

            // 使用线程池等待执行完成（带超时）
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> runProcess.waitFor());
            
            // 监控内存使用
            long maxMemory = 0;
            ExecutorService memoryMonitor = Executors.newSingleThreadExecutor();
            Future<Long> memoryFuture = memoryMonitor.submit(() -> {
                long max = 0;
                try {
                    while (runProcess.isAlive()) {
                        long currentMemory = getProcessMemory(pid);
                        if (currentMemory > max) {
                            max = currentMemory;
                        }
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
                return max;
            });
            
            int runCode;
            String output = "";
            String error = "";

            try {
                runCode = future.get(timeoutSeconds, TimeUnit.SECONDS);
                output = readStream(runProcess.getInputStream());
                error = readStream(runProcess.getErrorStream());
                
                // 获取最大内存使用
                try {
                    maxMemory = memoryFuture.get(100, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    maxMemory = 0;
                }
            } catch (TimeoutException e) {
                runProcess.destroy();
                result.put("success", false);
                result.put("error", "执行超时：程序超过 " + timeoutSeconds + " 秒未结束，可能存在死循环");
                return result;
            } finally {
                executor.shutdown();
                memoryMonitor.shutdownNow();
            }

            // 计算执行时间
            long endTime = System.currentTimeMillis();
            int executeTime = (int) (endTime - startTime);

            result.put("success", runCode == 0);
            result.put("output", output);
            result.put("error", error.isEmpty() ? null : error);
            result.put("exitCode", runCode);
            result.put("executeTime", executeTime);
            result.put("memoryUsed", (int) (maxMemory / 1024));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "执行异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 执行 JavaScript 代码
     */
    private Map<String, Object> executeJavaScript(String code, String input, int timeoutSeconds, String workDir) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查 Node.js 是否可用
            if (!isCommandAvailable("node")) {
                result.put("success", false);
                result.put("error", "Node.js 未安装。\n\n请安装 Node.js：\n1. 下载：https://nodejs.org/\n2. 安装\n3. 重启后端服务");
                return result;
            }

            // 写入源文件
            String sourceFile = workDir + File.separator + "main.js";
            Files.writeString(Paths.get(sourceFile), code, StandardCharsets.UTF_8);

            // 记录开始时间
            long startTime = System.currentTimeMillis();
            Process runProcess = Runtime.getRuntime().exec(new String[]{"node", sourceFile});
            long pid = runProcess.pid();

            // 写入输入
            if (input != null && !input.isEmpty()) {
                try (OutputStream stdin = runProcess.getOutputStream()) {
                    stdin.write(input.getBytes(StandardCharsets.UTF_8));
                    stdin.flush();
                }
            }

            // 使用线程池等待执行完成（带超时）
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> runProcess.waitFor());
            
            // 监控内存使用
            long maxMemory = 0;
            ExecutorService memoryMonitor = Executors.newSingleThreadExecutor();
            Future<Long> memoryFuture = memoryMonitor.submit(() -> {
                long max = 0;
                try {
                    while (runProcess.isAlive()) {
                        long currentMemory = getProcessMemory(pid);
                        if (currentMemory > max) {
                            max = currentMemory;
                        }
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    // 忽略异常
                }
                return max;
            });
            
            int runCode;
            String output = "";
            String error = "";

            try {
                runCode = future.get(timeoutSeconds, TimeUnit.SECONDS);
                output = readStream(runProcess.getInputStream());
                error = readStream(runProcess.getErrorStream());
                
                // 获取最大内存使用
                try {
                    maxMemory = memoryFuture.get(100, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    maxMemory = 0;
                }
            } catch (TimeoutException e) {
                runProcess.destroy();
                result.put("success", false);
                result.put("error", "执行超时：程序超过 " + timeoutSeconds + " 秒未结束，可能存在死循环");
                return result;
            } finally {
                executor.shutdown();
                memoryMonitor.shutdownNow();
            }

            // 计算执行时间
            long endTime = System.currentTimeMillis();
            int executeTime = (int) (endTime - startTime);

            result.put("success", runCode == 0);
            result.put("output", output);
            result.put("error", error.isEmpty() ? null : error);
            result.put("exitCode", runCode);
            result.put("executeTime", executeTime);
            result.put("memoryUsed", (int) (maxMemory / 1024));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "执行异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 编译 Java 代码
     */
    private Map<String, Object> compileJava(String code, String workDir) throws Exception {
        Map<String, Object> result = new HashMap<>();

        String className = extractJavaClassName(code);
        if (className == null) {
            className = "Main";
        }

        String sourceFile = workDir + File.separator + className + ".java";
        Files.writeString(Paths.get(sourceFile), code, StandardCharsets.UTF_8);

        ProcessBuilder compileBuilder = new ProcessBuilder("javac", "-encoding", "UTF-8", sourceFile);
        compileBuilder.directory(new File(workDir));
        Process compileProcess = compileBuilder.start();

        String compileError = readStream(compileProcess.getErrorStream());
        int compileExitCode = compileProcess.waitFor();

        result.put("success", compileExitCode == 0);
        result.put("error", compileExitCode == 0 ? null : compileError);

        return result;
    }

    /**
     * 编译 C++ 代码
     */
    private Map<String, Object> compileCpp(String code, String workDir) throws Exception {
        Map<String, Object> result = new HashMap<>();

        String sourceFile = workDir + File.separator + "main.cpp";
        String execFile = workDir + File.separator + "main.exe";
        Files.writeString(Paths.get(sourceFile), code, StandardCharsets.UTF_8);

        ProcessBuilder compileBuilder = new ProcessBuilder("g++", "-o", execFile, sourceFile);
        compileBuilder.directory(new File(workDir));
        Process compileProcess = compileBuilder.start();

        String compileError = readStream(compileProcess.getErrorStream());
        int compileExitCode = compileProcess.waitFor();

        result.put("success", compileExitCode == 0);
        result.put("error", compileExitCode == 0 ? null : compileError);

        return result;
    }

    /**
     * 编译 C 代码
     */
    private Map<String, Object> compileC(String code, String workDir) throws Exception {
        Map<String, Object> result = new HashMap<>();

        String sourceFile = workDir + File.separator + "main.c";
        String execFile = workDir + File.separator + "main.exe";
        Files.writeString(Paths.get(sourceFile), code, StandardCharsets.UTF_8);

        ProcessBuilder compileBuilder = new ProcessBuilder("gcc", "-o", execFile, sourceFile);
        compileBuilder.directory(new File(workDir));
        Process compileProcess = compileBuilder.start();

        String compileError = readStream(compileProcess.getErrorStream());
        int compileExitCode = compileProcess.waitFor();

        result.put("success", compileExitCode == 0);
        result.put("error", compileExitCode == 0 ? null : compileError);

        return result;
    }

    /**
     * 提取 Java 类名
     */
    private String extractJavaClassName(String code) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("public class ")) {
                int start = line.indexOf("class ") + 6;
                int end = line.indexOf("{");
                if (end == -1) {
                    end = line.indexOf(" ", start);
                }
                if (end == -1) {
                    end = line.length();
                }
                return line.substring(start, end).trim();
            }
        }
        return null;
    }

    /**
     * 读取流内容
     */
    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * 清理目录
     */
    private void cleanupDirectory(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException e) {
                                log.warn("删除文件失败: {}", p, e);
                            }
                        });
            }
        } catch (Exception e) {
            log.warn("清理目录失败: {}", dirPath, e);
        }
    }

    /**
     * 检查命令是否可用
     */
    private boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, "--version");
            Process process = pb.start();
            boolean finished = process.waitFor(3, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
            }
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取进程内存使用（字节）
     * Windows 系统使用 tasklist 命令
     */
    private long getProcessMemory(long pid) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows: 使用 tasklist 命令
                ProcessBuilder pb = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid, "/FO", "CSV", "/NH");
                Process process = pb.start();
                
                String output = readStream(process.getInputStream());
                process.waitFor(1, TimeUnit.SECONDS);
                
                // 解析输出: "java.exe","12345","Console","1","123,456 K"
                if (output != null && !output.isEmpty()) {
                    String[] parts = output.split(",");
                    if (parts.length >= 5) {
                        // 提取内存值，格式如 "123,456 K"
                        String memStr = parts[4].replaceAll("[\"\\s,K]", "");
                        try {
                            return Long.parseLong(memStr) * 1024; // 转换为字节
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    }
                }
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Linux/Mac: 使用 ps 命令
                ProcessBuilder pb = new ProcessBuilder("ps", "-p", String.valueOf(pid), "-o", "rss=");
                Process process = pb.start();
                
                String output = readStream(process.getInputStream());
                process.waitFor(1, TimeUnit.SECONDS);
                
                if (output != null && !output.isEmpty()) {
                    try {
                        // ps 返回的是 KB
                        return Long.parseLong(output.trim()) * 1024;
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常，返回0
        }
        return 0;
    }
}
