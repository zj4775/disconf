package com.zmx.config;

import com.baidu.disconf.client.common.annotations.DisconfYmlFile;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * author: zhongjie
 * date: 2019/6/26
 * time: 21:51
 * description:
 */
@DisconfYmlFile(filename = "app.yml")
@Data
@ToString
public class YmlConfigBean2 {

    private List<Server2> server2;

    @Data
    @ToString
    public static class Server2{
        private Integer port;

        private String host;

    }




}
