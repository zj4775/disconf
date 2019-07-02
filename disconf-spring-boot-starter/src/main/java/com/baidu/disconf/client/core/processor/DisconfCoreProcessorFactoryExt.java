package com.baidu.disconf.client.core.processor;

import com.baidu.disconf.client.core.processor.impl.DisconfFileCoreProcessorImplExt;
import com.baidu.disconf.client.core.processor.impl.DisconfItemCoreProcessorImplExt;
import com.baidu.disconf.client.core.processor.impl.DisconfYmlFileCoreProcessorImplExt;
import com.baidu.disconf.client.fetcher.FetcherMgr;
import com.baidu.disconf.client.support.registry.Registry;
import com.baidu.disconf.client.watch.WatchMgr;
import org.springframework.context.ApplicationContext;

public class DisconfCoreProcessorFactoryExt {

    /**
     * 获取配置文件核心处理器
     */
    public static DisconfCoreProcessor getDisconfCoreProcessorFile(WatchMgr watchMgr, FetcherMgr fetcherMgr, Registry
            registry,ApplicationContext applicationContext) {

        return new DisconfFileCoreProcessorImplExt(watchMgr, fetcherMgr, registry,applicationContext);
    }
    public static DisconfCoreProcessor getDisconfYmlCoreProcessorFile(WatchMgr watchMgr, FetcherMgr fetcherMgr, Registry
            registry,ApplicationContext applicationContext) {

        return new DisconfYmlFileCoreProcessorImplExt(watchMgr, fetcherMgr, registry,applicationContext);
    }

    /**
     * 获取配置项核心 处理器
     */
    public static DisconfCoreProcessor getDisconfCoreProcessorItem(WatchMgr watchMgr, FetcherMgr fetcherMgr, Registry
            registry,ApplicationContext applicationContext) {

        return new DisconfItemCoreProcessorImplExt(watchMgr, fetcherMgr, registry,applicationContext);
    }
}
