package com.zmx;

import com.baidu.disconf.client.annotations.EnableDisconf;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * author: zhongjie
 * date: 2019/6/23
 * time: 20:47
 * description:
 */
@SpringBootApplication
@EnableDisconf
public class DisconfTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(DisconfTestApplication.class, args);
    }
}
