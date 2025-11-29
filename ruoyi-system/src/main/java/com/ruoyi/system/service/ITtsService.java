package com.ruoyi.system.service;

/**
 * 文字转语音服务接口
 * 
 * @author ruoyi
 */
public interface ITtsService 
{
    /**
     * 将文本转换为语音
     * 
     * @param text 要转换的文本
     * @return 音频数据（Base64编码）
     */
    String textToSpeech(String text);
    
    /**
     * 将文本转换为语音（带语音选择）
     * 
     * @param text 要转换的文本
     * @param voice 语音类型
     * @return 音频数据（Base64编码）
     */
    String textToSpeech(String text, String voice);
}
