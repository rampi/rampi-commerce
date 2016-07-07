package com.i2btech.remoteatg.plugin.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

import com.i2btech.remoteatg.plugin.control.SSHControl;
import com.i2btech.remoteatg.plugin.event.JobListener;
import com.i2btech.remoteatg.plugin.util.ResourcesUtil;

public class ManageWLSHandler extends BaseHandler implements JobListener{

	private static final String WLS_CONSOLE = "WebLogic Console";
	
	private static Map<String, SSHControl[]> sshInstancesMap = new HashMap<>();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try{
			
			String paramName = event.getCommand().getParameter("manage").getName();
			
			if( sshInstancesMap.containsKey("wls") ){
				SSHControl[] sshIns = sshInstancesMap.get("wls");				
				for( SSHControl ssh : sshIns ){
					ssh.close();
				}				
			}
			
			switch( paramName ){
			case "start":
				String processId = findProcess(ResourcesUtil.getInstance().getProperty("wls.process.name"));
				if( processId == null || (processId != null && processId.isEmpty() ) ){
					writeInConsole(WLS_CONSOLE, "Starting WLS, please wait..");
					SSHControl sshCommand = new SSHControl();
					SSHControl sshLog = new SSHControl();
					sshInstancesMap.put("wls", new SSHControl[]{sshCommand, sshLog});
					runJobCommandId(sshCommand, this, "Start WebLogic", "command.start.WLS", false);
					readLog(sshLog, 0);
				}else{
					writeInConsole(WLS_CONSOLE, "WLS is started, loading log...");
					SSHControl sshLog = new SSHControl();
					sshInstancesMap.put("wls", new SSHControl[]{sshLog});
					readLog(sshLog, 100);
				}
				break;
			case "stop":
				writeInConsole(WLS_CONSOLE, "Stopping WLS, please wait..");
				runJobCommandId(this, "Stop WebLogic", "command.stop.WLS", true);
				break;
			}
		}catch(Exception ex){
			writeInDefaultConsole(ex.getMessage());
		}
		return null;
	}

	private void readLog(SSHControl ssh, int lines)throws Exception{
		String logPath = ResourcesUtil.getInstance().getProperty("wls.log.path");
		runJobCommandId(ssh, this, "read_wls_log", "command.tail", false, "-n "+lines, logPath);
	}
	
	@Override
	public void write(final String str) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try{
					writeInConsole(WLS_CONSOLE, str);
				}catch(Exception ex){ex.printStackTrace();}
			}
		});				
	}

	@Override
	public void onEnd(String jobName) {
		
	}
	
}
