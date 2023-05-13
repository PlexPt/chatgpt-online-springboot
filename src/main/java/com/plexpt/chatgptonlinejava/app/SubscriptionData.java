package com.plexpt.chatgptonlinejava.app;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SubscriptionData {

    /**
     * 金额：美元
     */
    @JsonProperty("hard_limit_usd")
    private BigDecimal hard_limit_usd;
}
