package com.baidu.disconf.client.addons.properties;

import com.baidu.disconf.client.DisconfMgrExt;
import com.baidu.disconf.client.config.DisClientConfig;
import com.baidu.disconf.client.config.DisClientSysConfig;
import com.baidu.disconf.client.fetcher.FetcherFactory;
import com.baidu.disconf.client.fetcher.FetcherMgr;
import com.baidu.disconf.core.common.constants.DisConfigTypeEnum;
import com.baidu.disconf.core.common.path.DisconfWebPathMgr;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReloadablePropertiesFactoryBeanExt extends PropertiesFactoryBean
		implements DisposableBean, ApplicationContextAware {

	private static ApplicationContext applicationContext;

	protected static final Logger log = LoggerFactory.getLogger(ReloadablePropertiesFactoryBeanExt.class);

	private Resource[] locations;
	private long[] lastModified;
	private List<IReloadablePropertiesListener> preListeners;
	private boolean globalShareEnable = DisClientConfig.getInstance().isGLOBAL_SHARE_ENABLE();

	/**
	 * 定义global enable
	 * 
	 * author zhongjie
	 * date 2017年1月6日 下午1:22:36
	 *
	 * param globalShareEnable
	 */
	public void setGlobalShareEnable(boolean globalShareEnable) {
		this.globalShareEnable = globalShareEnable;
	}

	/**
	 * 定义资源文件
	 *
	 * param fileNames
	 */
	public void setLocation(final String fileNames) {
		List<String> list = new ArrayList<String>();
		list.add(fileNames);
		setLocations(list);
	}

	/**
	*/
	public void setLocations(List<String> fileNames) {

		List<String> fileNameList = new ArrayList<String>();

		if (this.globalShareEnable && DisClientConfig.getInstance().isGLOBAL_SHARE_ENABLE()) {
			log.info("global share enable");
			// 获取global配置项获取global文件列表
			String[] fileList = loadGlobalItem();
			if (fileList != null && fileList.length > 0) {
				fileNameList.addAll(Arrays.asList(fileList));
			}
		} else {
			log.info("global share disable");
		}

		fileNameList.addAll(fileNames);
		List<Resource> resources = new ArrayList<Resource>();
		for (String filename : fileNameList) {

			// trim
			filename = filename.trim();

			String realFileName = getFileName(filename);

			//
			// register to disconf
			//
			DisconfMgrExt.getInstance().reloadableScan(realFileName);

			//
			// only properties will reload
			//
			String ext = FilenameUtils.getExtension(filename);
			if (ext.equals("properties")) {

				PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
				try {
					Resource[] resourceList = pathMatchingResourcePatternResolver.getResources(filename);
					for (Resource resource : resourceList) {
						resources.add(resource);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		this.locations = resources.toArray(new Resource[resources.size()]);
		lastModified = new long[locations.length];
		super.setLocations(locations);
	}

	/**
	 * get file name from resource
	 *
	 * param fileName
	 *
	 * return
	 */
	private String getFileName(String fileName) {

		if (fileName != null) {
			int index = fileName.indexOf(':');
			if (index < 0) {
				return fileName;
			} else {

				fileName = fileName.substring(index + 1);

				index = fileName.lastIndexOf('/');
				if (index < 0) {
					return fileName;
				} else {
					return fileName.substring(index + 1);
				}

			}
		}
		return null;
	}

	protected Resource[] getLocations() {
		return locations;
	}

	/**
	 * listener , 用于通知回调
	 *
	 * param listeners
	 */
	public void setListeners(final List listeners) {
		// early type check, and avoid aliassing
		this.preListeners = new ArrayList<IReloadablePropertiesListener>();
		for (Object o : listeners) {
			preListeners.add((IReloadablePropertiesListener) o);
		}
	}

	private ReloadablePropertiesBase reloadableProperties;

	/**
	 * return
	 *
	 * @throws IOException
	 */
	@Override
	protected Properties createProperties() throws IOException {

		Properties properties= (Properties) createMyInstance();
    	StandardEnvironment standardEnvironment= (StandardEnvironment)applicationContext.getEnvironment();
    	MutablePropertySources mutablePropertySources=standardEnvironment.getPropertySources();
    	mutablePropertySources.addLast(new MapPropertySource("disconfProperties", (Map)properties));
    	return properties;
	}

	/**
	 * createInstance 废弃了
	 *
	 * @throws IOException
	 */
	protected Object createMyInstance() throws IOException {
		// would like to uninherit from AbstractFactoryBean (but it's final!)
		if (!isSingleton()) {
			throw new RuntimeException("ReloadablePropertiesFactoryBean only works as singleton");
		}

		// set listener
		reloadableProperties = new ReloadablePropertiesImpl();
		if (preListeners != null) {
			reloadableProperties.setListeners(preListeners);
		}

		// reload
		reload(true);

		// add for monitor
		ReloadConfigurationMonitor.addReconfigurableBean((ReconfigurableBean) reloadableProperties);

		return reloadableProperties;
	}

	public void destroy() throws Exception {
		reloadableProperties = null;
	}

	/**
	 * 根据修改时间来判定是否reload
	 *
	 * param forceReload
	 *
	 * @throws IOException
	 */
	protected void reload(final boolean forceReload) throws IOException {

		boolean reload = forceReload;
		for (int i = 0; i < locations.length; i++) {
			Resource location = locations[i];
			File file;

			try {
				file = location.getFile();
			} catch (IOException e) {
				// not a file resource
				// may be spring boot
				log.warn(e.toString());
				continue;
			}
			try {
				long l = file.lastModified();

				if (l > lastModified[i]) {
					lastModified[i] = l;
					reload = true;
				}
			} catch (Exception e) {
				// cannot access file. assume unchanged.
				if (log.isDebugEnabled()) {
					log.debug("can't determine modification time of " + file + " for " + location, e);
				}
			}
		}
		if (reload) {
			doReload();
		}
	}

	/**
	 * 设置新的值
	 *
	 * @throws IOException
	 */
	private void doReload() throws IOException {
		reloadableProperties.setProperties(mergeProperties());
	}

	/**
	 * return
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}


	/**
	 * 回调自己
	 */
	class ReloadablePropertiesImpl extends ReloadablePropertiesBase implements ReconfigurableBean {

		// reload myself
		public void reloadConfiguration() throws Exception {
			ReloadablePropertiesFactoryBeanExt.this.reload(false);
		}
	}

	/**
	 * 加载global配置项，获取global文件列表
	 */
	private String[] loadGlobalItem() {
		String keyName = "global";
		String value = null;
		String[] fileNames = null;
		//
		// 开启disconf才需要远程下载, 否则就用默认值
		//
		if (DisClientConfig.getInstance().ENABLE_DISCONF) {
			//
			// 下载配置
			//
			try {
				FetcherMgr fetcherMgr = FetcherFactory.getFetcherMgr();
				// Disconf-web url
				String url = DisconfWebPathMgr.getRemoteUrlParameter(
						DisClientSysConfig.getInstance().CONF_SERVER_STORE_ACTION, DisClientConfig.getInstance().getGLOBAL_APP(),
						DisClientConfig.getInstance().getGLOBAL_VERSION(), DisClientConfig.getInstance().ENV,
						DisClientConfig.getInstance().getGLOBAL_KEY(), DisConfigTypeEnum.ITEM);

				value = fetcherMgr.getValueFromServer(url);
				if (value != null) {
					log.debug("value: " + value);
					// value获取global文件列表
					fileNames = value.split(",");
					for (int i = 0; i < fileNames.length; i++) {
						fileNames[i] = "classpath:/" + fileNames[i];
					}
				}
				log.info("loaded global prop ok.");

			} catch (Exception e) {
				log.error("cannot use remote configuration: " + keyName, e);
				log.info("skip variable: " + keyName);
			}
		} else {
			PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
			try {
				Resource[] resourceList = pathMatchingResourcePatternResolver.getResources("global*.properties");
				fileNames = new String[resourceList.length];
				for (int i = 0; i < resourceList.length; i++) {
					fileNames[i] = "classpath:/" + resourceList[i].getFilename();
				}
			} catch (IOException e) {
				log.error("disconf remote conf false,scan global config throw exception:{}", e);
			}
		}
		return fileNames;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		this.applicationContext = applicationContext;
		// 如果locations 为空，初始化localtions，因为采用了global
		if (this.getLocations() == null || this.getLocations().length <= 0) {
			setLocations(new ArrayList<String>());
		}
	}
}
