package cn.zhang.miao.plugins;

import com.alibaba.fastjson2.JSONObject;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class IPInfoPlugin extends BotPlugin {
    private static final String COMMAND_PREFIX = "ipinfo ";
    private static final String API_URL = "http://ip-api.com/json/";
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
        return handleMessage(bot, event.getMessage(), event.getUserId(), event.getGroupId(), true);
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
        return handleMessage(bot, event.getMessage(), event.getUserId(), null, false);
    }

    /**
     * 处理消息，根据是否为群组消息进行不同的处理
     *
     * @param bot     Bot实例
     * @param message 消息内容
     * @param userId  用户ID
     * @param groupId 群组ID（如果是群组消息）
     * @param isGroup 是否为群组消息
     * @return 消息处理状态，MESSAGE_BLOCK表示消息已处理，MESSAGE_IGNORE表示消息忽略
     */
    private int handleMessage(Bot bot, String message, long userId, Long groupId, boolean isGroup) {
        if (message.startsWith(COMMAND_PREFIX)) {
            executorService.submit(() -> {
                String target = message.substring(COMMAND_PREFIX.length()).trim();
                String ipInfoResult = getIPInfo(target, userId, isGroup);
                if (isGroup) {
                    bot.sendGroupMsg(groupId, ipInfoResult, false);
                } else {
                    bot.sendPrivateMsg(userId, ipInfoResult, false);
                }
            });
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

    /**
     * 获取IP信息
     *
     * @param target  目标IP地址或域名
     * @param userId  用户ID
     * @param isGroup 是否为群组消息
     * @return IP信息字符串
     */
    private String getIPInfo(String target, long userId, boolean isGroup) {
        StringBuilder result = new StringBuilder();
        if (isGroup) {
            result.append(MsgUtils.builder().at(userId).text("\n").build());
        }

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(API_URL + target, String.class);
            JSONObject ipInfo = JSONObject.parseObject(response.getBody());
            if (ipInfo != null) {
                result.append("[IP地址] ").append(ipInfo.getString("query")).append("\n").append("[ISP] ").append(ipInfo.getString("isp")).append("\n").append("[组织] ").append(ipInfo.getString("org")).append("\n").append("[ASN] ").append(ipInfo.getString("as")).append("\n").append("[位置] ").append(ipInfo.getString("city")).append(", ").append(ipInfo.getString("country")).append("\n");
            } else {
                result.append("未能获取到 IP 信息");
            }
        } catch (Exception e) {
            result.append("获取 IP 信息时出错");
        }
        return result.toString();
    }
}