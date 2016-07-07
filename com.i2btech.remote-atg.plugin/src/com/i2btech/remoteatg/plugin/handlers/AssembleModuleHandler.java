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

public class AssembleModuleHandler extends BaseHandler implements JobListener{
	
	private String consoleId;
	
	private String instanceName;
	
	private String modulesList;
	
	private PreferencesUtil preferences;
	
	public AssembleModuleHandler(){
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
        		String instanceId = event.getCommand().getParameter("instanceId").getName();
        		instanceName = ResourcesUtil.getInstance().getProperty("atg.instance.".concat(instanceId).concat(".name"));
        		
        		String preferenceKey = instanceName+"-"+projectName;
        		String modules = preferences.get(preferenceKey);
        		
	            String defaultModules = modules == null ? projectName : modules;
	            InputDialog inputDialog = new InputDialog(window.getShell(), "ATG runAssembler", "Modules list (Write modules separated with space.\nExample: module1 module2", defaultModules, null);
	            
				if (inputDialog.open() != Window.OK) {
					return null;
				}
				
				modulesList = inputDialog.getValue();				
				consoleId = ResourcesUtil.getInstance().getProperty("atg.instance.".concat(instanceId).concat(".consoleId"));
				writeInConsole(consoleId, "Assembling "+instanceName+" with modules "+modulesList+", please wait...");
				runJobCommandId(this, "assembler_remove_job", "command.assemble.remove", true, instanceName);
				
				preferences.put(preferenceKey, modulesList);
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
					writeInConsole(consoleId, str);
				}catch(Exception ex){ex.printStackTrace();}
			}
		});	
	}

	@Override
	public void onEnd(String jobName) {
		switch( jobName ){
		case "assembler_remove_job":
			runJobCommandId(this, "assembler_cimEars_remove_job", "command.cimEars.remove", true, new String[]{instanceName});
			break;
		case "assembler_cimEars_remove_job":
			runJobCommandId(this, "assembler_module_job", "command.assemble.module", true, new String[]{instanceName, instanceName, modulesList});
			break;		
		default:
			write("Module "+instanceName+" has been assembled.");
			break;
		}
	}
	
}
