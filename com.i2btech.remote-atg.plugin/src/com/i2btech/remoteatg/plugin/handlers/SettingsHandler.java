package com.i2btech.remoteatg.plugin.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.i2btech.remoteatg.plugin.util.PreferencesUtil;

public class SettingsHandler extends BaseHandler{

	private PreferencesUtil preferences;
	
	public SettingsHandler(){
		preferences = new PreferencesUtil();
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try{
		
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			
			String atgLocalPath = preferences.get("atgLocalPath");
			
			InputDialog inputDialog = new InputDialog(window.getShell(), "I2BCommerce Settings", "Please, enter your LOCAL directory of ATG installation", atgLocalPath, null);		
			if (inputDialog.open() != Window.OK) {
				return null;
			}
			
			preferences.put("atgLocalPath", inputDialog.getValue());
			preferences.flush();
		
		}catch(Exception ex){
			writeInDefaultConsole(ex.getMessage());
		}
		
		return null;
	}
	
}
