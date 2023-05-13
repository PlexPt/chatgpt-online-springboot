package com.plexpt.chatgptonlinejava.app;


import lombok.Data;

@Data
public class Result {

    String status = "success";

    String msg;
    String message;
    String raw_message;

    public static Result ok(String text) {
        Result result = new Result();
        result.setRaw_message(text);
        result.buildMsg();

        return result;
    }

    public static Result error(String message) {
        Result result = new Result();
        result.setMessage(message);
        result.setRaw_message(message);
        return result;

    }

    public void buildMsg() {
        message = "<p>" + raw_message + "</p>\n";
    }
}
