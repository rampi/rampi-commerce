package com.i2btech.remoteatg.plugin.event;

public interface JobListener {

	public void write(String str); 
	
	public void onEnd(String jobName);
	
}
