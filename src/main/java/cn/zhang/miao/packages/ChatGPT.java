package cn.zhang.miao.packages;

import cn.hutool.core.convert.ConvertException;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class ChatGPT {
    private static final String CHAT_ENDPOINT = "https://free.gpt.ge/v1/chat/completions";
    private static final String API_KEY = "Bearer sk-6QWhi99KUkfyYnwD8cF49aC2F9B04745841fBeB611355dDc";
    private static final String PROMPT = "现在你将模仿一只可爱的猫娘";
    private static final String MODEL = "gpt-3.5-turbo";
    private static final String SYSTEM_ROLE = "system";
    private static final String USER_ROLE = "user";
    private static final String ASSISTANT_ROLE = "assistant";
    private static final String ERROR_MESSAGE = "出现了异常";
    private final Map<String, List<Map<String, String>>> userContextMessages = new ConcurrentHashMap<>();

    /**
     * 进行聊天
     *
     * @param qq   QQ号码
     * @param text 用户输入的文本
     * @return 聊天回复内容
     */
    public String chat(String qq, String text) {
        List<Map<String, String>> contextMessages = userContextMessages.computeIfAbsent(qq, k -> {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage(SYSTEM_ROLE, PROMPT));
            return messages;
        });

        contextMessages.add(createMessage(USER_ROLE, text));
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("model", MODEL);
        paramMap.put("messages", contextMessages);

        try {
            String body = HttpRequest.post(CHAT_ENDPOINT).header("Authorization", API_KEY).header("Content-Type", "application/json").body(JSONUtil.toJsonStr(paramMap)).execute().body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONArray choices = jsonObject.getJSONArray("choices");
            JSONObject result = choices.get(0, JSONObject.class, Boolean.TRUE);
            JSONObject message = result.getJSONObject("message");
            String content = message.getStr("content");
            contextMessages.add(createMessage(ASSISTANT_ROLE, content));
            return content;
        } catch (HttpException | ConvertException e) {
            return ERROR_MESSAGE;
        }
    }

    /**
     * 创建消息
     *
     * @param role    消息角色
     * @param content 消息内容
     * @return 包含角色和内容的消息映射
     */
    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>(2);
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    /**
     * 清除用户上下文消息
     *
     * @param qq QQ号码
     */
    public void clear(String qq) {
        userContextMessages.remove(qq);
    }
}