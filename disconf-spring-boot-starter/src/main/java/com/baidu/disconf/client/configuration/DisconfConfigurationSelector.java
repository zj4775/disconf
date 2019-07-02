package com.baidu.disconf.client.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

public class DisconfConfigurationSelector implements ImportSelector,EnvironmentAware {

	private Environment environment;

	static String scanPackage;
	
	static List<String> locations=new ArrayList<>();
	
	static String CLASSPATH_PREFIX="classpath:/";
	
	static String DISCONF_SCAN_PACKAGE="com.baidu.disconf";

	static String SCAN_PACKAGE="spring.disconf.scanpackage";

	static String RELOAD_LOCATIONS="spring.disconf.locations";

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		MultiValueMap<String,Object> springBootAttributes=importingClassMetadata.getAllAnnotationAttributes("com.baidu.disconf.client.annotations.EnableDisconf");
		List<Object> _scanpkgs=springBootAttributes.get("scanBasePackages");
		checkScanPackage(_scanpkgs);

		if(!environment.containsProperty(SCAN_PACKAGE)){
			throw new RuntimeException(String.format("%s is not config",SCAN_PACKAGE));
		}
		DisconfConfigurationSelector.scanPackage=environment.getProperty(SCAN_PACKAGE);
		String _reloadpkgs=environment.getProperty(RELOAD_LOCATIONS);
		if(StringUtils.isNotBlank(_reloadpkgs)){
			String[] splitpkgs= _reloadpkgs.split(",");
			for(String pkg:splitpkgs){
				DisconfConfigurationSelector.locations.add(CLASSPATH_PREFIX+pkg);
			}
		}
		return new String[] {DisconfConfigBeanConfiguration.class.getTypeName()};
	}

	private void checkScanPackage(List<Object> _scanpkgs) {
		boolean hasScanPath=false;
		if(!CollectionUtils.isEmpty(_scanpkgs)) {
			String[] scanpkgs=(String[])_scanpkgs.get(0);
			for(String pkg:scanpkgs) {
				if(pkg.startsWith(DISCONF_SCAN_PACKAGE)) {
					hasScanPath=true;
				}
			}
		}
		if(!hasScanPath) {
			throw new RuntimeException(
					String.format("disconf path: ' %s ' is not in scan path", DISCONF_SCAN_PACKAGE));
		}
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment=environment;
	}
}
