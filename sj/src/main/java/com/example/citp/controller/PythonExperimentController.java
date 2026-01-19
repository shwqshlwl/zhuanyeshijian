package com.example.citp.controller;

import com.example.citp.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Python 实验控制器
 * 用于管理和运行 Python 实验文件
 */
@Slf4j
@RestController
@RequestMapping("/python-experiments")
@Tag(name = "Python实验管理", description = "Python实验文件管理和运行")
public class PythonExperimentController {

    // Python 实验文件存放的基础目录
    private static final String PYTHON_BASE_DIR = "python-experiments";

    /**
     * 获取项目的绝对路径
     */
    private String getProjectAbsolutePath(String relativePath) {
        if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
            relativePath = relativePath.substring(1);
        }
        return System.getProperty("user.dir") + File.separator + PYTHON_BASE_DIR + File.separator + relativePath;
    }

    @GetMapping("/list")
    @Operation(summary = "获取Python文件列表")
    public Result<Map<String, Object>> listFiles(
            @RequestParam(value = "path", defaultValue = "") String relativePath,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        try {
            File dir = validatePath(relativePath);
            if (!dir.exists()) {
                // 如果目录不存在，创建它
                dir.mkdirs();
            }
            
            if (!dir.isDirectory()) {
                return Result.error("路径不是目录");
            }

            File[] files = dir.listFiles();
            if (files == null) {
                return Result.success(Collections.emptyMap());
            }

            List<FileNode> all = new ArrayList<>();
            for (File f : files) {
                // 只显示 .py 文件和目录
                if (f.isDirectory() || f.getName().endsWith(".py")) {
                    String fullPath = relativePath.isEmpty() ? f.getName() : relativePath + File.separator + f.getName();
                    all.add(new FileNode(
                            f.getName(),
                            f.isDirectory(),
                            f.length(),
                            fullPath,
                            f.lastModified()
                    ));
                }
            }

            // 排序：目录优先 + 名称升序
            all.sort(Comparator.comparing(FileNode::isDirectory).reversed()
                    .thenComparing(FileNode::getName, String.CASE_INSENSITIVE_ORDER));

            int total = all.size();
            int fromIndex = Math.min((pageNum - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<FileNode> pageList = all.subList(fromIndex, toIndex);

            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", pageList);
            result.put("currentPath", relativePath);
            return Result.success(result);
        } catch (SecurityException e) {
            log.error("非法路径访问", e);
            return Result.error("非法路径访问");
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return Result.error("获取文件列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/content")
    @Operation(summary = "获取Python文件内容")
    public Result<String> getFileContent(@RequestParam("path") String relativePath) {
        try {
            File file = validatePath(relativePath);
            if (!file.exists() || file.isDirectory()) {
                return Result.error("文件不存在");
            }

            if (!file.getName().endsWith(".py")) {
                return Result.error("只能读取 Python 文件");
            }

            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return Result.success(content);
        } catch (Exception e) {
            log.error("读取文件失败", e);
            return Result.error("读取失败: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    @Operation(summary = "保存Python文件")
    public Result<Void> saveFile(@RequestBody Map<String, String> body) {
        try {
            String relativePath = body.get("path");
            String content = body.get("content");

            if (relativePath == null || relativePath.isEmpty()) {
                return Result.error("文件路径不能为空");
            }

            if (!relativePath.endsWith(".py")) {
                return Result.error("只能保存 Python 文件");
            }

            File file = validatePath(relativePath);
            
            // 确保父目录存在
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            return Result.successMsg("保存成功");
        } catch (Exception e) {
            log.error("保存文件失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    @PostMapping("/run")
    @Operation(summary = "运行Python脚本")
    public Result<Map<String, Object>> runPython(@RequestBody Map<String, String> body) {
        String relativePath = body.get("path");
        String inputText = body.getOrDefault("input", "");
        
        try {
            File file = validatePath(relativePath);
            if (!file.exists() || !file.getName().endsWith(".py")) {
                return Result.error("无效的Python文件");
            }

            // 尝试多个 Python 命令
            String[] pythonCommands = {"python", "python3", "py"};
            String pythonCmd = null;
            
            for (String cmd : pythonCommands) {
                if (isPythonAvailable(cmd)) {
                    pythonCmd = cmd;
                    break;
                }
            }

            if (pythonCmd == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("output", "");
                result.put("error", "Python 未安装或未添加到 PATH。\n\n请安装 Python：\n1. 下载：https://www.python.org/downloads/\n2. 安装时勾选 'Add Python to PATH'\n3. 重启 IDE 和后端服务");
                return Result.success(result);
            }

            // 运行 Python 脚本
            ProcessBuilder pb = new ProcessBuilder(pythonCmd, file.getAbsolutePath());
            pb.redirectErrorStream(false); // 分开处理标准输出和错误输出
            Process process = pb.start();

            // 写入输入
            if (inputText != null && !inputText.isEmpty()) {
                try (OutputStream stdin = process.getOutputStream()) {
                    stdin.write(inputText.getBytes(StandardCharsets.UTF_8));
                    stdin.flush();
                }
            }

            // 超时控制（5秒）
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> process.waitFor());
            
            int exitCode;
            String output = "";
            String error = "";
            
            try {
                exitCode = future.get(5, TimeUnit.SECONDS);
                output = readStream(process.getInputStream());
                error = readStream(process.getErrorStream());
            } catch (TimeoutException e) {
                process.destroy();
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("output", "");
                result.put("error", "运行超时：程序超过 5 秒未结束，可能存在死循环");
                return Result.success(result);
            } finally {
                executor.shutdown();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", exitCode == 0);
            result.put("output", output);
            result.put("error", error.isEmpty() ? null : error);
            result.put("exitCode", exitCode);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("运行Python脚本失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("output", "");
            result.put("error", "运行异常: " + e.getMessage());
            return Result.success(result);
        }
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Python文件")
    public Result<Void> deleteFile(@RequestParam("path") String relativePath) {
        try {
            File file = validatePath(relativePath);
            if (!file.exists()) {
                return Result.error("文件不存在");
            }

            if (file.isDirectory()) {
                return Result.error("不能删除目录");
            }

            if (!file.getName().endsWith(".py")) {
                return Result.error("只能删除 Python 文件");
            }

            if (file.delete()) {
                return Result.successMsg("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 验证路径安全性，防止路径遍历攻击
     */
    private File validatePath(String relativePath) throws SecurityException {
        String basePath = System.getProperty("user.dir") + File.separator + PYTHON_BASE_DIR;
        File base = new File(basePath);
        File target = new File(base, relativePath);
        
        try {
            String canonicalBase = base.getCanonicalPath();
            String canonicalTarget = target.getCanonicalPath();
            if (!canonicalTarget.startsWith(canonicalBase)) {
                throw new SecurityException("路径越权访问");
            }
        } catch (IOException e) {
            throw new SecurityException("路径解析失败");
        }
        return target;
    }

    /**
     * 检查 Python 命令是否可用
     */
    private boolean isPythonAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, "--version");
            Process process = pb.start();
            boolean finished = process.waitFor(3, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
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
     * 文件节点类
     */
    public static class FileNode {
        private String name;
        private boolean isDirectory;
        private long size;
        private String fullPath;
        private long lastModified;

        public FileNode(String name, boolean isDirectory, long size, String fullPath, long lastModified) {
            this.name = name;
            this.isDirectory = isDirectory;
            this.size = size;
            this.fullPath = fullPath;
            this.lastModified = lastModified;
        }

        public String getName() { return name; }
        public boolean isDirectory() { return isDirectory; }
        public long getSize() { return size; }
        public String getFullPath() { return fullPath; }
        public long getLastModified() { return lastModified; }
    }
}
