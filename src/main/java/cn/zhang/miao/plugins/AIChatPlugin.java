package cn.zhang.miao.plugins;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AIChatPlugin extends BotPlugin {
    private static final String CLEAR_COMMAND = "清除对话";
    private static final String CLEAR_SUCCESS_MESSAGE = "清除成功了喵~";
    private static final String CHAT_ENDPOINT = "https://free.gpt.ge/v1/chat/completions";
    private static final String API_KEY = "Bearer sk-6QWhi99KUkfyYnwD8cF49aC2F9B04745841fBeB611355dDc";
    private static final String PROMPT = "现在请你模仿一只可爱的猫娘";
    private static final String MODEL = "gpt-3.5-turbo";
    private static final String SYSTEM_ROLE = "system";
    private static final String USER_ROLE = "user";
    private static final String ASSISTANT_ROLE = "assistant";
    private static final String ERROR_MESSAGE = "出错了喵~";
    private final Map<String, List<Map<String, String>>> userContextMessages = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 处理群组消息事件
     *
     * @param bot   Bot实例
     * @param event 群组消息事件
     * @return 消息处理状态
     */
    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        String atMessage = "[CQ:at,qq=" + bot.getSelfId() + "]";
        if (event.getMessage().contains(atMessage)) {
            executorService.submit(() -> {
                String message = event.getMessage().substring(atMessage.length() + 1);
                String sendMsg;
                if (CLEAR_COMMAND.equals(message)) {
                    clearUserContext(String.valueOf(event.getUserId()));
                    sendMsg = buildGroupMessage(event.getUserId(), CLEAR_SUCCESS_MESSAGE);
                } else {
                    sendMsg = buildGroupMessage(event.getUserId(), chatWithGPT(String.valueOf(event.getUserId()), message));
                }
                bot.sendGroupMsg(event.getGroupId(), sendMsg, false);
            });
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

    /**
     * 处理私聊消息事件
     *
     * @param bot   Bot实例
     * @param event 私聊消息事件
     * @return 消息处理状态
     */
    @Override
    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        String message = event.getMessage();
        executorService.submit(() -> {
            String sendMsg;
            if (CLEAR_COMMAND.equals(message)) {
                clearUserContext(String.valueOf(event.getUserId()));
                sendMsg = MsgUtils.builder().text(CLEAR_SUCCESS_MESSAGE).build();
            } else {
                sendMsg = MsgUtils.builder().text(chatWithGPT(String.valueOf(event.getUserId()), message)).build();
            }
            bot.sendPrivateMsg(event.getUserId(), sendMsg, false);
        });
        return MESSAGE_BLOCK;
    }

    /**
     * 构建群组消息
     *
     * @param userId 用户ID
     * @param text   消息内容
     * @return 构建后的群组消息
     */
    private String buildGroupMessage(long userId, String text) {
        return MsgUtils.builder().at(userId).text(text).build();
    }

    /**
     * 清除用户上下文
     *
     * @param qq 用户QQ号
     */
    private void clearUserContext(String qq) {
        userContextMessages.remove(qq);
    }

    /**
     * 与GPT进行对话
     *
     * @param qq   用户QQ号
     * @param text 用户发送的消息
     * @return GPT回复的消息
     */
    private String chatWithGPT(String qq, String text) {
        List<Map<String, String>> contextMessages = userContextMessages.computeIfAbsent(qq, k -> {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage(SYSTEM_ROLE, PROMPT));
            return messages;
        });

        contextMessages.add(createMessage(USER_ROLE, text));
        Map<String, Object> paramMap = Map.of("model", MODEL, "messages", contextMessages);

        try {
            String body = HttpRequest.post(CHAT_ENDPOINT).header("Authorization", API_KEY).header("Content-Type", "application/json").body(JSONUtil.toJsonStr(paramMap)).execute().body();
            JSONObject jsonObject = JSONObject.parseObject(body);
            JSONArray choices = jsonObject.getJSONArray("choices");
            JSONObject result = choices.getJSONObject(0);
            JSONObject message = result.getJSONObject("message");
            String content = message.getString("content");
            contextMessages.add(createMessage(ASSISTANT_ROLE, content));
            return content;
        } catch (Exception e) {
            return ERROR_MESSAGE;
        }
    }

    /**
     * 创建消息
     *
     * @param role    角色（system, user, assistant）
     * @param content 消息内容
     * @return 创建的消息
     */
    private Map<String, String> createMessage(String role, String content) {
        return Map.of("role", role, "content", content);
    }
}