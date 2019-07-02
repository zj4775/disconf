package com.baidu.disconf.client.core.filetype.impl;

import com.baidu.disconf.client.core.filetype.DisconfFileTypeProcessor;
import org.yaml.snakeyaml.Yaml;
import com.baidu.disconf.core.common.utils.YamlFileUtils;
import java.io.FileInputStream;
import java.util.Map;

/**
 * author: zhongjie
 * date: 2019/6/26
 * time: 21:05
 * description:
 */
public class DisconfYmlProcessorImpl implements DisconfFileTypeProcessor {

    @Override
    public Map<String, Object> getKvMap(String fileName) throws Exception {
        Yaml yaml=new Yaml();
        Map<String,Object> map= yaml.loadAs(new FileInputStream(fileName),Map.class);
        return YamlFileUtils.readYamlMap(map);
    }
}
