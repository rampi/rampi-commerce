package com.i2btech.remoteatg.plugin.util;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class PreferencesUtil {

	private Preferences preferences;
	
	public PreferencesUtil(){
		this("com.i2btech.remote-atg.plugin");
	}
	
	public PreferencesUtil(String preferenceName){		
		preferences = InstanceScope.INSTANCE.getNode(preferenceName);
	}
	
	public String get(String key){
		return preferences.get(key, null);
	}
	
	public void put(String key, String value){
		preferences.put(key, value);
	}
	
	public void flush() throws BackingStoreException{
		preferences.flush();
	}
	
}