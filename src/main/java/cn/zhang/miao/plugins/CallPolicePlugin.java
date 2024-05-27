package cn.zhang.miao.plugins;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CallPolicePlugin extends BotPlugin {
    private static final String COMMAND = "call police";
    private static final String[] EMOJIS = {"👮", "👮🏻", "🚔", "🚨", "🚓"};
    private static final Random RANDOM = new Random();
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
                String sendMsg = generateRandomEmojis();
                bot.sendGroupMsg(event.getGroupId(), sendMsg, false);
            });
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

    /**
     * 生成随机的警察表情符号
     *
     * @return 随机生成的警察表情符号字符串
     */
    private String generateRandomEmojis() {
        int count = 10 + RANDOM.nextInt(11);
        StringBuilder emojiBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            emojiBuilder.append(EMOJIS[RANDOM.nextInt(EMOJIS.length)]);
        }
        return emojiBuilder.toString();
    }
}