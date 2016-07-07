package com.i2btech.remoteatg.plugin.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.i2btech.remoteatg.plugin.event.JobListener;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class JRebelActivationHandler extends BaseHandler implements JobListener{
	/**
	 * The constructor.
	 */
	
	public JRebelActivationHandler() {
		
	}
	
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try{			
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			InputDialog inputDialog = new InputDialog(window.getShell(), "JRebel Licence", "Please, paste your licence key:", "", null);		
			if (inputDialog.open() != Window.OK) {
				return null;
			}
			String value = inputDialog.getValue();
			writeInDefaultConsole("Trying to register JRebel with key: "+value);
			runJobCommandId(this, "jrebel_activation", "command.activateMyJRebel", true, value);
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
					writeInDefaultConsole(str);
				}catch(Exception ex){ex.printStackTrace();}
			}
		});	
	}

	@Override
	public void onEnd(String jobName) {
		
	}
			
}