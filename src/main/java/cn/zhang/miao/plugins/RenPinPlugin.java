package cn.zhang.miao.plugins;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class RenPinPlugin extends BotPlugin {
    private static final String COMMAND = "jrrp";
    private static final String SUCCESS_MESSAGE_FORMAT = "今日的人品值为 %d";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String HASH_ALGORITHM = "MD5";
    private static final int MAX_REN_PIN_VALUE = 100;
    private static final int BYTE_MASK = 0xFF;
    private static final ThreadLocal<MessageDigest> MESSAGE_DIGEST_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    });
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
                int renPinValue = calculateRenPin(String.valueOf(event.getUserId()));
                String sendMsg = buildGroupMessage(event.getUserId(), String.format(SUCCESS_MESSAGE_FORMAT, renPinValue));
                bot.sendGroupMsg(event.getGroupId(), sendMsg, false);
            });
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

    /**
     * 计算人品值
     *
     * @param qqNumber 用户QQ号
     * @return 计算出的人品值
     */
    private int calculateRenPin(String qqNumber) {
        String input = LocalDate.now().format(DATE_FORMATTER) + qqNumber;
        MessageDigest md = MESSAGE_DIGEST_THREAD_LOCAL.get();
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        int renPinValue = ((hash[0] & BYTE_MASK) << 8) | (hash[1] & BYTE_MASK);
        return renPinValue % (MAX_REN_PIN_VALUE + 1);
    }

    /**
     * 构建群组消息
     *
     * @param userId 用户ID
     * @param text   消息内容
     * @return 构建后的群组消息
     */
    private String buildGroupMessage(long userId, String text) {
        return MsgUtils.builder().at(userId).text(text).build();
    }
}