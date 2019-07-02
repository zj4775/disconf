package com.baidu.disconf.client;

public class DisconfMgrBeanSecondExt {
	public void init() {

        DisconfMgrExt.getInstance().secondScan();
    }

    public void destroy() {
    	DisconfMgrExt.getInstance().close();
    }
}
