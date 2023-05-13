package com.plexpt.chatgptonlinejava.app;


import com.alibaba.fastjson.JSON;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.ChatGPTStream;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;

import net.dreamlu.mica.http.HttpRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ChatController {

    final KeyManager keyManager;

    static String URL_SUB = "v1/dashboard/billing/subscription";
    static String URL_USEAGE = "v1/dashboard/billing/usage";

    //代理可以为null
    //端口是你的魔法端口
    static Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1081));
    //    Proxy  proxy = Proxy.NO_PROXY;

    //实际请用数据库管理上下文
    private static Map<String, List<Message>> context = new HashMap<>();


    @PostMapping("message")
    public Result chat(@RequestBody ChatParam param,
                       @RequestParam(required = false) String balance) {

        if ("1".equals(balance)) {
            return getBalanceRes(param.getKey());
        }

        log.info("正在提问: " + param.getMessage());
        Message message = getText(param);
        String text = message.getContent();


        log.info("问题：" + param.getMessage() + "\n回答：" + text);

        return Result.ok(text);
    }

    @GetMapping("/chat/sse")
    @CrossOrigin
    public SseEmitter sseEmitter(@RequestParam("key") String key, String id, String prompt) {
        boolean haskey = !StringUtils.isEmpty(key);
        String APIKEY = keyManager.getKey();

        if (haskey) {
            APIKEY = key;
        }
        ChatGPTStream chatGPTStream = ChatGPTStream.builder()
                .timeout(50)
                .apiKey(APIKEY)
                .proxy(proxy)
                .apiHost("https://api.openai.com/")
                .build()
                .init();

        SseEmitter sseEmitter = new SseEmitter(-1L);

        GPTEventSourceListener listener = new GPTEventSourceListener(sseEmitter);
        Message message = Message.of(prompt);

        List<Message> messages = get(id);
        messages.add(message);
        listener.setOnComplate(msg -> {
            add(id, message);
            add(id, Message.ofAssistant(msg));

        });
        chatGPTStream.streamChatCompletion(messages, listener);

        return sseEmitter;
    }

    @SneakyThrows
    public BigDecimal getBalance(String key) {

        try {

            String host = "https://api.openai.com/";
            String res = getSub(key, host);

            String res2 = getUseage(key, host);

            if (StringUtils.contains(res, "The server had an error processing your request.")) {
                log.error("服务器错误，请重试");
                return null;
            }

            if (StringUtils.contains(res, "Incorrect API key provided:")) {
                log.error("KEY错误，重试也没用");

                return null;
            }
            if (StringUtils.contains(res, "This key is associated with a deactivated account")) {
                log.error("KEY被ban了，重试也没用");

                return null;
            }


            SubscriptionData balanceDTO = JSON.parseObject(res, SubscriptionData.class);
            BigDecimal total = balanceDTO.getHard_limit_usd();
            UseageResponse useageResponse = JSON.parseObject(res2, UseageResponse.class);
            BigDecimal used = useageResponse.getTotal_usage()
                    .divide(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal balance = total.subtract(used);

            return balance;
        } catch (Exception e) {
            if (e instanceof SocketException) {
                throw e;
            }
            if (e instanceof SocketTimeoutException) {
                throw e;
            }

            e.printStackTrace();
            log.warn("同步key余额出错：" + e);
        }
        return null;
    }

    private static String getUseage(String key, String host) {
        DateTime start = DateUtil.beginOfMonth(new Date());
        DateTime end = DateUtil.offsetDay(new Date(), 1);
        String res2 = HttpRequest.get(host + URL_USEAGE)
                .setHeader("Authorization", "Bearer " + key)
                .setHeader("Content-type", "application/json")
                .query("start_date", formatDate(start))
                .query("end_date", formatDate(end))
                .proxy(proxy)
                .connectTimeout(Duration.ofSeconds(6))
                .readTimeout(Duration.ofSeconds(6))
                .writeTimeout(Duration.ofSeconds(6))
                .execute()
                .asString();
        return res2;
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    private static String getSub(String key, String host) {
        String res = HttpRequest.get(host + URL_SUB)
                .setHeader("Authorization", "Bearer " + key)
                .setHeader("Content-type", "application/json")
                .proxy(proxy)
                .connectTimeout(Duration.ofSeconds(6))
                .readTimeout(Duration.ofSeconds(6))
                .writeTimeout(Duration.ofSeconds(6))
                .execute()
                .asString();
        return res;
    }

    private Result getBalanceRes(String key) {
        try {


            BigDecimal balance = getBalance(key);

            final Result result = Result.ok(balance.toPlainString());
            result.setStatus("1");
            return result;
        } catch (Exception e) {
            log.warn("同步key余额出错：" + e);
        }
        return null;
    }

    private Message getText(ChatParam param) {
        boolean haskey = param.hasKey();

        String key = keyManager.getKey();

        if (haskey) {
            key = param.getKey();
            System.out.println("key:" + key);
        }

        String prompt = param.buildPrompt();


        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey(key)
                .timeout(50)
                .proxy(proxy)
                .build()
                .init();

        try {
            Message message = Message.of(prompt);

            List<Message> messages = get(param.getId());
            messages.add(message);

            ChatCompletionResponse completion = chatGPT.chatCompletion(messages);
            Message message1 = completion.getChoices().get(0).getMessage();


            add(param.getId(), message);
            add(param.getId(), message1);
            return message1;

        } catch (Exception e) {
            log.error("API调用出错：{}", e);
            throw new RuntimeException("服务器挤爆了，请检查KEY， 网络。请输入你的APIKEY后试用: " + e.getMessage());
        }
    }

    public void save(String id, List<Message> list) {
        List<Message> messages = context.get(id);
        if (messages == null) {
            messages = new ArrayList<>();
            context.put(id, messages);
        }
        messages.addAll(list);
    }

    public List<Message> get(String id) {
        List<Message> messages = context.get(id);
        if (messages == null) {
            messages = new ArrayList<>();
            context.put(id, messages);
        }

        return messages;
    }

    public void add(String id, String msg) {

        Message message = Message.builder().content(msg).build();
        add(id, message);
    }

    public void add(String id, Message message) {
        List<Message> messages = context.get(id);
        if (messages == null) {
            messages = new ArrayList<>();
            context.put(id, messages);
        }
        messages.add(message);
    }
}
