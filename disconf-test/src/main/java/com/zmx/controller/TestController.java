package com.zmx.controller;

import com.zmx.config.YmlConfigBean;
import com.zmx.config.YmlConfigBean2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * author: zhongjie
 * date: 2019/6/23
 * time: 20:52
 * description:
 */
@RestController
public class TestController {

    @Autowired
    private YmlConfigBean ymlConfigBean;
    @Autowired
    private YmlConfigBean2 ymlConfigBean2;

    @RequestMapping("/test")
    public String get(){
        return ymlConfigBean.getName()+ymlConfigBean2.getServer2();
    }

}
