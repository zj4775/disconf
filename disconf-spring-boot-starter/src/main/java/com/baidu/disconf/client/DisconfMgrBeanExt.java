package com.baidu.disconf.client;

import com.baidu.disconf.client.addons.properties.ReloadablePropertiesFactoryBeanExt;
import com.baidu.disconf.client.store.aspect.DisconfAspectJ;
import com.baidu.disconf.client.store.inner.DisconfCenterHostFilesStore;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DisconfMgrBeanExt implements BeanDefinitionRegistryPostProcessor, PriorityOrdered, ApplicationContextAware {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DisconfMgrBeanExt.class);

	public final static String SCAN_SPLIT_TOKEN = ",";

    private ApplicationContext applicationContext;

    private String scanPackage = null;

    public void destroy() {

        DisconfMgrExt.getInstance().close();
    }

    public void setScanPackage(String scanPackage) {
        this.scanPackage = scanPackage;
    }

    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    /**
     * 这个函数无法达到最高优先级，例如PropertyPlaceholderConfigurer
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    /**
     * 第一次扫描
     * 在Spring内部的Bean定义初始化后执行，这样是最高优先级的
     */
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        // 为了做兼容
        DisconfCenterHostFilesStore.getInstance().addJustHostFileSet(fileList);

        List<String> scanPackList = parseStringToStringList(scanPackage, SCAN_SPLIT_TOKEN);
        // unique
        Set<String> hs = new HashSet<String>();
        if(null==scanPackList) {
        	scanPackList=new ArrayList<>();
        }
        hs.addAll(scanPackList);
        scanPackList.clear();
        scanPackList.addAll(hs);

        // 进行扫描
        DisconfMgrExt.getInstance().setApplicationContext(applicationContext);
        DisconfMgrExt.getInstance().firstScan(scanPackList);

        // register java bean
        registerAspect(registry);
        
        /**
         * 在此处就立马实例化该bean,并将资源文件放入环境上下文中
         */
        LOGGER.info("initial ReloadablePropertiesFactoryBeanExt begin ...");
        applicationContext.getBean(ReloadablePropertiesFactoryBeanExt.class);
        LOGGER.info("initial ReloadablePropertiesFactoryBeanExt end!");
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * register aspectJ for disconf get request
     *
     * param registry
     */
    private void registerAspect(BeanDefinitionRegistry registry) {

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DisconfAspectJ.class);
        beanDefinition.setLazyInit(false);
        beanDefinition.setAbstract(false);
        beanDefinition.setAutowireCandidate(true);
        beanDefinition.setScope("singleton");

        registry.registerBeanDefinition("disconfAspectJ", beanDefinition);
    }

    private List<String> parseStringToStringList(String source,String token){
        if (StringUtils.isBlank(source) || StringUtils.isEmpty(token)) {
            return null;
        }

        List<String> result = new ArrayList<String>();

        String[] units = source.split(token);
        for (String unit : units) {
            result.add(unit);
        }
        return result;
    }

    /*
     * 已经废弃了，不推荐使用
     */
    @Deprecated
    private Set<String> fileList = new HashSet<String>();

    @Deprecated
    public Set<String> getFileList() {
        return fileList;
    }

    @Deprecated
    public void setFileList(Set<String> fileList) {
        this.fileList = fileList;
    }
}
