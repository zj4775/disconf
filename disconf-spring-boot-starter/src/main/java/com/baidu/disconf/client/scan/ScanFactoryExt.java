package com.baidu.disconf.client.scan;

import com.baidu.disconf.client.scan.impl.ScanMgrImplExt;
import com.baidu.disconf.client.support.registry.Registry;

public class ScanFactoryExt extends ScanFactory {
	public static ScanMgr getScanMgr(Registry registry) throws Exception {

        ScanMgr scanMgr = new ScanMgrImplExt(registry);
        return scanMgr;
    }
}
