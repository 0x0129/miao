package cn.zhang.miao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MiaoApplication {

    /**
     * 应用程序的入口点
     *
     * @param args 传递给main方法的命令行参数
     * @return 无返回值。这个方法启动Spring Boot应用程序。
     */
    public static void main(String[] args) {
        SpringApplication.run(MiaoApplication.class, args);
    }
}