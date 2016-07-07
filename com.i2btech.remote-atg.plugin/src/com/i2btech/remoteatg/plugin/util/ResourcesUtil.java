package com.i2btech.remoteatg.plugin.util;

import java.util.ResourceBundle;

public class ResourcesUtil {

	private static ResourcesUtil instance;
	
	private ResourceBundle bundle;
	
	private ResourcesUtil()throws Exception{
		bundle = ResourceBundle.getBundle("resources");
	}
	
	public static final ResourcesUtil getInstance()throws Exception{
		if( instance == null ){
			instance = new ResourcesUtil();
		}
		return instance;
	}
	
	public String getProperty(String key)throws Exception{
		return bundle.getString(key);
	}
		
}