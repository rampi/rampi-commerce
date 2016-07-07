package com.i2btech.remoteatg.plugin.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.i2btech.remoteatg.plugin.control.SSHControl;
import com.i2btech.remoteatg.plugin.event.JobListener;
import com.i2btech.remoteatg.plugin.util.ResourcesUtil;

public class ManageATGHandler extends BaseHandler implements JobListener{

	private String consoleId;
	
	private String instanceName;
	
	private String instanceId;
	
	private String scriptPath;
	
	private static Map<String, SSHControl[]> sshInstancesMap = new HashMap<>();
	
	@Override
	protected String findProcess(String processName)throws Exception{
		SSHControl ssh = null;
		try{
			ssh = new SSHControl();
			ssh.connect();
			return ssh.exec("command.find.process.atg.instance", processName, "{_id}", "'{print $2}'");
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
			
			String ACTION = event.getCommand().getParameter("manage").getName();
			instanceId = event.getCommand().getParameter("instanceId").getName();
			
			instanceName = ResourcesUtil.getInstance().getProperty("atg.instance.".concat(instanceId).concat(".name"));
			consoleId = ResourcesUtil.getInstance().getProperty("atg.instance.".concat(instanceId).concat(".consoleId"));
			scriptPath = ResourcesUtil.getInstance().getProperty("atg.instance.".concat(instanceId).concat(".scriptPath"));
			
			if( sshInstancesMap.containsKey(instanceId) ){
				SSHControl[] sshIns = sshInstancesMap.get(instanceId);				
				for( SSHControl ssh : sshIns ){
					ssh.close();
				}				
			}
			
			switch( ACTION ){
			case "start":
				String processId = findProcess(instanceName);
				if( processId == null || (processId != null && processId.isEmpty() ) ){
					writeInConsole(consoleId, "Starting "+instanceName+", please wait..");					
					SSHControl sshCommand = new SSHControl();
					SSHControl sshLog = new SSHControl();					
					sshInstancesMap.put(instanceId, new SSHControl[]{sshCommand, sshLog});					
					runJob(sshCommand, this, "start_instance", scriptPath, false);
					readLog(sshLog, 0);					
				}else{
					writeInConsole(consoleId, instanceName+" is started, loading log...");
					SSHControl sshLog = new SSHControl();
					sshInstancesMap.put(instanceId, new SSHControl[]{sshLog});
					readLog(sshLog, 100);
				}
				break;
			case "stop":
				writeInConsole(consoleId, "Stopping "+instanceName+", please wait..");
				Job job = new Job(consoleId) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						SSHControl ssh = null;
						try{					
							ssh = new SSHControl();
				    		ssh.connect();
							String response = ssh.exec("command.find.process", instanceName, "{_id}", "'{print $2}'");						
							if( response == null || ( response != null && response.isEmpty() ) ){
								write(instanceName+" is not running");								
							}else{
								String[] pids = response != null && response.contains("\n") ? response.trim().split("\n") : new String[]{response};								
								for( String pid : pids ){
									write("Killing process "+pid);
									write(ssh.exec("command.kill.process", pid));
									write("Process killed");
								}
								write(instanceName+" is stopped");
							}							
						}catch(Exception ex){
							write(ex.getMessage());
						}finally{
							ssh.close();
						}
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();		
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
					writeInConsole(consoleId, str);
				}catch(Exception ex){ex.printStackTrace();}
			}
		});		
	}

	private void readLog(SSHControl ssh, int lines)throws Exception{
		String logPath = ResourcesUtil.getInstance().getProperty("atg.instance.".concat(instanceId).concat(".logPath"));
		runJobCommandId(ssh, this, "read_instance_log", "command.tail", false, "-n "+lines, logPath);
	}
	
	@Override
	public void onEnd(String jobName) {}
	
}