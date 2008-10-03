/*******************************************************************************
 * Copyright (c) 2005, 2006 Eclipse Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bjorn Freeman-Benson - initial implementation
 *     Ward Cunningham - initial implementation
 *******************************************************************************/
package net.sf.groovyMonkey.actions;
import net.sf.groovyMonkey.wizard.NewScriptWizard;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class NewGroovyMonkeyScriptAction 
implements IWorkbenchWindowActionDelegate 
{
	private IWorkbenchWindow window;

	public NewGroovyMonkeyScriptAction() {}

	public void run( final IAction action ) 
    {
	    final NewScriptWizard wizard = new NewScriptWizard();
	    final WizardDialog dialog = new WizardDialog( window.getShell(), wizard );
	    dialog.create();
	    dialog.open();
	}
	public void selectionChanged( final IAction action, 
                                  final ISelection selection )
    {
    }
    public void dispose()
    {
    }
    public void init( final IWorkbenchWindow window )
    {
        this.window = window;
    }
}