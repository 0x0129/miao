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
    private final Map<String, List<Map<String, String>>> userContextMessages = new HashMap<>();
    public String chat(String qq, String text) {
        List<Map<String, String>> contextMessages = userContextMessages.computeIfAbsent(qq, k -> new ArrayList<>());
        if (contextMessages.isEmpty()) {
            contextMessages.add(createMessage(SYSTEM_ROLE, PROMPT));
        }
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
            contextMessages.add(createMessage(ASSISTANT_ROLE, message.getStr("content")));
            return message.getStr("content");
        } catch (HttpException | ConvertException e) {
            return ERROR_MESSAGE;
        }
    }

    private Map<String, String> createMessage(String role, String content) {
        return new HashMap<>() {{
            put("role", role);
            put("content", content);
        }};
    }
    public void clear(String qq) {
        userContextMessages.remove(qq);
    }
}