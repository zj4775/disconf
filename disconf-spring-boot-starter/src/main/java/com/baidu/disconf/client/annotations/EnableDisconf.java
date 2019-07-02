package com.baidu.disconf.client.annotations;

import com.baidu.disconf.client.configuration.DisconfConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Documented
@Import(DisconfConfigurationSelector.class)
public @interface EnableDisconf {

    String[] scanBasePackages() default "com.baidu.disconf";
}
