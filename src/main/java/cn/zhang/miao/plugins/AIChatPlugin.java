package cn.zhang.miao.plugins;

import cn.zhang.miao.packages.ChatGPT;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.springframework.stereotype.Component;

@Component
public class AIChatPlugin extends BotPlugin {
    /**
     * 当接收到私聊消息时，会调用此方法进行处理。
     *
     * @param bot   Bot对象，表示机器人实例
     * @param event PrivateMessageEvent对象，表示私聊消息事件
     * @return int类型的返回值，表示消息处理结果，MESSAGE_BLOCK表示已处理消息并阻止其继续传递
     */
    @Override
    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        String sendMsg;
        if ("清除对话".equals(event.getMessage())) {
            ChatGPT.clear(String.valueOf(event.getUserId()));
            sendMsg = MsgUtils.builder().text("清除成功了喵~").build();
            bot.sendPrivateMsg(event.getUserId(), sendMsg, false);
            return MESSAGE_BLOCK;
        }
        sendMsg = MsgUtils.builder().text(ChatGPT.chat(String.valueOf(event.getUserId()), event.getMessage())).build();
        bot.sendPrivateMsg(event.getUserId(), sendMsg, false);
        return MESSAGE_BLOCK;
    }
}