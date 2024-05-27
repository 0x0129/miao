package cn.zhang.miao.plugins;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class HelpPlugin extends BotPlugin {
    private static final String COMMAND = "help";
    private static final String PLUGIN_PACKAGE = "cn.zhang.miao.plugins";
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
        String message = event.getMessage();
        if (COMMAND.equalsIgnoreCase(message)) {
            executorService.submit(() -> {
                String helpMessage = getAllPluginNames();
                bot.sendGroupMsg(event.getGroupId(), helpMessage, false);
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
     * @return 消息处理状态，MESSAGE_BLOCK表示消息已处理，MESSAGE_IGNORE表示消息忽略
     */
    @Override
    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        String message = event.getMessage();
        if (COMMAND.equalsIgnoreCase(message)) {
            executorService.submit(() -> {
                String helpMessage = getAllPluginNames();
                bot.sendPrivateMsg(event.getUserId(), helpMessage, false);
            });
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

    /**
     * 获取所有插件的名称
     *
     * @return 插件名称列表字符串
     */
    private String getAllPluginNames() {
        StringBuilder helpMessage = new StringBuilder("以下是可用的插件列表\n");
        Reflections reflections = new Reflections(PLUGIN_PACKAGE);
        Set<Class<? extends BotPlugin>> pluginClasses = reflections.getSubTypesOf(BotPlugin.class);

        for (Class<? extends BotPlugin> pluginClass : pluginClasses) {
            helpMessage.append(pluginClass.getSimpleName()).append("\n");
        }

        return helpMessage.toString();
    }
}