package com.i2btech.remoteatg.plugin.control;

import java.text.MessageFormat;

import com.i2btech.remoteatg.plugin.event.JobListener;
import com.i2btech.remoteatg.plugin.util.ResourcesUtil;
import com.i2btech.remoteatg.plugin.util.SSHUtil;

public class SSHControl {

	private SSHUtil ssh = new SSHUtil();
	
	private ResourcesUtil res;
	
	public SSHControl()throws Exception{
		
	}

	public void connect()throws Exception{
		try{			
			res = ResourcesUtil.getInstance();			 
			ssh.connect(
	    		res.getProperty("host"),
	    		Integer.parseInt(res.getProperty("port")),
	    		res.getProperty("username"), 
	    		res.getProperty("password")			     
			);		
		}catch(Exception ex){
			throw new Exception("Error connecting to i2bCommerce machine, try again - Is i2bCommerce machine running ?\nMessage: "+ex.getMessage());
		}		 
	}
	
	public String exec(String idCommand, String... args)throws Exception{
		String command = res.getProperty(idCommand);
		String result = MessageFormat.format(command, (Object[])args);
		JobListener listener = ssh.getListener();
		if( listener != null ){
			listener.write( "Executing command "+result );
		}
		return ssh.exec(result);
	}
	
	public String execCommand(String command, String... args)throws Exception{
		String result = MessageFormat.format(command, (Object[])args);
		JobListener listener = ssh.getListener();
		if( listener != null ){
			listener.write( "Executing command "+result );
		}
		return ssh.exec(result);
	}
		
	public void close(){
		ssh.close();
	}
	
	public void verifyConnection()throws Exception{
		if( !ssh.isConnected() ){
			try{
				ssh.reconnect();
			}catch(Exception ex){
				throw new Exception("Error connecting to i2bCommerce machine, try again - Is i2bCommerce machine running ?\nMessage: "+ex.getMessage());
			}
		}
	}

	public boolean isConnected(){
		return ssh.isConnected();
	}
	
	public JobListener getListener() {
		return ssh.getListener();
	}

	public void setListener(JobListener listener) {
		ssh.setListener(listener);
	}
	
}
