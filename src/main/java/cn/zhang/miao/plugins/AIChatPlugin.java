package cn.zhang.miao.plugins;

import cn.zhang.miao.packages.ChatGPT;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Component
public class AIChatPlugin extends BotPlugin {
    private static final String CLEAR_COMMAND = "清除对话";
    private static final String CLEAR_SUCCESS_MESSAGE = "清除成功了喵~";
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("\\[[^\\]]+\\]\\s*(.+)");

    /**
     * 处理群消息事件
     *
     * @param bot   机器人实例
     * @param event 群消息事件
     * @return 返回 MESSAGE_BLOCK 表示不执行下一个插件，返回 MESSAGE_IGNORE 表示执行下一个插件
     */
    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        String message = extractMessage(event.getMessage());
        String sendMsg;
        String atMessage = "[CQ:at,qq=" + bot.getSelfId() + "]";
        if (event.getMessage().contains(atMessage)) {
            if (CLEAR_COMMAND.equals(message)) {
                ChatGPT.clear(String.valueOf(event.getUserId()));
                sendMsg = buildGroupMessage(event.getUserId(), CLEAR_SUCCESS_MESSAGE);
            } else {
                sendMsg = buildGroupMessage(event.getUserId(), ChatGPT.chat(String.valueOf(event.getUserId()), message));
            }
            bot.sendGroupMsg(event.getGroupId(), sendMsg, false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

    /**
     * 处理私聊消息事件
     *
     * @param bot   机器人实例
     * @param event 私聊消息事件
     * @return 返回 MESSAGE_BLOCK 表示不执行下一个插件
     */
    @Override
    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        String message = event.getMessage();
        String sendMsg;
        if (CLEAR_COMMAND.equals(message)) {
            ChatGPT.clear(String.valueOf(event.getUserId()));
            sendMsg = buildGroupMessage(event.getUserId(), CLEAR_SUCCESS_MESSAGE);
        } else {
            sendMsg = MsgUtils.builder().text(ChatGPT.chat(String.valueOf(event.getUserId()), message)).build();
        }
        bot.sendPrivateMsg(event.getUserId(), sendMsg, false);
        return MESSAGE_BLOCK;
    }

    /**
     * 从消息中提取有效内容
     *
     * @param message 原始消息内容
     * @return 提取后的消息内容
     */
    private String extractMessage(String message) {
        Matcher msgMatcher = MESSAGE_PATTERN.matcher(message);
        if (msgMatcher.find()) {
            return msgMatcher.group(1);
        }
        return message;
    }

    /**
     * 构建群消息
     *
     * @param userId 用户ID
     * @param text   消息文本
     * @return 构建后的消息内容
     */
    private String buildGroupMessage(long userId, String text) {
        return MsgUtils.builder().at(userId).text(text).build();
    }
}