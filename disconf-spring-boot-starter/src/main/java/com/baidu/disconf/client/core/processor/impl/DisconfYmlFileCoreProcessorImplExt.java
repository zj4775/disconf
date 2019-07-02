package com.baidu.disconf.client.core.processor.impl;

import com.baidu.disconf.client.common.model.DisConfCommonModel;
import com.baidu.disconf.client.common.model.DisconfYmlCenterFile;
import com.baidu.disconf.client.common.update.IDisconfUpdatePipeline;
import com.baidu.disconf.client.config.DisClientConfig;
import com.baidu.disconf.client.core.filetype.FileTypeProcessorUtils;
import com.baidu.disconf.client.core.processor.DisconfCoreProcessor;
import com.baidu.disconf.client.fetcher.FetcherMgr;
import com.baidu.disconf.client.store.DisconfStoreProcessor;
import com.baidu.disconf.client.store.DisconfStoreProcessorFactory;
import com.baidu.disconf.client.store.inner.DisconfCenterStore;
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

public class DisconfYmlFileCoreProcessorImplExt implements DisconfCoreProcessor {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DisconfFileCoreProcessorImpl.class);

    // 监控器
    private WatchMgr watchMgr = null;

    // 抓取器
    private FetcherMgr fetcherMgr = null;

    // 仓库算子
    private DisconfStoreProcessor disconfYmlStoreProcessor = DisconfStoreProcessorFactory.getDisconfYmlStoreFileProcessor();

    // bean registry
    private Registry registry = null;

    private ApplicationContext applicationContext;

    public DisconfYmlFileCoreProcessorImplExt(WatchMgr watchMgr, FetcherMgr fetcherMgr, Registry registry, ApplicationContext applicationContext) {

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
        for (String fileName : disconfYmlStoreProcessor.getConfKeySet()) {

            processOneItem(fileName);
        }
    }

    @Override
    public void processOneItem(String key) {

        LOGGER.debug("==============\tstart to process disconf file: " + key +
                "\t=============================");

        List<DisconfYmlCenterFile> disconfYmlCenterFiles = disconfYmlStoreProcessor.getConfYmlData(key);

        try {
            updateOneConfFile(key, disconfYmlCenterFiles);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    /**
     * 更新 一個配置文件, 下载、注入到仓库、Watch 三步骤
     */
    private void updateOneConfFile(String fileName, List<DisconfYmlCenterFile> disconfYmlCenterFiles) throws Exception {

        if (CollectionUtils.isEmpty(disconfYmlCenterFiles)) {
            throw new Exception("cannot find disconfYmlCenterFile " + fileName);
        }
        DisconfYmlCenterFile disconfYmlCenterFile=disconfYmlCenterFiles.get(0);
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

                String url = disconfYmlCenterFile.getRemoteServerUrl();
                filePath = fetcherMgr.downloadFileFromServer(url, fileName, disconfYmlCenterFile.getFileDir());

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
            dataMap = FileTypeProcessorUtils.getKvMap(disconfYmlCenterFile.getSupportFileTypeEnum(),
                    disconfYmlCenterFile.getFilePath());
            
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

        /**
         * 直接注入bean中
         */
        for(DisconfYmlCenterFile disconfCenterFile1:disconfYmlCenterFiles){
            disconfYmlStoreProcessor.inject2Instance(disconfCenterFile1, fileName);
        }
        LOGGER.debug("inject ok.");

        //
        // 注入到仓库中
        //
        /*for(DisconfCenterFile disconfCenterFile1:disconfYmlCenterFiles){
            disconfYmlStoreProcessor.inject2Store(fileName,disconfCenterFile1, new DisconfValue(null, dataMap));
        }
        LOGGER.debug("inject ok.");*/

        //
        // 开启disconf才需要进行watch
        //
        if (DisClientConfig.getInstance().ENABLE_DISCONF) {
            //
            // Watch
            //
            DisConfCommonModel disConfCommonModel = disconfYmlStoreProcessor.getCommonModel(fileName);
            if (watchMgr != null) {
                watchMgr.watchPath(this, disConfCommonModel, fileName, DisConfigTypeEnum.YMLFILE,
                        GsonUtils.toJson(disconfYmlCenterFile.getKV()));
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
        DisconfCoreProcessUtils.callOneConf(disconfYmlStoreProcessor, key);
        callUpdatePipeline(key);
    }

    /**
     * param key
     */
    private void callUpdatePipeline(String key) {

        List<DisconfYmlCenterFile> disconfYmlCenterFiles = disconfYmlStoreProcessor.getConfYmlData(key);
        if (!CollectionUtils.isEmpty(disconfYmlCenterFiles)){
            DisconfYmlCenterFile disconfYmlCenterFile = disconfYmlCenterFiles.get(0);

            IDisconfUpdatePipeline iDisconfUpdatePipeline =
                    DisconfCenterStore.getInstance().getiDisconfUpdatePipeline();
            if (iDisconfUpdatePipeline != null) {
                try {
                    iDisconfUpdatePipeline.reloadDisconfYmlFile(key, disconfYmlCenterFile.getFilePath());
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

        List<DisconfYmlCenterFile> disconfYmlCenterFiles = disconfYmlStoreProcessor.getConfYmlData(fileName);

        if (!CollectionUtils.isEmpty(disconfYmlCenterFiles)) {
            // 更新仓库
            updateOneConfFile(fileName, disconfYmlCenterFiles);
            // 更新实例
            //inject2OneConf(fileName, disconfCenterFiles);
        }
    }

    /**
     * 为某个配置文件进行注入实例中
     */
    private void inject2OneConf(String fileName, List<DisconfYmlCenterFile> disconfYmlCenterFiles) {
        if (CollectionUtils.isEmpty(disconfYmlCenterFiles)) {
            return;
        }

        for(DisconfYmlCenterFile disconfYmlCenterFile:disconfYmlCenterFiles){
            try {
                //
                // 获取实例
                //
                Object object;
                try {

                    object = disconfYmlCenterFile.getObject();
                    if (object == null) {
                        object = registry.getFirstByType(disconfYmlCenterFile.getCls(), false, true);
                        disconfYmlCenterFile.setObject(object);
                    }

                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }

                // 注入实体中
                disconfYmlStoreProcessor.inject2Instance(disconfYmlCenterFile, fileName);

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
        for (String key : disconfYmlStoreProcessor.getConfKeySet()) {

            LOGGER.debug("==============\tstart to inject value to disconf file item instance: " + key +
                    "\t=============================");

            List<DisconfYmlCenterFile> disconfYmlCenterFiles = disconfYmlStoreProcessor.getConfYmlData(key);

            inject2OneConf(key, disconfYmlCenterFiles);
        }
    }
}
