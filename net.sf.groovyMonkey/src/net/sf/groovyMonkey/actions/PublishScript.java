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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.sf.groovyMonkey.EclipseMonkeyPlugin;
import net.sf.groovyMonkey.dom.Utilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
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

public class PublishScript implements IWorkbenchWindowActionDelegate,
		IObjectActionDelegate {

	public PublishScript() {
	}

	public void run(IAction action) {
		String result = "";

		IStructuredSelection sel = (IStructuredSelection) this.selection;
		List selectedObjects = sel.toList();
		for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
			IFile element = (IFile) iter.next();

			try {
				String contents = Utilities.getFileContents(element);

				result += decorateText(contents);
			} catch (IOException x) {
				MessageDialog.openInformation(shell, "Eclipse Monkey", x
						.toString()
						+ " while trying to copy script for publication");
			} catch (CoreException x) {
				MessageDialog.openInformation(shell, "Eclipse Monkey", x
						.toString()
						+ " while trying to copy script for publication");
			}
		}

		Clipboard clipboard = new Clipboard(shell.getDisplay());
		try {
			TextTransfer textTransfer = TextTransfer.getInstance();
			clipboard.setContents(new Object[] { result },
					new Transfer[] { textTransfer });
		} finally {
			clipboard.dispose();
		}
	}

	protected String decorateText(String contents) {
		return EclipseMonkeyPlugin.PUBLISH_BEFORE_MARKER
		+ "\n" + contents + "\n"
		+ EclipseMonkeyPlugin.PUBLISH_AFTER_MARKER;
	}

	private ISelection selection;

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		shell = window.getShell();
	}

	private Shell shell;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}
}