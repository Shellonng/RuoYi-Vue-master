package com.ruoyi.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Whisper服务启动器
 * 在Spring Boot应用启动完成后自动启动Whisper Python服务
 * 
 * @author ruoyi
 */
@Component
public class WhisperServiceStarter implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(WhisperServiceStarter.class);
    
    private Process whisperProcess;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        startWhisperService();
        
        // 添加JVM关闭钩子,在应用关闭时停止Whisper服务
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopWhisperService();
        }));
    }
    
    /**
     * 启动Whisper服务
     */
    private void startWhisperService() {
        try {
            log.info("正在启动Whisper服务...");
            
            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            File whisperServiceDir = new File(projectRoot, "whisper-service");
            
            if (!whisperServiceDir.exists()) {
                log.warn("Whisper服务目录不存在: {}", whisperServiceDir.getAbsolutePath());
                return;
            }
            
            // 检查虚拟环境是否存在
            File pythonExe = new File(whisperServiceDir, "venv/Scripts/python.exe");
            if (!pythonExe.exists()) {
                log.warn("虚拟环境Python不存在: {}", pythonExe.getAbsolutePath());
                return;
            }
            
            // 构建启动命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                pythonExe.getAbsolutePath(),
                "whisper_server.py"
            );
            
            processBuilder.directory(whisperServiceDir);
            processBuilder.redirectErrorStream(true);
            
            // 启动进程
            whisperProcess = processBuilder.start();
            
            // 创建线程读取输出
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(whisperProcess.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[Whisper] {}", line);
                        // 检测服务是否启动成功
                        if (line.contains("Uvicorn running on")) {
                            log.info("✓ Whisper服务启动成功!");
                        }
                    }
                } catch (Exception e) {
                    log.error("读取Whisper服务输出失败", e);
                }
            }, "WhisperServiceLogger").start();
            
            log.info("Whisper服务进程已启动");
            
        } catch (Exception e) {
            log.error("启动Whisper服务失败", e);
        }
    }
    
    /**
     * 停止Whisper服务
     */
    private void stopWhisperService() {
        if (whisperProcess != null && whisperProcess.isAlive()) {
            log.info("正在停止Whisper服务...");
            whisperProcess.destroy();
            try {
                whisperProcess.waitFor();
                log.info("✓ Whisper服务已停止");
            } catch (InterruptedException e) {
                log.error("等待Whisper服务停止时被中断", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
