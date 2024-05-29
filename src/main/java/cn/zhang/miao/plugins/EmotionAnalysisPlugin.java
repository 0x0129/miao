package cn.zhang.miao.plugins;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;
import org.xm.tendency.word.HownetWordTendency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EmotionAnalysisPlugin extends BotPlugin {
    private static final String COMMAND_PREFIX = "emotion ";
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
        if (message.startsWith(COMMAND_PREFIX)) {
            executorService.submit(() -> {
                String word = message.substring(COMMAND_PREFIX.length()).trim();
                HownetWordTendency hownet = new HownetWordTendency();
                bot.sendGroupMsg(event.getGroupId(), String.valueOf(hownet.getTendency(word)), false);
            });
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
