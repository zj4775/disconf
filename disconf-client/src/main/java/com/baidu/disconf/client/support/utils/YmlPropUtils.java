package com.baidu.disconf.client.support.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;

/**
 * author: zhongjie
 * date: 2019/6/30
 * time: 11:11
 * description:
 */
public class YmlPropUtils {

    protected static final Logger LOGGER = LoggerFactory.getLogger(YmlPropUtils.class);


    public static Yaml yaml;

    static {
        PropertyUtils propertyUtils=new PropertyUtils();
        propertyUtils.setSkipMissingProperties(true);
        Representer representer=new Representer();
        representer.setPropertyUtils(propertyUtils);
        yaml=new Yaml(representer);
    }


    public static  <T> T convert2Bean(String filePath,Class<T> targetClazz){
        Resource resource = new ClassPathResource(filePath);
        try {
            return yaml.loadAs(resource.getInputStream(), targetClazz);
        } catch (IOException e) {
            LOGGER.error("yml conver 2 bean fail");
        }
        return null;
    }

}
