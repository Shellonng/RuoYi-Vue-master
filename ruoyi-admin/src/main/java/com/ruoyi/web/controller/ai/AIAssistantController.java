package com.ruoyi.web.controller.ai;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI助手Controller - 教学助手"小智"
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/assistant")
public class AIAssistantController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(AIAssistantController.class);

    @Autowired
    private AIService aiService;

    /**
     * AI聊天接口
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, Object> requestData)
    {
        try
        {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = (List<Map<String, String>>) requestData.get("messages");
            
            if (messages == null || messages.isEmpty())
            {
                return AjaxResult.error("消息列表不能为空");
            }

            // 获取最后一条用户消息
            String userMessage = "";
            for (int i = messages.size() - 1; i >= 0; i--)
            {
                Map<String, String> msg = messages.get(i);
                if ("user".equals(msg.get("role")))
                {
                    userMessage = msg.get("content");
                    break;
                }
            }

            if (userMessage.isEmpty())
            {
                return AjaxResult.error("未找到用户消息");
            }

            log.info("AI助手收到消息：{}", userMessage);

            // 调用AI服务生成回复
            String aiReply = aiService.chatWithTeachingAssistant(userMessage, messages);

            log.info("AI助手回复：{}", aiReply);

            return AjaxResult.success("请求成功", aiReply);
        }
        catch (Exception e)
        {
            log.error("AI助手处理失败", e);
            return AjaxResult.error("AI助手暂时无法响应，请稍后再试：" + e.getMessage());
        }
    }
}
