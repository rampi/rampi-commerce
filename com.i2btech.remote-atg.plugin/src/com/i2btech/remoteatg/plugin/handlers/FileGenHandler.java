package com.i2btech.remoteatg.plugin.handlers;

import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.i2btech.remoteatg.plugin.util.ResourcesUtil;

public class FileGenHandler extends BaseHandler{

	private String browseWsButtonSelected(Shell shell, String title, String message)throws Exception{		  
		
		ElementTreeSelectionDialog dialog=new ElementTreeSelectionDialog(
				shell,
				new WorkbenchLabelProvider(), 
				new BaseWorkbenchContentProvider()
		);
		
		dialog.setTitle(title);	  
		dialog.setMessage(message);	  
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());		  
	  
		if (dialog.open() == IDialogConstants.OK_ID ) {
			IResource resource=(IResource)dialog.getFirstResult();
			if( resource != null && resource.getProjectRelativePath() != null ){				
				return resource.getProjectRelativePath().toOSString();
			}
		}
	  
		throw new Exception("Exploded war not selected, try again");
		
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
        		
	    		//Generating WEB-INF/classes rebel.xml
	    		String explodedWarPath = browseWsButtonSelected(
	    				window.getShell(), 
	    				"Select", 
	    				"Please, select the exploded war directory, for example:\n MyModule/j2ee-apps/mymodule/mymodule.war"  
	    		);
        		
        		//Generating SRC rebel.xml
        		createFile(
        				project, ResourcesUtil.getInstance().getProperty("jrebel.xml.template.rebel.path"), 
        				ResourcesUtil.getInstance().getProperty("jrebel.xml.template.classes.content"), 
        				MessageFormat.format(ResourcesUtil.getInstance().getProperty("jrebel.xml.template.classes.path"), project.getName()),
        				MessageFormat.format(ResourcesUtil.getInstance().getProperty("jrebel.xml.template.config.path"), project.getName())        				
        		);
        			    		
	    		createFolder(project, explodedWarPath+"/WEB-INF/classes");
        		createFile(        				
        				project, explodedWarPath+"/WEB-INF/classes/rebel.xml", 
        				ResourcesUtil.getInstance().getProperty("jrebel.xml.template.war.content"),
        				ResourcesUtil.getInstance().getProperty("atg.modules.path")+project.getName()+"/"+explodedWarPath        				
        		);
	    		
        	}catch(Exception ex){
        		writeInDefaultConsole(ex.getMessage());
        	}
        }
		return null;
	}
	
}
