package com.i2btech.remoteatg.plugin.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

import com.i2btech.remoteatg.plugin.control.SSHControl;
import com.i2btech.remoteatg.plugin.event.JobListener;

public class JRebelWLSConfigurationHandler extends BaseHandler implements JobListener{

	public JRebelWLSConfigurationHandler(){
	}
	
	private boolean verifyWLSJRebelEnabled()throws Exception{
		SSHControl ssh = null;
		try{
			ssh = new SSHControl();
			ssh.connect();
			String resp = ssh.exec("command.configured.WLS");
	    	if( resp == null ){
	    		throw new Exception("Error connecting to guest, try again.");
	    	}		    	
	    	return Boolean.parseBoolean(resp.trim());
		}catch(Exception ex){
			throw ex;
		}finally{
			if( ssh != null ){
				ssh.close();
			}
		}
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try{
			
			String manage = event.getCommand().getParameter("manage").getName();
			boolean enabled = verifyWLSJRebelEnabled();
			
			switch( manage ){
			case "enable":									
				if( !enabled ){
			    	writeInDefaultConsole( "[JRebel Configuration] - Stopping WLS..." );
			    	runJobCommandId(this, "enable_stop_wls", "command.stop.WLS", true);
				}else{
					writeInDefaultConsole( "[JRebel Configuration] - JRebel agent is already configured !!" );
				}
				break;								
			case "disable":
				if( enabled ){
					writeInDefaultConsole( "[JRebel Configuration] - Stopping WLS..." );
			    	runJobCommandId(this, "disable_stop_wls", "command.stop.WLS", true);					
				}else{
					writeInDefaultConsole( "[JRebel Configuration] - JRebel agent is already removed !!" );
				}
				break;
			}
								    	
		}catch(Exception ex){
			write(ex.getMessage());
		}
		
		return null;
	}

	@Override
	public void write(final String str) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try{
					writeInConsole(DEFAULT_CONSOLE, str);
				}catch(Exception ex){ex.printStackTrace();}
			}
		});	
	}

	@Override
	public void onEnd(String jobName) {
		try{
			switch( jobName ){
			case "enable_stop_wls":
		    	writeInDefaultConsole("[JRebel Configuration] - WLS Stopped");		    	
		    	writeInDefaultConsole("[JRebel Configuration] - Configuring JRebel Agent...");
		    	runJobCommandId(this, "enable_change_start", "command.changeStart.WLS", true);
				break;
			case "enable_change_start":
		    	runJobCommandId(this, "enable_new_start", "command.newStart.WLS", true);	
				break;
			case "enable_new_start":				
		    	boolean configured = verifyWLSJRebelEnabled();
		    	writeInDefaultConsole( configured ? "[JRebel Configuration] - Configured JRebel agent succesfully" : "[JRebel Configuration] - Configured JRebel agent failed !!" );
				break;
			case "disable_stop_wls":
				writeInDefaultConsole("[JRebel Configuration] - Removing JRebel agent configuration...");
				runJobCommandId(this, "disable_new_start", "command.newStart.WLS.rollback", true);
				break;
			case "disable_new_start":
				runJobCommandId(this, "disable_change_start", "command.changeStart.WLS.rollback", true);
				break;
			case "disable_change_start":
				configured = verifyWLSJRebelEnabled();
		    	writeInDefaultConsole( !configured ? "[JRebel Configuration] - Removed JRebel agent succesfully" : "[JRebel Configuration] - Remove JRebel agent failed !!" );
				break;
			}
		}catch(Exception ex){
			write(ex.getMessage());			
		}
	}
	
}