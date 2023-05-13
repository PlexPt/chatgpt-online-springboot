package com.plexpt.chatgptonlinejava.app;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "keys")
public class KeyConfig {


    List<String> list = new ArrayList<>();


}
