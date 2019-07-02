package com.baidu.disconf.core.common.utils;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * author: zhongjie
 * date: 2019/6/24
 * time: 9:22
 * description:
 */
public class YamlFileUtils {

    public  static Map<String, Object>  readYamlMap(Map map) {
        return readYamlMap("", map);
    }

    public static Map<String, Object> readYamlMap(String upKey, Map map) {
        if (StringUtils.isBlank(upKey)) {
            upKey = "";
        } else {
            upKey += ".";
        }
        Map<String, Object> result = Maps.newHashMap();
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof Map) {
                result.putAll(readYamlMap(upKey.concat(key.toString()), (Map) value));
            }
            else{
                result.put(upKey.concat(key.toString()), value);
            }

        }
        return result;
    }


}
