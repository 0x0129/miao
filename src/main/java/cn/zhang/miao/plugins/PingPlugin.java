package cn.zhang.miao.plugins;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PingPlugin extends BotPlugin {
    private static final String COMMAND_PREFIX = "ping ";
    private static final Charset CHARSET = getCharset();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 获取字符集，根据操作系统选择适当的字符集
     *
     * @return 字符集，Windows系统返回GBK，其他系统返回UTF-8
     */
    private static Charset getCharset() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? Charset.forName("GBK") : StandardCharsets.UTF_8;
    }

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
                String target = message.substring(COMMAND_PREFIX.length()).trim();
                String pingResult = executePing(target, event.getUserId(), true);
                bot.sendGroupMsg(event.getGroupId(), pingResult, false);
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
        if (message.startsWith(COMMAND_PREFIX)) {
            executorService.submit(() -> {
                String target = message.substring(COMMAND_PREFIX.length()).trim();
                String pingResult = executePing(target, event.getUserId(), false);
                bot.sendPrivateMsg(event.getUserId(), pingResult, false);
            });
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

    /**
     * 执行ping命令
     *
     * @param target  目标IP地址或域名
     * @param userId  用户ID
     * @param isGroup 是否为群组消息
     * @return ping命令的执行结果
     */
    private String executePing(String target, long userId, boolean isGroup) {
        StringBuilder result = new StringBuilder();
        if (isGroup) {
            result.append(MsgUtils.builder().at(userId).text("\n").build());
        }
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ping", target);
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), CHARSET))) {
                String line;
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    result.append(line).append("\n");
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            result.append("执行 ping 命令时出错");
        }
        return result.toString();
    }
}