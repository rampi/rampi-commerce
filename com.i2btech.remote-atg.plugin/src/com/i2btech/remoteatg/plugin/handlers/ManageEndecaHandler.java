package com.i2btech.remoteatg.plugin.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

import com.i2btech.remoteatg.plugin.event.JobListener;

public class ManageEndecaHandler extends BaseHandler implements JobListener{
	
	public static final String ENDECA_CONSOLE = "Endeca Console";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try{			
			String paramName = event.getCommand().getParameter("manage").getName();
			switch( paramName ){
			case "start":		    	 
				writeInConsole(ENDECA_CONSOLE, "Starting Endeca, please wait..");
				runJobCommandId(this, "Start Endeca", "command.start.endeca", true);
				break;
			case "stop":				
				writeInConsole(ENDECA_CONSOLE, "Stoping Endeca, please wait..");
				runJobCommandId(this, "Stop Endeca", "command.stop.endeca", true);
				break;
			}
		}catch(Exception ex){
			writeInDefaultConsole(ex.getMessage());
		}
		return null;
	}

	@Override
	public void write(final String str) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try{
					writeInConsole(ENDECA_CONSOLE, str);
				}catch(Exception ex){ex.printStackTrace();}
			}
		});	
	}

	@Override
	public void onEnd(String jobName) {
		
	}

}
