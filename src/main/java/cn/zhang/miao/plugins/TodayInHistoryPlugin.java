package cn.zhang.miao.plugins;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TodayInHistoryPlugin extends BotPlugin {
    private static final String COMMAND = "历史上的今天";
    private static final String BASE_URL = "https://baike.baidu.com/cms/home/eventsOnHistory/";
    private static final String[] SENSITIVE_WORDS = {"党", "中国"};
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 处理群组消息事件
     *
     * @param bot   Bot实例
     * @param event 群组消息事件
     * @return 消息处理状态，MESSAGE_BLOCK表示消息已处理，MESSAGE_IGNORE表示消息忽略
     */
    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        return handleMessage(bot, event.getGroupId(), event.getMessage(), true);
    }

    /**
     * 处理私聊消息事件
     *
     * @param bot   Bot实例
     * @param event 私聊消息事件
     * @return 消息处理状态，MESSAGE_BLOCK表示消息已处理，MESSAGE_IGNORE表示消息忽略
     */
    @Override
    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        return handleMessage(bot, event.getUserId(), event.getMessage(), false);
    }

    /**
     * 处理消息，根据是否为群组消息进行不同的处理
     *
     * @param bot     Bot实例
     * @param id      群组ID或用户ID
     * @param message 消息内容
     * @param isGroup 是否为群组消息
     * @return 消息处理状态，MESSAGE_BLOCK表示消息已处理，MESSAGE_IGNORE表示消息忽略
     */
    private int handleMessage(Bot bot, long id, String message, boolean isGroup) {
        if (COMMAND.equalsIgnoreCase(message)) {
            executorService.submit(() -> {
                String historyResult = getTodayInHistory();
                if (isGroup) {
                    bot.sendGroupMsg(id, historyResult, false);
                } else {
                    bot.sendPrivateMsg(id, historyResult, false);
                }
            });
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

    /**
     * 获取历史上的今天信息
     *
     * @return 历史上的今天信息字符串
     */
    private String getTodayInHistory() {
        StringBuilder result = new StringBuilder();
        try {
            String month = getCurrentMonth();
            URL url = new URL(BASE_URL + month + ".json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }
        } catch (IOException e) {
            return "获取历史上的今天信息时出错";
        }
        return parseHistoryResult(result.toString());
    }

    /**
     * 获取当前月份
     *
     * @return 当前月份字符串，格式为MM
     */
    private String getCurrentMonth() {
        return String.format("%02d", LocalDate.now().getMonthValue());
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期字符串，格式为MMdd
     */
    private String getCurrentDate() {
        LocalDate now = LocalDate.now();
        return String.format("%02d%02d", now.getMonthValue(), now.getDayOfMonth());
    }

    /**
     * 获取当前完整日期
     *
     * @return 当前完整日期字符串，格式为yyyy年MM月dd日
     */
    private String getCurrentFullDate() {
        LocalDate now = LocalDate.now();
        return String.format("%d年%02d月%02d日", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

    /**
     * 解析历史上的今天信息
     *
     * @param jsonResult 历史上的今天信息的JSON字符串
     * @return 解析后的信息字符串
     */
    private String parseHistoryResult(String jsonResult) {
        StringBuilder parsedResult = new StringBuilder();
        String currentFullDate = getCurrentFullDate();
        parsedResult.append("今天是").append(currentFullDate).append("\n历史上的今天发生了以下事件\n");
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonResult);
            String currentMonth = getCurrentMonth();
            String currentDate = getCurrentDate();
            if (jsonObject.containsKey(currentMonth)) {
                JSONArray events = jsonObject.getJSONObject(currentMonth).getJSONArray(currentDate);
                if (events != null) {
                    for (int i = 0; i < events.size(); i++) {
                        JSONObject event = events.getJSONObject(i);
                        String title = event.getString("title").replaceAll("<a[^>]*>([^<]*)</a>", "$1");
                        if (!containsSensitiveWords(title)) {
                            parsedResult.append("[").append(event.getString("year")).append("年] ").append(title).append("\n");
                        }
                    }
                } else {
                    parsedResult.append("今天没有历史事件记录");
                }
            } else {
                parsedResult.append("今天没有历史事件记录");
            }
        } catch (JSONException e) {
            parsedResult.append("解析历史上的今天信息时出错");
        }
        return parsedResult.toString();
    }

    /**
     * 检查标题中是否包含敏感词
     *
     * @param title 标题
     * @return 如果包含敏感词，则返回true，否则返回false
     */
    private boolean containsSensitiveWords(String title) {
        for (String word : SENSITIVE_WORDS) {
            if (title.contains(word)) {
                return true;
            }
        }
        return false;
    }
}