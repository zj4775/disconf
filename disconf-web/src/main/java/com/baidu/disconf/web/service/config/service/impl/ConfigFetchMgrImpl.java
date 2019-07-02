package com.baidu.disconf.web.service.config.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baidu.disconf.core.common.constants.Constants;
import com.baidu.disconf.core.common.constants.DisConfigTypeEnum;
import com.baidu.disconf.core.common.json.ValueVo;
import com.baidu.disconf.web.service.config.bo.Config;
import com.baidu.disconf.web.service.config.dao.ConfigDao;
import com.baidu.disconf.web.service.config.service.ConfigFetchMgr;
import com.baidu.disconf.web.service.config.utils.ConfigUtils;
import com.baidu.disconf.web.utils.CodeUtils;

/**
 * author knightliao
 */
@Service
public class ConfigFetchMgrImpl implements ConfigFetchMgr {

    protected static final Logger LOG = LoggerFactory.getLogger(ConfigFetchMgrImpl.class);

    @Autowired
    private ConfigDao configDao;

    /**
     * 根据详细参数获取配置
     */
    @Override
    public Config getConfByParameter(Long appId, Long envId, String version, String key,
                                     DisConfigTypeEnum disConfigTypeEnum) {

    	Config config= configDao.getByParameter(appId, envId, version, key, disConfigTypeEnum);
    	if(null!=config) {
    		config.setValue(CodeUtils.unicodeToUtf8(config.getValue()));
    		return config;
    	}
    	return null;
    }

    /**
     * 根据详细参数获取配置返回
     */
    public ValueVo getConfItemByParameter(Long appId, Long envId, String version, String key) {

        Config config = configDao.getByParameter(appId, envId, version, key, DisConfigTypeEnum.ITEM);
        if (config == null) {
            return ConfigUtils.getErrorVo("cannot find this config");
        }

        ValueVo valueVo = new ValueVo();
        valueVo.setValue(CodeUtils.unicodeToUtf8(config.getValue()));
        valueVo.setStatus(Constants.OK);

        return valueVo;
    }

    /**
     * 根据详细参数获取配置列表返回
     */
    public List<Config> getConfListByParameter(Long appId, Long envId, String version, Boolean hasValue) {
    	List<Config> configs=configDao.getConfigList(appId, envId, version, hasValue);
    	if(!CollectionUtils.isEmpty(configs)) {
    		for(Config config:configs) {
    			config.setValue(CodeUtils.unicodeToUtf8(config.getValue()));
    		}
    		return configs;
    	}
        return null;
    }

}
