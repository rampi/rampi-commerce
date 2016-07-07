package com.i2btech.remoteatg.plugin.handlers;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.i2btech.remoteatg.plugin.util.OSUtil;
import com.i2btech.remoteatg.plugin.util.PreferencesUtil;
import com.i2btech.remoteatg.plugin.util.ResourcesUtil;

public class ManageACCHandler extends BaseHandler{

	private PreferencesUtil preferences;
	
	public ManageACCHandler(){
		preferences = new PreferencesUtil();
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		try{
			String atgLocalPath = preferences.get("atgLocalPath");
			if( atgLocalPath != null && !atgLocalPath.isEmpty() ){
				File f = new File(atgLocalPath);
				if( f.exists() && f.isDirectory() ){
					String path = atgLocalPath;
					String prefix = "";
					if( OSUtil.isWindows() ){
						prefix = ResourcesUtil.getInstance().getProperty("windows.shell.run.prefix");
						path = path.concat(ResourcesUtil.getInstance().getProperty("windows.atg.acc.path"));
					}else if( OSUtil.isUnix() || OSUtil.isMac() ){
						prefix = ResourcesUtil.getInstance().getProperty("unix.shell.run.prefix");
						path = path.concat(ResourcesUtil.getInstance().getProperty("unix.atg.acc.path"));
					}
					if( new File(path).exists() ){
						writeInDefaultConsole("Starting ACC "+prefix.concat(path));
						Runtime.getRuntime().exec(prefix.concat(path));						
					}else{
						MessageDialog.openError(
								window.getShell(), 
								"Error", 
								"Invalid ATG path, go to \"Settings\" and enter the ATG LOCAL installation path."
						);
					}
				}				
			}else{				
				MessageDialog.openError(						
						window.getShell(), 
						"Error", 
						"ATG path not specified or not valid, go to \"Settings\" and enter the ATG LOCAL installation path."
				);
			}			
		}catch(Exception ex){
			writeInDefaultConsole(ex.getMessage());
		}
		return null;
	}
	
}