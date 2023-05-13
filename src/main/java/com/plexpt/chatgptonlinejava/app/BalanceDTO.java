package com.plexpt.chatgptonlinejava.app;

import lombok.Data;

@Data
public class BalanceDTO {


    double total_available; //剩余

    String total_used; //已使用


    String total_granted;//全部

}
