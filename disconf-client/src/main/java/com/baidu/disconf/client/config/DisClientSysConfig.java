package com.baidu.disconf.client.config;

import com.baidu.disconf.client.config.inner.DisInnerConfigAnnotation;
import com.baidu.disconf.client.support.utils.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;

/**
 * Disconf 系统自带的配置
 *
 * author liaoqiqi
 * @version 2014-6-6
 */
public class DisClientSysConfig {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DisClientSysConfig.class);

    protected static final DisClientSysConfig INSTANCE = new DisClientSysConfig();

    public static DisClientSysConfig getInstance() {
        return INSTANCE;
    }

    protected static final String filename = "disconf_sys.properties";

    private boolean isLoaded = false;

    private DisClientSysConfig() {

    }

    public synchronized boolean isLoaded() {
        return isLoaded;
    }

    /**
     * load config normal
     */
    public synchronized void loadConfig(Environment environment) throws Exception {

        if (isLoaded) {
            return;
        }

        String filePathInternal = filename;



        Field[] declaredFields = INSTANCE.getClass().getDeclaredFields();
        for(Field field:declaredFields){
            if(field.isAnnotationPresent(DisInnerConfigAnnotation.class)){
                DisInnerConfigAnnotation annotation = field.getAnnotation(DisInnerConfigAnnotation.class);
                if(StringUtils.isNotBlank(annotation.name())){
                    String value = environment.getProperty(annotation.name());
                    if(null==value){
                        value=annotation.defaultValue();
                    }
                    field.setAccessible(true);
                    if (null != value) {
                        try {
                            ClassUtils.setFieldValeByType(field, INSTANCE, value);
                        } catch (Exception e) {
                            LOGGER.error(String.format("invalid config: %s", annotation.name()), e);
                        }
                    }
                }
            }
        }

        //DisconfAutowareConfig.autowareConfig(INSTANCE, filePathInternal);

        isLoaded = true;
    }

    /**
     * STORE URL
     *
     * author
     * @since 1.0.0
     */
    @DisInnerConfigAnnotation(name = "disconf.conf_server_store_action",defaultValue = "/api/config")
    public String CONF_SERVER_STORE_ACTION;

    /**
     * STORE URL
     *
     * author
     * @since 1.0.0
     */
    @DisInnerConfigAnnotation(name = "disconf.conf_server_zoo_action",defaultValue = "/api/zoo")
    public String CONF_SERVER_ZOO_ACTION;

    /**
     * 获取远程主机个数的URL
     *
     * author
     * @since 1.0.0
     */
    @DisInnerConfigAnnotation(name = "disconf.conf_server_master_num_action",defaultValue = "/api/getmasterinfo")
    public String CONF_SERVER_MASTER_NUM_ACTION;

    /**
     * 下载文件夹, 远程文件下载后会放在这里
     *
     * author
     * @since 1.0.0
     */
    @DisInnerConfigAnnotation(name = "disconf.local_download_dir",defaultValue = "/disconf/download")
    public String LOCAL_DOWNLOAD_DIR;

}
