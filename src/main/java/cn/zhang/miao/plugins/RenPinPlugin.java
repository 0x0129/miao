package cn.zhang.miao.plugins;

import cn.zhang.miao.packages.RenPin;
import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;

@Shiro
@Component
public class RenPinPlugin {
    private static final String COMMAND = "jrrp";
    private static final String ERROR_MESSAGE = "人品值计算出错";
    private static final String SUCCESS_MESSAGE_FORMAT = "今日的人品值为 %d";

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = COMMAND)
    public void jrrp(Bot bot, GroupMessageEvent event, Matcher matcher) {
        String sendMsg;
        try {
            int renPinValue = RenPin.calculate(String.valueOf(event.getUserId()));
            sendMsg = buildMessage(event.getUserId(), String.format(SUCCESS_MESSAGE_FORMAT, renPinValue));
        } catch (NoSuchAlgorithmException e) {
            sendMsg = buildMessage(event.getUserId(), ERROR_MESSAGE);
        }
        bot.sendGroupMsg(event.getGroupId(), sendMsg, false);
    }

    private String buildMessage(long userId, String text) {
        return MsgUtils.builder().at(userId).text(text).build();
    }
}