package com.zmx.config;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.springframework.stereotype.Component;

/**
 * author: zhongjie
 * date: 2019/6/23
 * time: 20:50
 * description:
 */
@Component
@DisconfFile(filename = "disconf-test.properties")
public class ConfigBean {

    private String name;

    @DisconfFileItem(name = "name", associateField = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DisconfFileItem(name = "age", associateField = "age")
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    private String age;
}
