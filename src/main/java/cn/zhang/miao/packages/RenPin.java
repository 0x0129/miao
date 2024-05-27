package cn.zhang.miao.packages;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
public class RenPin {
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

    /**
     * 计算人品值
     *
     * @param qqNumber QQ号码
     * @return 计算出的人品值
     */
    public static int calculate(String qqNumber) {
        String input = LocalDate.now().format(DATE_FORMATTER) + qqNumber;
        MessageDigest md = MESSAGE_DIGEST_THREAD_LOCAL.get();
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        int renPinValue = ((hash[0] & BYTE_MASK) << 8) | (hash[1] & BYTE_MASK);
        return renPinValue % (MAX_REN_PIN_VALUE + 1);
    }
}