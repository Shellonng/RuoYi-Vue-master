package com.ruoyi.common.utils.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VideoAudioExtractorRenwu3
{
    private static final Logger log = LoggerFactory.getLogger(VideoAudioExtractorRenwu3.class);
    
    // FFmpeg 可执行文件路径（需要预先安装）
    private static final String FFMPEG_PATH = "ffmpeg"; // 如果FFmpeg在系统PATH中，直接使用"ffmpeg"
    
    public static File extractAudioFromVideo(File videoFile) throws Exception
    {
        if (!videoFile.exists())
        {
            throw new IllegalArgumentException("视频文件不存在: " + videoFile.getAbsolutePath());
        }
        
        // 生成输出音频文件路径（与视频同目录）
        String videoPath = videoFile.getAbsolutePath();
        String audioPath = videoPath.substring(0, videoPath.lastIndexOf('.')) + "_audio.wav";
        File audioFile = new File(audioPath);
        
        // 如果音频文件已存在，先删除
        if (audioFile.exists())
        {
            audioFile.delete();
        }
        
        log.info("【任务3-视频】开始从视频提取音频: {} -> {}", videoFile.getName(), audioFile.getName());
        
        try
        {
            // 构建 FFmpeg 命令
            // -i: 输入文件
            // -vn: 不处理视频流
            // -acodec pcm_s16le: 使用 PCM 编码（标准 WAV 格式）
            // -ar 16000: 采样率 16kHz（Whisper 推荐）
            // -ac 1: 单声道
            ProcessBuilder processBuilder = new ProcessBuilder(
                FFMPEG_PATH,
                "-i", videoFile.getAbsolutePath(),
                "-vn",
                "-acodec", "pcm_s16le",
                "-ar", "16000",
                "-ac", "1",
                "-y", // 覆盖已存在的文件
                audioFile.getAbsolutePath()
            );
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // 读取 FFmpeg 输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    output.append(line).append("\n");
                }
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            
            if (exitCode != 0)
            {
                log.error("FFmpeg 执行失败，退出码: {}", exitCode);
                log.error("FFmpeg 输出: {}", output);
                throw new RuntimeException("音频提取失败: FFmpeg 返回错误码 " + exitCode);
            }
            
            if (!audioFile.exists() || audioFile.length() == 0)
            {
                throw new RuntimeException("音频提取失败: 输出文件不存在或为空");
            }
            
            log.info("【任务3-视频】音频提取成功，文件大小: {} MB", audioFile.length() / 1024.0 / 1024.0);
            
            return audioFile;
        }
        catch (Exception e)
        {
            log.error("【任务3-视频】音频提取失败", e);
            if (audioFile.exists())
            {
                audioFile.delete();
            }
            throw e;
        }
    }
    
    public static boolean isFFmpegInstalled()
    {
        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder(FFMPEG_PATH, "-version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    public static String getVideoInfo(File videoFile) throws Exception
    {
        ProcessBuilder processBuilder = new ProcessBuilder(
            FFMPEG_PATH,
            "-i", videoFile.getAbsolutePath()
        );
        
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        
        StringBuilder info = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                info.append(line).append("\n");
            }
        }
        
        process.waitFor();
        return info.toString();
    }
}
