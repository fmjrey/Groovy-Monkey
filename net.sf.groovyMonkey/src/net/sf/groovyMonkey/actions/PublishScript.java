/*******************************************************************************
 * Copyright (c) 2005 Eclipse Foundation
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
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PUBLISH_AFTER_MARKER;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PUBLISH_BEFORE_MARKER;
import static net.sf.groovyMonkey.dom.Utilities.getContents;
import static net.sf.groovyMonkey.util.ListUtils.array;
import static org.eclipse.jface.dialogs.MessageDialog.openInformation;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class PublishScript
implements IWorkbenchWindowActionDelegate, IObjectActionDelegate
{
    private Shell shell;
    private ISelection selection;

    public PublishScript() {}

    protected List< IFile > getScripts()
    {
    	final List< IFile > scripts = array();
    	if( !( selection instanceof IStructuredSelection ) )
    		return scripts;
    	final IStructuredSelection sel = ( IStructuredSelection )selection;
        final List selectedObjects = sel.toList();
        for( final Object object : selectedObjects )
			if( object instanceof IFile )
				scripts.add( ( IFile )object );
    	return scripts;
    }
    public void run( final IAction action )
    {
        String result = "";
        final List< IFile > scripts = getScripts();
        for( final IFile script : scripts )
        {
            try
            {
                final String contents = getContents( script );
                result += decorateText( contents );
            }
            catch( final IOException x )
            {
                openInformation( shell, "Groovy Monkey", x.toString() + " while trying to copy script for publication" );
            }
            catch( final CoreException x )
            {
                openInformation( shell, "Groovy Monkey", x.toString() + " while trying to copy script for publication" );
            }
        }
        final Clipboard clipboard = new Clipboard( shell.getDisplay() );
        try
        {
            final TextTransfer textTransfer = TextTransfer.getInstance();
            clipboard.setContents( new Object[]{ result }, new Transfer[]{ textTransfer } );
        }
        finally
        {
            clipboard.dispose();
        }
    }
    protected String decorateText( final String contents )
    {
        return PUBLISH_BEFORE_MARKER + "\n" + contents + "\n" + PUBLISH_AFTER_MARKER;
    }
    public void selectionChanged( final IAction action,
                                  final ISelection selection )
    {
        this.selection = selection;
    }
    public void dispose()
    {
    }
    public void init( final IWorkbenchWindow window )
    {
        shell = window.getShell();
    }
    public void setActivePart( final IAction action,
                               final IWorkbenchPart targetPart )
    {
        shell = targetPart.getSite().getShell();
    }
}
