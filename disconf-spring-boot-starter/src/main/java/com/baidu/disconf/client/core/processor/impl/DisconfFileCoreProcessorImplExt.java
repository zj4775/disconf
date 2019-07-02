package com.baidu.disconf.client.core.processor.impl;

import com.baidu.disconf.client.common.model.DisConfCommonModel;
import com.baidu.disconf.client.common.model.DisconfCenterFile;
import com.baidu.disconf.client.common.update.IDisconfUpdatePipeline;
import com.baidu.disconf.client.config.DisClientConfig;
import com.baidu.disconf.client.core.filetype.FileTypeProcessorUtils;
import com.baidu.disconf.client.core.processor.DisconfCoreProcessor;
import com.baidu.disconf.client.fetcher.FetcherMgr;
import com.baidu.disconf.client.store.DisconfStoreProcessor;
import com.baidu.disconf.client.store.DisconfStoreProcessorFactory;
import com.baidu.disconf.client.store.inner.DisconfCenterStore;
import com.baidu.disconf.client.store.processor.model.DisconfValue;
import com.baidu.disconf.client.support.registry.Registry;
import com.baidu.disconf.client.watch.WatchMgr;
import com.baidu.disconf.core.common.constants.DisConfigTypeEnum;
import com.baidu.disconf.core.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisconfFileCoreProcessorImplExt implements DisconfCoreProcessor {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DisconfFileCoreProcessorImpl.class);

    // 监控器
    private WatchMgr watchMgr = null;

    // 抓取器
    private FetcherMgr fetcherMgr = null;

    // 仓库算子
    private DisconfStoreProcessor disconfStoreProcessor = DisconfStoreProcessorFactory.getDisconfStoreFileProcessor();

    // bean registry
    private Registry registry = null;
    
    private ApplicationContext applicationContext;

    public DisconfFileCoreProcessorImplExt(WatchMgr watchMgr, FetcherMgr fetcherMgr, Registry registry,ApplicationContext applicationContext) {

        this.fetcherMgr = fetcherMgr;
        this.watchMgr = watchMgr;
        this.registry = registry;
        this.applicationContext=applicationContext;
    }

    /**
     *
     */
    @Override
    public void processAllItems() {

        /**
         * 配置文件列表处理
         */
        for (String fileName : disconfStoreProcessor.getConfKeySet()) {

            processOneItem(fileName);
        }
    }

    @Override
    public void processOneItem(String key) {

        LOGGER.debug("==============\tstart to process disconf file: " + key +
                "\t=============================");

        List<DisconfCenterFile> disconfCenterFiles = disconfStoreProcessor.getConfData(key);

        try {
            updateOneConfFile(key, disconfCenterFiles);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    /**
     * 更新 一個配置文件, 下载、注入到仓库、Watch 三步骤
     */
    private void updateOneConfFile(String fileName, List<DisconfCenterFile> disconfCenterFiles) throws Exception {

        if (CollectionUtils.isEmpty(disconfCenterFiles)) {
            throw new Exception("cannot find disconfCenterFile " + fileName);
        }
        DisconfCenterFile disconfCenterFile=disconfCenterFiles.get(0);
        String filePath = fileName;
        Map<String, Object> dataMap = new HashMap<String, Object>();

        //
        // 开启disconf才需要远程下载, 否则就本地就好
        //
        if (DisClientConfig.getInstance().ENABLE_DISCONF) {

            //
            // 下载配置
            //
            try {

                String url = disconfCenterFile.getRemoteServerUrl();
                filePath = fetcherMgr.downloadFileFromServer(url, fileName, disconfCenterFile.getFileDir());

            } catch (Exception e) {

                //
                // 下载失败了, 尝试使用本地的配置
                //

                LOGGER.error(e.toString(), e);
                LOGGER.warn("using local properties in class path: " + fileName);

                // change file path
                filePath = fileName;
            }
            LOGGER.debug("download ok.");
        }

        try {
            dataMap = FileTypeProcessorUtils.getKvMap(disconfCenterFile.getSupportFileTypeEnum(),
                    disconfCenterFile.getFilePath());
            
            StandardEnvironment standardEnvironment= (StandardEnvironment)applicationContext.getEnvironment();
        	MutablePropertySources mutablePropertySources=standardEnvironment.getPropertySources();
        	if(!mutablePropertySources.contains("disconfAnnotationProperties")) {
        		mutablePropertySources.addLast(new MapPropertySource("disconfAnnotationProperties", dataMap));
        	}else {
        		Map<String, Object> oldMap= (Map<String, Object>)mutablePropertySources.get("disconfAnnotationProperties").getSource();
        		oldMap.putAll(dataMap);
        	}
        	
        } catch (Exception e) {
            LOGGER.error("cannot get kv data for " + filePath, e);
        }

        //
        // 注入到仓库中
        //
        for(DisconfCenterFile disconfCenterFile1:disconfCenterFiles){
            disconfStoreProcessor.inject2Store(fileName,disconfCenterFile1, new DisconfValue(null, dataMap));
        }
        LOGGER.debug("inject ok.");

        //
        // 开启disconf才需要进行watch
        //
        if (DisClientConfig.getInstance().ENABLE_DISCONF) {
            //
            // Watch
            //
            DisConfCommonModel disConfCommonModel = disconfStoreProcessor.getCommonModel(fileName);
            if (watchMgr != null) {
                watchMgr.watchPath(this, disConfCommonModel, fileName, DisConfigTypeEnum.FILE,
                        GsonUtils.toJson(disconfCenterFile.getKV()));
                LOGGER.debug("watch ok.");
            } else {
                LOGGER.warn("cannot monitor {} because watch mgr is null", fileName);
            }
        }
    }

    /**
     * 更新消息: 某个配置文件 + 回调
     */
    @Override
    public void updateOneConfAndCallback(String key) throws Exception {

        // 更新 配置
        updateOneConf(key);

        // 回调
        DisconfCoreProcessUtils.callOneConf(disconfStoreProcessor, key);
        callUpdatePipeline(key);
    }

    /**
     * param key
     */
    private void callUpdatePipeline(String key) {

        List<DisconfCenterFile> disconfCenterFiles = disconfStoreProcessor.getConfData(key);
        if (!CollectionUtils.isEmpty(disconfCenterFiles)){
            DisconfCenterFile disconfCenterFile = disconfCenterFiles.get(0);

            IDisconfUpdatePipeline iDisconfUpdatePipeline =
                    DisconfCenterStore.getInstance().getiDisconfUpdatePipeline();
            if (iDisconfUpdatePipeline != null) {
                try {
                    iDisconfUpdatePipeline.reloadDisconfFile(key, disconfCenterFile.getFilePath());
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        }
    }

    /**
     * 更新消息：某个配置文件
     */
    private void updateOneConf(String fileName) throws Exception {

        List<DisconfCenterFile> disconfCenterFiles = disconfStoreProcessor.getConfData(fileName);

        if (!CollectionUtils.isEmpty(disconfCenterFiles)) {
            // 更新仓库
            updateOneConfFile(fileName, disconfCenterFiles);
            // 更新实例
            inject2OneConf(fileName, disconfCenterFiles);
        }
    }

    /**
     * 为某个配置文件进行注入实例中
     */
    private void inject2OneConf(String fileName, List<DisconfCenterFile> disconfCenterFiles) {
        if (CollectionUtils.isEmpty(disconfCenterFiles)) {
            return;
        }

        for(DisconfCenterFile disconfCenterFile:disconfCenterFiles){
            try {
                //
                // 获取实例
                //
                Object object;
                try {

                    object = disconfCenterFile.getObject();
                    if (object == null) {
                        object = registry.getFirstByType(disconfCenterFile.getCls(), false, true);
                        disconfCenterFile.setObject(object);
                    }

                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }

                // 注入实体中
                disconfStoreProcessor.inject2Instance(disconfCenterFile, fileName);

            } catch (Exception e) {
                LOGGER.warn(e.toString(), e);
            }
        }


    }

    @Override
    public void inject2Conf() {

        /**
         * 配置文件列表处理
         */
        for (String key : disconfStoreProcessor.getConfKeySet()) {

            LOGGER.debug("==============\tstart to inject value to disconf file item instance: " + key +
                    "\t=============================");

            List<DisconfCenterFile> disconfCenterFiles = disconfStoreProcessor.getConfData(key);

            inject2OneConf(key, disconfCenterFiles);
        }
    }
}
