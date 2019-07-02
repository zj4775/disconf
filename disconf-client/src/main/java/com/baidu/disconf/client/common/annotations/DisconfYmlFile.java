package com.baidu.disconf.client.common.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * yml配置文件注解
 *
 * author zhongjie
 * @version 2019-6-30
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DisconfYmlFile {

    /**
     * 配置文件名,必须指定
     */
    String filename();

    /**
     * 环境,默认为用户指定的环境
     */
    String env() default "";

    /**
     * 版本,默认为用户指定的版本
     */
    String version() default "";

    /**
     * 版本,默认为用户指定的app
     */
    String app() default "";

    /**
     * 配置文件目标地址dir, 以"/"开头则是系统的全路径，否则则是相对于classpath的路径，默认是classpath根路径
     * 注意：根路径要注意是否有权限，否则会出现找不到路径，推荐采用相对路径
     *
     * return
     */
    String targetDirPath() default "";
}
