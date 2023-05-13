package com.plexpt.chatgptonlinejava;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatgptOnlineJavaApplication implements ApplicationRunner {

    @Value("${server.port}")
    Integer port;

    public static void main(String[] args) {

        SpringApplication.run(ChatgptOnlineJavaApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        System.out.println("==================================================");
        System.out.println("启动成功");
        System.out.println("本地访问地址：http://localhost:" + port);
        System.out.println("访问地址：http://你的服务器IP:" + port);

        System.out.println("==================================================");


    }
}
