package com.plexpt.chatgptonlinejava.app;

import com.plexpt.chatgpt.entity.chat.Message;
import lombok.Data;
import lombok.NonNull;
import org.springframework.util.StringUtils;

import java.util.List;

@Data
public class ChatParam {


    private String apiKey;

    String model;
    private @NonNull List<Message> messages;

    Integer max_tokens;
    Double temperature;
    Boolean stream;
    String message;
    List<List<String>> context;
    String key;
    String id;
    private String apiHost;

    public boolean hasKey() {
        return !StringUtils.isEmpty(key);
    }


}


