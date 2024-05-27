package cn.zhang.miao.plugins;
import cn.zhang.miao.packages.RenPin;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;
@Component
public class RenPinPlugin extends BotPlugin {
    private static final String COMMAND = "jrrp";
    private static final String SUCCESS_MESSAGE_FORMAT = "今日的人品值为 %d";

    /**
     * 处理群消息事件
     *
     * @param bot   机器人实例
     * @param event 群消息事件
     * @return 返回 MESSAGE_BLOCK 表示不执行下一个插件，返回 MESSAGE_IGNORE 表示执行下一个插件
     */
    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        String message = event.getMessage();
        if (COMMAND.equalsIgnoreCase(message)) {
            int renPinValue = RenPin.calculate(String.valueOf(event.getUserId()));
            String sendMsg = buildGroupMessage(event.getUserId(), String.format(SUCCESS_MESSAGE_FORMAT, renPinValue));
            bot.sendGroupMsg(event.getGroupId(), sendMsg, false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
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