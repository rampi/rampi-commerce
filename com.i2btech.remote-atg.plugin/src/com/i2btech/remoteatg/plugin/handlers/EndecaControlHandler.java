package com.i2btech.remoteatg.plugin.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.i2btech.remoteatg.plugin.event.JobListener;
import com.i2btech.remoteatg.plugin.util.PreferencesUtil;
import com.i2btech.remoteatg.plugin.util.ResourcesUtil;

public class EndecaControlHandler extends BaseHandler implements JobListener{

	private PreferencesUtil preferences;
	
	public EndecaControlHandler(){
		preferences = new PreferencesUtil();
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
        Object firstElement = selection.getFirstElement();
        if (firstElement instanceof IAdaptable)
        {
        	try{
        		        		
        		IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
 	            String projectName = project.getName();
 	             	           
 	            String action = event.getCommand().getParameter("action").getName();
 	            
        		String controlPath = preferences.get(projectName);
        		controlPath = controlPath == null ? ResourcesUtil.getInstance().getProperty("endeca.control.path.default") : controlPath;        		 
        		
				InputDialog inputDialog = new InputDialog(
						window.getShell(), "Endeca Control", 
						"Please, enter the full path for Endeca App control folder", 
						controlPath, null
				);
				
				if (inputDialog.open() != Window.OK) {
					return null;
				}
				
				controlPath = inputDialog.getValue();
				
				runJobCommandId(this, action, "set_templates".equals(action) ?  "endeca.control.setTemplates" : "endeca.control.promoteContent", true, controlPath);
				
				preferences.put(projectName, controlPath);
				preferences.flush();
				
        	}catch(Exception ex){
        		ex.printStackTrace();
        	}
        }
		
		return null;
	}

	@Override
	public void write(final String str) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try{
					writeInConsole(ManageEndecaHandler.ENDECA_CONSOLE, str);
				}catch(Exception ex){ex.printStackTrace();}
			}
		});	
	}

	@Override
	public void onEnd(String jobName) {
		
	}
	
}
