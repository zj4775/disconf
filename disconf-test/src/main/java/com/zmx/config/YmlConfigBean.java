package com.zmx.config;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.springframework.stereotype.Component;

/**
 * author: zhongjie
 * date: 2019/6/26
 * time: 21:51
 * description:
 */
@Component
@DisconfFile(filename = "app.yml")
public class YmlConfigBean {
    private String name;

    @DisconfFileItem(name = "spring1.name", associateField = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
