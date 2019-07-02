package com.baidu.disconf.client.core;

import org.springframework.context.ApplicationContext;

import com.baidu.disconf.client.config.DisClientConfig;
import com.baidu.disconf.client.core.impl.DisconfCoreMgrImpl;
import com.baidu.disconf.client.core.impl.DisconfCoreMgrImplExt;
import com.baidu.disconf.client.fetcher.FetcherFactory;
import com.baidu.disconf.client.fetcher.FetcherMgr;
import com.baidu.disconf.client.support.registry.Registry;
import com.baidu.disconf.client.watch.WatchFactory;
import com.baidu.disconf.client.watch.WatchMgr;

public class DisconfCoreFactoryExt {
	public static DisconfCoreMgr getDisconfCoreMgr(Registry registry,ApplicationContext applicationContext) throws Exception {

        FetcherMgr fetcherMgr = FetcherFactory.getFetcherMgr();

        //
        // 不开启disconf，则不要watch了
        //
        WatchMgr watchMgr = null;
        if (DisClientConfig.getInstance().ENABLE_DISCONF) {
            // Watch 模块
            watchMgr = WatchFactory.getWatchMgr(fetcherMgr);
        }

        return new DisconfCoreMgrImplExt(watchMgr, fetcherMgr, registry,applicationContext);
    }
}
