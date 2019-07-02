package com.baidu.disconf.client.scan.impl;

import com.baidu.disconf.client.config.DisClientConfig;
import com.baidu.disconf.client.scan.inner.statically.impl.StaticScannerNonAnnotationFileMgrImplExt;
import com.baidu.disconf.client.support.registry.Registry;

public class ScanMgrImplExt extends ScanMgrImpl {

	public ScanMgrImplExt(Registry registry) {
		super(registry);
	}
	
	@Override
    public void reloadableScan(String fileName) throws Exception {

        if (DisClientConfig.getInstance().getIgnoreDisconfKeySet().contains(fileName)) {
            return;
        }

        StaticScannerNonAnnotationFileMgrImplExt.scanData2Store(fileName);
    }

}
