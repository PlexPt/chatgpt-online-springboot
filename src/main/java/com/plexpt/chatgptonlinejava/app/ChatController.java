package com.plexpt.chatgptonlinejava.app;


import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.ChatGPTStream;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ChatController {

    final KeyManager keyManager;

    //代理可以为null
    //端口是你的魔法端口
//    static Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1081));
    Proxy proxy = Proxy.NO_PROXY;

    //实际请用数据库管理上下文
    private static Map<String, List<Message>> context = new HashMap<>();


    @PostMapping("chat")
    public ChatCompletionResponse chat(@RequestBody ChatParam param) {

        log.info("正在提问: " + param.getMessage());
        ChatCompletionResponse completion = getText(param);
        String text = completion.getChoices().get(0).getMessage().getContent();

        log.info("问题：" + param.getMessage() + "\n回答：" + text);

        return completion;
    }

    @GetMapping("/chat/sse")
    @CrossOrigin
    public SseEmitter sseEmitter(@RequestBody ChatParam param) {

        ChatGPTStream chatGPTStream = ChatGPTStream.builder()
                .timeout(50)
                .apiKey(param.getApiKey())
                .proxy(proxy)
                .apiHost(param.getApiHost())
                .build()
                .init();

        SseEmitter sseEmitter = new SseEmitter(-1L);

        GPTEventSourceListener listener = new GPTEventSourceListener(sseEmitter);

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(param.getMessages())
                .model(param.model)
                .maxTokens(param.getMax_tokens())
                .stream(true)
                .build();

        chatGPTStream.streamChatCompletion(chatCompletion, listener);

        return sseEmitter;
    }


    private ChatCompletionResponse getText(ChatParam param) {


        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey(param.getApiKey())
                .timeout(50)
                .proxy(proxy)
                .apiHost(param.getApiHost())
                .build()
                .init();

        try {
            ChatCompletion chatCompletion = ChatCompletion.builder()
                    .messages(param.getMessages())
                    .model(param.model)
                    .maxTokens(param.getMax_tokens())
                    .stream(false)
                    .build();

            ChatCompletionResponse completion = chatGPT.chatCompletion(chatCompletion);

            return completion;

        } catch (Exception e) {
            log.error("API调用出错：{}", e);
            throw new RuntimeException("请检查KEY， 网络。请输入你的APIKEY后试用: " + e.getMessage());
        }
    }

}
