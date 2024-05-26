package cn.zhang.miao.plugins;

import cn.zhang.miao.packages.ChatGPT;
import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Shiro
@Component
public class AIChatPlugin {
    private static final String CLEAR_COMMAND = "清除对话";
    private static final String CLEAR_SUCCESS_MESSAGE = "清除成功了喵~";
    private static final String MESSAGE_REGEX = "\\[[^\\]]+\\]\\s*(.+)";

    @GroupMessageHandler
    @MessageHandlerFilter(at = AtEnum.NEED)
    public void aiGroupChat(Bot bot, GroupMessageEvent event, Matcher matcher) {
        String message = extractMessage(event.getMessage());
        String sendMsg;
        if (CLEAR_COMMAND.equals(message)) {
            ChatGPT.clear(String.valueOf(event.getUserId()));
            sendMsg = buildMessage(event.getUserId(), CLEAR_SUCCESS_MESSAGE);
        } else {
            sendMsg = buildMessage(event.getUserId(), ChatGPT.chat(String.valueOf(event.getUserId()), message));
        }
        bot.sendGroupMsg(event.getGroupId(), sendMsg, false);
    }

    @PrivateMessageHandler
    public void aiPrivateChat(Bot bot, PrivateMessageEvent event) {
        String message = event.getMessage();
        String sendMsg;
        if (CLEAR_COMMAND.equals(message)) {
            ChatGPT.clear(String.valueOf(event.getUserId()));
            sendMsg = buildMessage(event.getUserId(), CLEAR_SUCCESS_MESSAGE);
        } else {
            sendMsg = MsgUtils.builder().text(ChatGPT.chat(String.valueOf(event.getUserId()), message)).build();
        }
        bot.sendPrivateMsg(event.getUserId(), sendMsg, false);
    }

    private String extractMessage(String message) {
        Pattern pattern = Pattern.compile(MESSAGE_REGEX);
        Matcher msgMatcher = pattern.matcher(message);
        if (msgMatcher.find()) {
            return msgMatcher.group(1);
        }
        return message;
    }

    private String buildMessage(long userId, String text) {
        return MsgUtils.builder().at(userId).text(text).build();
    }
}