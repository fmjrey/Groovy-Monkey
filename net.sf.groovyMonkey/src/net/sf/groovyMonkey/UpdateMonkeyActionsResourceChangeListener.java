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

package net.sf.groovyMonkey;

import java.io.IOException;
import java.io.InputStream;

import net.sf.groovyMonkey.actions.RecreateMonkeyMenuAction;
import net.sf.groovyMonkey.dom.Utilities;
import net.sf.groovyMonkey.lang.IMonkeyScriptFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class UpdateMonkeyActionsResourceChangeListener implements
		IResourceChangeListener {
	public void resourceChanged(IResourceChangeEvent event) {
		final Boolean changes[] = new Boolean[1];
		changes[0] = new Boolean(false);
		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
			private void found_a_change() {
				changes[0] = new Boolean(true);
			}

			public boolean visit(IResourceDelta delta) {
                if( !( delta.getResource() instanceof IFile ) )
                    return true;
				String fullPath = delta.getFullPath().toString();
				if (Utilities.isMonkeyScript( fullPath )) {
					IFile file = (IFile) delta.getResource();
					switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						processNewOrChangedScript(fullPath, file);
						found_a_change();
						break;
					case IResourceDelta.REMOVED:
						processRemovedScript(fullPath, file);
						found_a_change();
						break;
					case IResourceDelta.CHANGED:
						if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
							processRemovedScript(delta.getMovedFromPath()
									.toString(), file);
							processNewOrChangedScript(fullPath, file);
							found_a_change();
						}
						if ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
							processRemovedScript(fullPath, file);
							processNewOrChangedScript(delta.getMovedToPath()
									.toString(), file);
							found_a_change();
						}
						if ((delta.getFlags() & IResourceDelta.REPLACED) != 0) {
							processNewOrChangedScript(fullPath, file);
							found_a_change();
						}
						if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
							processNewOrChangedScript(fullPath, file);
							found_a_change();
						}
						break;
					}
				}
				return true;
			}
		};
		try {
			event.getDelta().accept(visitor);
		} catch (CoreException x) {
			// log an error in the error log
		}
		boolean anyMatches = ((Boolean) (changes[0])).booleanValue();
		if (anyMatches) {
			createTheMonkeyMenu();
		}
	}

	private void processNewOrChangedScript(String name, IFile file) {
		StoredScript store = new StoredScript();
		store.scriptFile = file;
		try {
			store.metadata = getMetadataFrom(file);
		} catch (CoreException x) {
			store.metadata = new ScriptMetadata();
			// log an error in the error log
		} catch (IOException x) {
			store.metadata = new ScriptMetadata();
			// log an error in the error log
		}
		EclipseMonkeyPlugin.getDefault().addScript(name, store);
        for( final IMonkeyScriptFactory factory : RunMonkeyScript.getFactories().values() )
            factory.changed( file );
	}

	private void processRemovedScript(String name, IFile file) {
		EclipseMonkeyPlugin.getDefault().removeScript(name);
        for( final IMonkeyScriptFactory factory : RunMonkeyScript.getFactories().values() )
            factory.changed( file );
	}

	public void rescanAllFiles() {
		EclipseMonkeyPlugin.getDefault().clearScripts();
        for( final IMonkeyScriptFactory factory : RunMonkeyScript.getFactories().values() )
            factory.clearCachedScripts();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		for (int i = 0; i < workspace.getRoot().getProjects().length; i++) {
			IProject project = workspace.getRoot().getProjects()[i];
			IFolder folder = project.getFolder("monkey");
			if (folder == null)
				continue;
			try {
				for (int j = 0; j < folder.members().length; j++) {
					IResource resource = folder.members()[j];
					if (resource instanceof IFile) {
						IFile file = (IFile) resource;
						if (Utilities.isMonkeyScript( file ) ) {
							processNewOrChangedScript(file.getFullPath().toString(), file);
						}
					}
				}
			} catch (CoreException x) {
				// ignore folders we cannot access
			}
		}
	}

	private String getFileContents(IFile file) throws CoreException,
			IOException {
		InputStream in = null;
		try {
			in = file.getContents();
			byte[] buf = new byte[100000];
			int count = in.read(buf);
			if (count <= 0)
				return "";
			byte[] buf2 = new byte[count];
			for (int k = 0; k < count; k++) {
				buf2[k] = buf[k];
			}
			return new String(buf2);
		} finally {
			if (in != null)
				in.close();
		}
	}

	private ScriptMetadata getMetadataFrom(IFile file) throws CoreException,
			IOException {
		String contents = getFileContents(file);
		ScriptMetadata metadata = ScriptMetadata.getScriptMetadata(contents);
		metadata.setFile(file);
		return metadata;
	}

	public static void createTheMonkeyMenu() {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			final IWorkbenchWindow window = windows[i];
			window.getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					RecreateMonkeyMenuAction action = new RecreateMonkeyMenuAction();
					action.init(window);
					action.run(null);
				}
			});
		}
	}

}
