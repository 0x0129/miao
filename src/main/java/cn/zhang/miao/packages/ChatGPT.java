package cn.zhang.miao.packages;

import cn.hutool.core.convert.ConvertException;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class ChatGPT {
    String chatEndpoint = "https://free.gpt.ge/v1/chat/completions";
    String apiKey = "Bearer sk-6QWhi99KUkfyYnwD8cF49aC2F9B04745841fBeB611355dDc";
    String prompt = "现在你将模仿一只可爱的猫娘";
    Map<String, List<Map<String, String>>> userContextMessages = new HashMap<>();

    /**
     * 与指定QQ号进行上下文聊天
     *
     * @param qq   QQ号
     * @param text 聊天内容
     * @return 聊天回复内容
     */
    public String chat(String qq, String text) {
        List<Map<String, String>> contextMessages = userContextMessages.getOrDefault(qq, new ArrayList<>());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("model", "gpt-3.5-turbo");
        if (contextMessages.isEmpty()) {
            contextMessages.add(new HashMap<String, String>() {{
                put("role", "system");
                put("content", prompt);
            }});
        }
        contextMessages.add(new HashMap<>() {{
            put("role", "user");
            put("content", text);
        }});
        paramMap.put("messages", contextMessages);

        JSONObject message;
        try {
            String body = HttpRequest.post(chatEndpoint).header("Authorization", apiKey).header("Content-Type", "application/json").body(JSONUtil.toJsonStr(paramMap)).execute().body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONArray choices = jsonObject.getJSONArray("choices");
            JSONObject result = choices.get(0, JSONObject.class, Boolean.TRUE);
            message = result.getJSONObject("message");
            JSONObject finalMessage = message;
            contextMessages.add(new HashMap<String, String>() {{
                put("role", "assistant");
                put("content", finalMessage.getStr("content"));
            }});
            userContextMessages.put(qq, contextMessages);
        } catch (HttpException | ConvertException e) {
            return "出现了异常";
        }
        return message.getStr("content");
    }

    /**
     * 清除指定QQ号的上下文消息记录
     *
     * @param qq QQ号
     */
    public void clear(String qq) {
        userContextMessages.remove(qq);
    }

    /**
     * 该函数是程序的入口点，用于与用户进行对话。
     *
     * @param args 字符串数组类型的命令行参数
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input;
        String qq = "10001";
        System.out.println("开始对话吧！（输入'退出'结束对话）");
        while (true) {
            System.out.print("[用户] ");
            input = scanner.nextLine();
            if ("退出".equals(input)) {
                System.out.println("对话结束。");
                break;
            }
            if ("清空".equals(input)) {
                clear(qq);
                System.out.println("对话记录已清空。");
                continue;
            }
            String response = chat(qq, input);
            System.out.println("[AI] " + response);
        }
        scanner.close();
    }
}