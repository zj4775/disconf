package com.baidu.disconf.client.core.impl;

import com.baidu.disconf.client.core.DisconfCoreMgr;
import com.baidu.disconf.client.core.processor.DisconfCoreProcessor;
import com.baidu.disconf.client.core.processor.DisconfCoreProcessorFactory;
import com.baidu.disconf.client.core.processor.DisconfCoreProcessorFactoryExt;
import com.baidu.disconf.client.fetcher.FetcherMgr;
import com.baidu.disconf.client.support.registry.Registry;
import com.baidu.disconf.client.watch.WatchMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class DisconfCoreMgrImplExt implements DisconfCoreMgr {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DisconfCoreMgrImpl.class);

    private List<DisconfCoreProcessor> disconfCoreProcessorList = new ArrayList<DisconfCoreProcessor>();

    // 监控器
    private WatchMgr watchMgr = null;

    // 抓取器
    private FetcherMgr fetcherMgr = null;

    // registry
    private Registry registry = null;
    
    public DisconfCoreMgrImplExt(WatchMgr watchMgr, FetcherMgr fetcherMgr, Registry registry,ApplicationContext applicationContext) {

        this.watchMgr = watchMgr;
        this.fetcherMgr = fetcherMgr;
        this.registry = registry;

        //
        // 在这里添加好配置项、配置文件的处理器
        //
        DisconfCoreProcessor disconfCoreProcessorFile =
                DisconfCoreProcessorFactoryExt.getDisconfCoreProcessorFile(watchMgr, fetcherMgr, registry,applicationContext);
        disconfCoreProcessorList.add(disconfCoreProcessorFile);

        DisconfCoreProcessor disconfYmlCoreProcessorFile =
                DisconfCoreProcessorFactoryExt.getDisconfYmlCoreProcessorFile(watchMgr, fetcherMgr, registry,applicationContext);
        disconfCoreProcessorList.add(disconfYmlCoreProcessorFile);

        DisconfCoreProcessor disconfCoreProcessorItem =
                DisconfCoreProcessorFactoryExt.getDisconfCoreProcessorItem(watchMgr, fetcherMgr, registry,applicationContext);
        disconfCoreProcessorList.add(disconfCoreProcessorItem);
    }

    /**
     * 1. 获取远程的所有配置数据<br/>
     * 2. 注入到仓库中<br/>
     * 3. Watch 配置 <br/>
     * <p/>
     * 更新 所有配置数据
     */
    public void process(ApplicationContext context) {

        //
        // 处理
        //
        for (DisconfCoreProcessor disconfCoreProcessor : disconfCoreProcessorList) {

            disconfCoreProcessor.processAllItems();
        }
    }
    
    public void process() {

        //
        // 处理
        //
        for (DisconfCoreProcessor disconfCoreProcessor : disconfCoreProcessorList) {

            disconfCoreProcessor.processAllItems();
        }
    }
    
    

    /**
     * 只处理某一个
     */
    @Override
    public void processFile(String fileName) {

        DisconfCoreProcessor disconfCoreProcessorFile =
                DisconfCoreProcessorFactory.getDisconfCoreProcessorFile(watchMgr, fetcherMgr, registry);

        disconfCoreProcessorFile.processOneItem(fileName);
    }

    /**
     * 特殊的，将仓库里的数据注入到 配置项、配置文件 的实体中
     */
    public void inject2DisconfInstance() {

        //
        // 处理
        //
        for (DisconfCoreProcessor disconfCoreProcessor : disconfCoreProcessorList) {

            disconfCoreProcessor.inject2Conf();
        }
    }

    @Override
    public void release() {

        if (fetcherMgr != null) {
            fetcherMgr.release();
        }

        if (watchMgr != null) {
            watchMgr.release();
        }
    }
}
