package com.plexpt.chatgptonlinejava.app;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import lombok.Data;

@Data
public class ChatParam {

    List<String> defaultPrompt = Arrays.asList("我问你个问题，你告诉我答案。你是谁？", "我是ChatGPT，一个由OpenAI" +
            "训练的大型语言模型。我设计用于回答各种问题和提供各种帮助。您有什么需要我帮忙的吗？");
    public static String Question = "Q:\n";
    public static String AI = "A:\n";

    String message;
    List<List<String>> context;
    String key;
    String id;

    public boolean hasKey() {
        return !StringUtils.isEmpty(key);
    }

    public String buildPrompt() {
        if (CollectionUtils.isEmpty(context)) {
            context = Arrays.asList(defaultPrompt);
        }
        String result = "";

        for (List<String> pro : context) {
            result = result + Question + pro.get(0) + "\n";
            result = result + AI + pro.get(1) + "\n";
        }

//        String next = Question;
//        for (List<String> pro : context) {
//            if (next == Question) {
//                next = AI;
//            } else {
//                next = Question;
//            }
//            result = result + next + pro + "\n";
//        }
        result = result + Question + message + "\n" + AI;

        return result;
    }

}


