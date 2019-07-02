package com.baidu.disconf.client.configuration;

import com.baidu.disconf.client.DisconfMgrBeanExt;
import com.baidu.disconf.client.DisconfMgrBeanSecondExt;
import com.baidu.disconf.client.addons.properties.ReloadablePropertiesFactoryBeanExt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: zhongjie
 * date: 2019/7/2
 * time: 9:00
 * description:
 */
@Configuration
@ComponentScan("com.baidu.disconf")
public class DisconfConfigBeanConfiguration {

    @Bean(destroyMethod="destroy")
    public DisconfMgrBeanExt disconfMgrBeanExt() {
        DisconfMgrBeanExt disconfMgrBeanExt=new DisconfMgrBeanExt();
        disconfMgrBeanExt.setScanPackage(DisconfConfigurationSelector.scanPackage);
        return disconfMgrBeanExt;
    }
    @Bean(initMethod="init", destroyMethod="destroy")
    public DisconfMgrBeanSecondExt disconfMgrBeanSecondExt() {
        DisconfMgrBeanSecondExt disconfMgrBeanSecondExt=new DisconfMgrBeanSecondExt();
        return disconfMgrBeanSecondExt;
    }
    @Bean
    public ReloadablePropertiesFactoryBeanExt reloadablePropertiesFactoryBeanExt() {
        ReloadablePropertiesFactoryBeanExt reloadablePropertiesFactoryBeanExt=new ReloadablePropertiesFactoryBeanExt();
        reloadablePropertiesFactoryBeanExt.setLocations(DisconfConfigurationSelector.locations);
        return reloadablePropertiesFactoryBeanExt;
    }
}
