package com.plexpt.chatgptonlinejava.app;

import com.plexpt.chatgptonlinejava.util.CircularBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class KeyManager implements ApplicationRunner {

    private static CircularBlockingQueue<String> keyQueue = new CircularBlockingQueue<>();

    @Autowired
    KeyConfig config;

    public synchronized String getKey() {

        String next = keyQueue.next();
        return next;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            log.info("开始配置KEY队列");

            List<String> list = config.getList();
            int size = list.size();

            log.info("找到" + size + "个配置的KEY");

            for (String key : list) {
                keyQueue.add(key);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
