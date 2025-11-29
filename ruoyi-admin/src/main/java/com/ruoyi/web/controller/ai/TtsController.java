package com.ruoyi.web.controller.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.ITtsService;

/**
 * 文字转语音 Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/tts")
public class TtsController extends BaseController
{
    @Autowired
    private ITtsService ttsService;

    /**
     * 文字转语音
     */
    @PostMapping("/convert")
    public AjaxResult textToSpeech(@RequestBody TtsRequest request)
    {
        String audioBase64 = ttsService.textToSpeech(request.getText(), request.getVoice());
        return AjaxResult.success(audioBase64);
    }
    
    /**
     * TTS请求对象
     */
    public static class TtsRequest 
    {
        private String text;
        private String voice;
        
        public String getText() 
        {
            return text;
        }
        
        public void setText(String text) 
        {
            this.text = text;
        }
        
        public String getVoice() 
        {
            return voice;
        }
        
        public void setVoice(String voice) 
        {
            this.voice = voice;
        }
    }
}
