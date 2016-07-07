package com.i2btech.remoteatg.plugin.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import com.i2btech.remoteatg.plugin.control.SSHControl;
import com.i2btech.remoteatg.plugin.event.JobListener;
import com.i2btech.remoteatg.plugin.util.ResourcesUtil;

public class BaseHandler extends AbstractHandler {
	
	public static final String DEFAULT_CONSOLE = "I2B Commerce Console";
	
	public BaseHandler(){
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}
	
	public void writeInDefaultConsole(String info){
		MessageConsole console = findConsole(DEFAULT_CONSOLE);
		console.newMessageStream().print(info+"\n");
	}
	
	public void writeInConsole(String consoleName, String info){
		MessageConsole console = findConsole(consoleName);
		console.newMessageStream().print(info+"\n");
	}

	private MessageConsole findConsole(String name) {			     
		ConsolePlugin plugin = ConsolePlugin.getDefault();	      
		IConsoleManager conMan = plugin.getConsoleManager();	      
		IConsole[] existing = conMan.getConsoles();	      
		for (int i = 0; i < existing.length; i++){	         
			if (name.equals(existing[i].getName())){	     
				ConsolePlugin.getDefault().getConsoleManager().showConsoleView(existing[i]);
				return (MessageConsole) existing[i];
			}
		}
		MessageConsole console = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{console});
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);		    	    
		return console;	   
	}
	
	protected void runJob(final JobListener listener, 
			final String jobName, final String command, 
			final boolean closeOnEnd, final String... params
			){
		try{
			runJob(new SSHControl(), listener, jobName, command, closeOnEnd, params);
		}catch(Exception ex){
			writeInDefaultConsole(ex.getMessage());
		}
	}
	
	protected void runJob(final SSHControl ssh, final JobListener listener, 
			final String jobName, final String command, 
			final boolean closeOnEnd, final String... params
			){
		Job job = new Job(jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try{										
					ssh.setListener(listener);
		    		ssh.connect();
		    		if( params == null ){
		    			ssh.execCommand(command);	
		    		}
		    		else{
		    			ssh.execCommand(command, params);
		    		}
				}catch(Exception ex){
					ex.printStackTrace();
				}finally{
					if( closeOnEnd ){
						ssh.close();
					}
					if( listener != null ){
						listener.onEnd(jobName);
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
		
	}
	
	protected void runJobCommandId(final JobListener listener, 
			String jobName, final String command, 
			final boolean closeOnEnd, final String... params){
		try{
			runJobCommandId(new SSHControl(), listener, jobName, command, closeOnEnd, params);
		}catch(Exception ex){
			writeInDefaultConsole(ex.getMessage());
		}
	}
	
	protected void runJobCommandId(SSHControl ssh, final JobListener listener, 
			String jobName, final String command, 
			final boolean closeOnEnd, final String... params){
		try{
			String comm = ResourcesUtil.getInstance().getProperty(command);
			runJob(ssh, listener, jobName, comm, closeOnEnd, params);
		}catch(Exception ex){
			writeInDefaultConsole(ex.getMessage());
		}
	}
	
	protected void createFolder(IProject project, String path)throws Exception{
		if( project == null || path == null )return;
		IFolder folder = project.getFolder(path);
		if( folder != null && !folder.exists() ){
			folder.create(true, true, null);
		}
	}
	
	protected void createFile(IProject project, String path, String content, String... params)throws Exception{
		if( project == null || path == null )return;			
		IFile srcFile = project.getFile(path);
		String realContent = MessageFormat.format(content, (Object[])params);
		realContent = realContent.replace("\\", "/");
		InputStream source = new ByteArrayInputStream(realContent.getBytes());
		if( srcFile.exists() ){
			srcFile.delete(true, null);
		}
		srcFile.create(source, false, null);
	}
	
	protected String findProcess(String processName)throws Exception{
		SSHControl ssh = null;
		try{
			ssh = new SSHControl();
			ssh.connect();
			return ssh.exec("command.find.process", processName, "{_id}", "'{print $2}'");
		}catch(Exception ex){
			throw ex;
		}finally{
			if( ssh != null && ssh.isConnected() ){
				ssh.close();
			}
		}			
	}
	
}