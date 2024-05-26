package cn.zhang.miao.packages;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RenPin {
    /**
     * 计算指定QQ号码的人品值
     *
     * @param qqNumber QQ号码
     * @return 人品值，取值范围[0, 100]
     * @throws NoSuchAlgorithmException 如果MD5算法不存在，则抛出此异常
     */
    public static int calculate(String qqNumber) throws NoSuchAlgorithmException {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateString = today.format(formatter);
        String input = dateString + qqNumber;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        int renPinValue = ((hash[0] & 0xFF) << 8) | (hash[1] & 0xFF);
        renPinValue = renPinValue % 101;
        return renPinValue;
    }

    /**
     * 主函数，程序的入口点
     *
     * @param args 命令行参数数组
     */
    public static void main(String[] args) {
        String qqNumber = "10001";
        try {
            int renPin = calculate(qqNumber);
            System.out.println(renPin);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
