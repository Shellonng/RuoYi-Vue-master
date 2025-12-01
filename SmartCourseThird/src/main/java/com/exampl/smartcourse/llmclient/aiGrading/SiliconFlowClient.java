package com.exampl.smartcourse.llmclient.aiGrading;

import com.exampl.smartcourse.dto.aiGrading.GradingPromptPayload;
import com.exampl.smartcourse.dto.aiGrading.GradingResultResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.exampl.smartcourse.dto.response.BehaviorPerformanceCorrelationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import java.util.Arrays;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SiliconFlowClient {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();
    private static final Logger logger = LoggerFactory.getLogger(SiliconFlowClient.class);

    @Value("${siliconflow.api-key:}")
    private String siliconflowApiKey;

    @Value("${siliconflow.failover.enabled:true}")
    private boolean failoverEnabled;

    public List<String> listModels() {
        return Arrays.asList(
                "deepseek-ai/DeepSeek-V3.2-Exp",
                "Pro/deepseek-ai/DeepSeek-V3.2-Exp",
                "Pro/deepseek-ai/DeepSeek-V3.1-Terminus",
                "deepseek-ai/DeepSeek-V3.1-Terminus",
                "Qwen/Qwen3-Next-80B-A3B-Instruct",
                "Qwen/Qwen3-Next-80B-A3B-Thinking",
                "Qwen/Qwen3-30B-A3B-Thinking-2507",
                "Qwen/Qwen3-235B-A22B-Thinking-2507",
                "Qwen/Qwen3-235B-A22B-Instruct-2507",
                "baidu/ERNIE-4.5-300B-A47B",
                "tencent/Hunyuan-A13B-Instruct",
                "Tongyi-Zhiwen/QwenLong-L1-32B",
                "Qwen/Qwen3-30B-A3B",
                "Qwen/Qwen3-32B",
                "Qwen/Qwen3-14B",
                "Qwen/Qwen3-8B",
                "Qwen/Qwen3-235B-A22B",
                "Qwen/QwQ-32B",
                "Pro/deepseek-ai/DeepSeek-R1",
                "Pro/deepseek-ai/DeepSeek-V3",
                "deepseek-ai/DeepSeek-R1",
                "deepseek-ai/DeepSeek-V3",
                "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B",
                "deepseek-ai/DeepSeek-V2.5"
        );
    }

    public GradingResultResponse grade(GradingPromptPayload payload, String model) throws Exception {
        String apiKey = System.getenv("SILICONFLOW_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) throw new IllegalStateException("SILICONFLOW_API_KEY missing");
        String prompt = buildPrompt(payload);
        logger.info("siliconflow grade start model={} content_len={}", model, payload.getContent() == null ? 0 : payload.getContent().length());
        long t0 = System.nanoTime();
        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        ArrayNode messages = mapper.createArrayNode();
        ObjectNode sys = mapper.createObjectNode();
        sys.put("role", "system");
        sys.put("content", prompt);
        ObjectNode usr = mapper.createObjectNode();
        usr.put("role", "user");
        usr.put("content", payload.getContent() == null ? "" : payload.getContent());
        messages.add(sys);
        messages.add(usr);
        root.set("messages", messages);
        ObjectNode rf = mapper.createObjectNode();
        rf.put("type", "json_object");
        root.set("response_format", rf);
        String body = mapper.writeValueAsString(root);
        String bodySnippet = body.length() > 1000 ? body.substring(0, 1000) : body;
        logger.debug("siliconflow request body {}", bodySnippet);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.siliconflow.cn/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp;
        try {
            resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception networkEx) {
            logger.error("siliconflow http send failed model={} error={}", model, networkEx.getMessage(), networkEx);
            throw new RuntimeException("network error: " + networkEx.getClass().getSimpleName() + ": " + networkEx.getMessage());
        }
        long t1 = System.nanoTime();
        logger.info("siliconflow http status {}", resp.statusCode());
        String respSnippet = resp.body() != null && resp.body().length() > 2000 ? resp.body().substring(0, 2000) : resp.body();
        logger.debug("siliconflow resp body {}", respSnippet);
        if (resp.statusCode() >= 300) {
            String reqId = resp.headers().firstValue("x-request-id").orElse(resp.headers().firstValue("x-trace-id").orElse(null));
            String msg = "http status " + resp.statusCode();
            if (reqId != null) msg += ", requestId=" + reqId;
            if (respSnippet != null && !respSnippet.isEmpty()) msg += ", bodySnippet=" + safeSnippet(respSnippet, 500);
            logger.error("siliconflow non-2xx model={} {}", model, msg);
            throw new RuntimeException(msg);
        }
        JsonNode rootResp = mapper.readTree(resp.body());
        JsonNode choices = rootResp.path("choices");
        String content = null;
        if (choices.isArray() && choices.size() > 0) {
            content = choices.get(0).path("message").path("content").asText(null);
        }
        logger.debug("siliconflow parsed content len {}", content == null ? 0 : content.length());
        GradingResultResponse r;
        try {
            r = parseContentJsonToGrading(content, model);
        } catch (Exception parseEx) {
            String msg = "parse error: " + parseEx.getClass().getSimpleName() + ": " + parseEx.getMessage();
            if (content != null) msg += ", contentSnippet=" + safeSnippet(content, 500);
            logger.error("siliconflow parse failed model={} {}", model, msg, parseEx);
            throw new RuntimeException(msg);
        }
        int totalTokens = rootResp.path("usage").path("total_tokens").asInt(0);
        r.setLlmTokens(totalTokens == 0 ? null : totalTokens);
        int ms = (int) Math.max(0, (t1 - t0) / 1_000_000);
        r.setProcessingTime(ms);
        logger.info("siliconflow grade done tokens={} latency_ms={}", r.getLlmTokens(), r.getProcessingTime());
        return r;
    }

    public static class VideoAiResult {
        private String comment;
        private String suggestionsJson;

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public String getSuggestionsJson() { return suggestionsJson; }
        public void setSuggestionsJson(String suggestionsJson) { this.suggestionsJson = suggestionsJson; }
    }

    public VideoAiResult analyzeVideoBehavior(BehaviorPerformanceCorrelationResponse data, String model) throws Exception {
        String apiKey = resolveApiKeyVideo();
        if (apiKey == null || apiKey.isEmpty()) {
            if (failoverEnabled) {
                return buildVideoAiDefault(data);
            }
            throw new IllegalStateException("SILICONFLOW_API_KEY missing. Please set env SILICONFLOW_API_KEY or property siliconflow.api-key");
        }
        String prompt = buildVideoBehaviorPrompt(data);
        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        ArrayNode messages = mapper.createArrayNode();
        ObjectNode sys = mapper.createObjectNode();
        sys.put("role", "system");
        sys.put("content", prompt);
        ObjectNode usr = mapper.createObjectNode();
        usr.put("role", "user");
        usr.put("content", "");
        messages.add(sys);
        messages.add(usr);
        root.set("messages", messages);
        ObjectNode rf = mapper.createObjectNode();
        rf.put("type", "json_object");
        root.set("response_format", rf);
        String body = mapper.writeValueAsString(root);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.siliconflow.cn/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) {
            String reqId = resp.headers().firstValue("x-request-id").orElse(resp.headers().firstValue("x-trace-id").orElse(null));
            String msg = "http status " + resp.statusCode();
            if (reqId != null) msg += ", requestId=" + reqId;
            throw new RuntimeException(msg);
        }
        JsonNode rootResp = mapper.readTree(resp.body());
        JsonNode choices = rootResp.path("choices");
        String content = null;
        if (choices.isArray() && choices.size() > 0) {
            content = choices.get(0).path("message").path("content").asText(null);
        }
        if (content == null || content.isEmpty()) throw new RuntimeException("empty content");
        JsonNode contentJson = mapper.readTree(content);
        VideoAiResult r = new VideoAiResult();
        r.setComment(contentJson.path("summary").asText(null));
        r.setSuggestionsJson(mapper.writeValueAsString(contentJson));
        return r;
    }

    private String buildVideoBehaviorPrompt(BehaviorPerformanceCorrelationResponse data) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个教育数据分析专家。现在提供课程的视频学习行为与成绩相关性数据，包括完成率、观看时长、观看次数与成绩的皮尔逊相关系数，以及部分学生样本。请基于这些数据生成可读的教学洞察与改进建议。\n");
        sb.append("请严格仅以一个JSON对象输出，字段如下：\n");
        sb.append("- summary: 字符串，中文总体评价，请详细分析学生的学习状态、强项和弱项，字数在300字以上。\n");
        sb.append("- insights: 数组，每项为 {metric: 字符串, strength: 字符串, explanation: 字符串}。\n");
        sb.append("- recommendations: 数组，每项为 {issue: 字符串, action: 字符串, priority: 字符串}。\n");
        sb.append("数据：\n");
        sb.append("课程ID：").append(data.getCourseId()).append("\n");
        sb.append("样本量：").append(data.getSampleSize()).append("\n");
        sb.append("完成率相关性：").append(data.getCompletionRateCorrelation()).append("\n");
        sb.append("观看时长相关性：").append(data.getDurationCorrelation()).append("\n");
        sb.append("观看次数相关性：").append(data.getWatchCountCorrelation()).append("\n");
        if (data.getSamples() != null && !data.getSamples().isEmpty()) {
            sb.append("样本（最多展示20条）：\n");
            int limit = Math.min(20, data.getSamples().size());
            for (int i = 0; i < limit; i++) {
                var s = data.getSamples().get(i);
                sb.append(i + 1).append(". 学生").append(s.getStudentId())
                        .append(" 完成率=").append(s.getAvgCompletionRate())
                        .append(" 时长=").append(s.getTotalWatchDuration())
                        .append(" 次数=").append(s.getTotalWatchCount())
                        .append(" 成绩=").append(s.getAvgScore())
                        .append("\n");
            }
        }
        sb.append("请仅输出包含上述字段的JSON对象。\n");
        return sb.toString();
    }

    private VideoAiResult buildVideoAiDefault(BehaviorPerformanceCorrelationResponse data) {
        ObjectNode root = mapper.createObjectNode();
        String summary = "基于统计数据的规则化分析。样本=" + data.getSampleSize();
        root.put("summary", summary);
        ArrayNode insights = mapper.createArrayNode();
        insights.add(buildInsight("完成率", data.getCompletionRateCorrelation()));
        insights.add(buildInsight("观看时长", data.getDurationCorrelation()));
        insights.add(buildInsight("观看次数", data.getWatchCountCorrelation()));
        root.set("insights", insights);
        ArrayNode recs = mapper.createArrayNode();
        recs.add(buildRec("完成率偏低", "明确观看任务与截止时间，分段检查", "高"));
        recs.add(buildRec("观看时长不足", "插入交互点与小测提升投入", "中"));
        recs.add(buildRec("重复观看较少", "提供难点标记与回看提示", "中"));
        root.set("recommendations", recs);
        VideoAiResult r = new VideoAiResult();
        r.setComment(summary);
        r.setSuggestionsJson(root.toString());
        return r;
    }

    private ObjectNode buildInsight(String metric, double corr) {
        ObjectNode n = mapper.createObjectNode();
        n.put("metric", metric);
        n.put("strength", classifyCorr(corr));
        n.put("explanation", "相关系数=" + BigDecimal.valueOf(corr).setScale(4, java.math.RoundingMode.HALF_UP));
        return n;
    }

    private ObjectNode buildRec(String issue, String action, String priority) {
        ObjectNode n = mapper.createObjectNode();
        n.put("issue", issue);
        n.put("action", action);
        n.put("priority", priority);
        return n;
    }

    private String classifyCorr(double c) {
        double a = Math.abs(c);
        if (a >= 0.6) return "强相关";
        if (a >= 0.3) return "中等相关";
        if (a >= 0.1) return "弱相关";
        return "相关性不显著";
    }

    private String resolveApiKeyVideo() {
        String k = System.getenv("SILICONFLOW_API_KEY");
        if (k == null || k.isEmpty()) k = siliconflowApiKey;
        return k;
    }
    

    private String buildPrompt(GradingPromptPayload payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个教育方面专家，对学生打分及其严格，你大部分只会给予60分以下的分数，除非作业的确优秀，而差的作业甚至只有30-50分。接下来我将给你一份作业的要求等信息，以及这份作业涉及到的知识点，课程资源。" +
                "你需要从从内容相关性、逻辑结构、知识点覆盖度、创新性等维度进行评分，并给出总分。对于知识点覆盖度，你需要使用作业相关信息中的知识点，并分析学生覆盖到的知识点和没有覆盖到的知识点，给出分数" +
                "然后生成个性化的评语和建议。评语和建议需要极其详细，对学生的作业进行全面的评价，不能简单的概括。" +
                "请严格按下述 JSON 模式返回，不要输出除 JSON 以外的任何内容。\n");
        sb.append("JSON 字段要求：\n");
        sb.append("- contentScore, logicScore, knowledgeScore, innovationScore, totalScore: 数字，范围 0-100。\n");
        sb.append("- aiComment: 字符串，简洁中文总体评价。\n");
        sb.append("- improvementSuggestions: 数组，每项为对象 {knowledge_point: 字符串, suggestion: 字符串, resources: 字符串数组}。需要注意这里的知识点和资源只能来自作业相关信息中的知识点和课程资源。\n");
        sb.append("- coveredKnowledgePoints: 字符串数组，已覆盖的知识点名称。该数组的内容只能来自提供的“关联知识点\n");
        sb.append("- missingKnowledgePoints: 字符串数组，未覆盖的知识点名称。该数组的内容只能来自提供的“关联知识点\n");
        sb.append("示例：{\"contentScore\":95,\"logicScore\":92,\"knowledgeScore\":90,\"innovationScore\":88,\"totalScore\":91,\"aiComment\":\"这份作业......\",\"improvementSuggestions\":[{\"knowledge_point\":\"注意力机制\",\"suggestion\":\"缺少与Transformer对比\",\"resources\":[\"paper_123\"]}],\"coveredKnowledgePoints\":[\"RNN\",\"CNN\"],\"missingKnowledgePoints\":[\"注意力机制\"]}\n");
        if (payload.getAssignmentTitle() != null) {
            sb.append("作业标题：").append(payload.getAssignmentTitle()).append("\n");
        }
        if (payload.getCourseId() != null) {
            sb.append("课程ID：").append(payload.getCourseId()).append("\n");
        }
        if (payload.getStartTime() != null) {
            sb.append("开始时间：").append(payload.getStartTime()).append("\n");
        }
        if (payload.getAssignmentDescription() != null) {
            sb.append("作业说明：").append(payload.getAssignmentDescription()).append("\n");
        }
        if (payload.getKnowledgePoints() != null && !payload.getKnowledgePoints().isEmpty()) {
            sb.append("关联知识点：\n");
            int idx = 1;
            for (GradingPromptPayload.Kp kp : payload.getKnowledgePoints()) {
                sb.append(idx++).append(". ");
                if (kp.getTitle() != null) sb.append(kp.getTitle());
                if (kp.getRequired() != null && kp.getRequired()) sb.append("（必修）");
                if (kp.getSequence() != null) sb.append("，顺序：").append(kp.getSequence());
                if (kp.getDescription() != null) sb.append("。说明：").append(kp.getDescription());
                sb.append("\n");
            }
        }
        if (payload.getResources() != null && !payload.getResources().isEmpty()) {
            sb.append("课程资源：\n");
            int ridx = 1;
            for (GradingPromptPayload.Resource r : payload.getResources()) {
                sb.append(ridx++).append(". ");
                if (r.getName() != null) sb.append(r.getName());
                if (r.getDescription() != null) sb.append("：").append(r.getDescription());
                sb.append("\n");
            }
        }
        sb.append("请仅输出符合上述字段的单个 JSON 对象。");
        return sb.toString();
    }

    private GradingResultResponse parseContentJsonToGrading(String contentJson, String model) throws Exception {
        if (contentJson == null || contentJson.isEmpty()) throw new RuntimeException("empty content");
        logger.debug("siliconflow parse content json {}", contentJson.length() > 1000 ? contentJson.substring(0, 1000) : contentJson);
        JsonNode data = mapper.readTree(contentJson);
        GradingResultResponse r = new GradingResultResponse();
        r.setContentScore(asDecimal(data.path("contentScore")));
        r.setLogicScore(asDecimal(data.path("logicScore")));
        r.setKnowledgeScore(asDecimal(data.path("knowledgeScore")));
        r.setInnovationScore(asDecimal(data.path("innovationScore")));
        r.setTotalScore(asDecimal(data.path("totalScore")));
        r.setAiComment(data.path("aiComment").isMissingNode() ? null : data.path("aiComment").asText(null));
        r.setImprovementSuggestions(nodeToString(data.path("improvementSuggestions")));
        r.setCoveredKnowledgePoints(nodeToString(data.path("coveredKnowledgePoints")));
        r.setMissingKnowledgePoints(nodeToString(data.path("missingKnowledgePoints")));
        r.setLlmModel(model);
        r.setTeacherConfirmed(false);
        logger.debug("siliconflow parse done totalScore={} model={}", r.getTotalScore(), model);
        return r;
    }

    private BigDecimal asDecimal(JsonNode n) {
        if (n.isNumber()) return new BigDecimal(n.asText());
        try { return new BigDecimal(n.asText()); } catch (Exception e) { return null; }
    }

    private String nodeToString(JsonNode n) {
        if (n == null || n.isMissingNode()) return null;
        if (n.isArray() || n.isObject()) {
            try { return mapper.writeValueAsString(n); } catch (Exception e) { return null; }
        }
        return n.asText(null);
    }

    private String safeSnippet(String s, int max) {
        if (s == null) return null;
        String trimmed = s.replaceAll("\n", " ").replaceAll("\r", " ");
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}
