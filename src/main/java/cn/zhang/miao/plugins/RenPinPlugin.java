package cn.zhang.miao.plugins;

import cn.zhang.miao.packages.RenPin;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;

@Component
public class RenPinPlugin extends BotPlugin {
    /**
     * 接收群消息时调用该方法。
     *
     * @param bot   Bot对象，表示机器人实例
     * @param event GroupMessageEvent对象，表示群消息事件
     * @return int类型的返回值，表示消息处理结果，MESSAGE_BLOCK表示已处理消息并阻止其继续传递
     */
    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if ("jrrp".equalsIgnoreCase(event.getMessage())) {
            String sendMsg;
            try {
                sendMsg = MsgUtils.builder().at(event.getUserId()).text("今日的人品值为 " + RenPin.calculate(String.valueOf(event.getUserId()))).build();
            } catch (NoSuchAlgorithmException e) {
                sendMsg = MsgUtils.builder().at(event.getUserId()).text("人品值计算出错").build();
            }
            bot.sendGroupMsg(event.getGroupId(), sendMsg, false);
        }
        return MESSAGE_IGNORE;
    }
}