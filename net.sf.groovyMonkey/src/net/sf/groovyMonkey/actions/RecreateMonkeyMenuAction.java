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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.groovyMonkey.GroovyMonkeyPlugin;
import net.sf.groovyMonkey.RunMonkeyScript;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.StoredScript;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.internal.ActionSetContributionItem;
import org.eclipse.ui.internal.WorkbenchWindow;

public class RecreateMonkeyMenuAction implements IWorkbenchWindowActionDelegate {

	public RecreateMonkeyMenuAction() {
	}

	public void run(IAction action) {
		clearTheMenu();
		final List< ScriptMetadata > metaDatas = getAllMetadatas();
		final List< Association > menuData = createMenuFromMetadatas(metaDatas);
		createTheMenu(menuData, action);
	}

	private List< ScriptMetadata > getAllMetadatas() 
    {
		final List< ScriptMetadata > result = new ArrayList< ScriptMetadata >();
		Iterator iter = GroovyMonkeyPlugin.getDefault().getScriptStore().values().iterator();
		for (; iter.hasNext();) {
			StoredScript element = (StoredScript) iter.next();
			result.add(element.metadata);
		}
		return result;
	}

	private void clearTheMenu() {
		MenuManager manager = ((WorkbenchWindow) window).getMenuManager();
        if( manager == null )
            return;
		IContributionItem two = manager.findUsingPath("eclipsemonkeyMenu");
		IMenuManager three = (IMenuManager) ((ActionSetContributionItem) two)
				.getInnerItem();
		three.removeAll();
	}

	private Pattern submenu_pattern = Pattern.compile("^(.+?)>(.*)$");

	class MonkeyMenuStruct {
		String key;

		IMenuManager menu;

		MonkeyMenuStruct submenu;
	}

	private void createTheMenu( final List< Association > menuData, 
                                final IAction action ) 
    {
		MenuManager outerManager = ((WorkbenchWindow) window).getMenuManager();
        if( outerManager == null )
            return;
		IContributionItem contribution = outerManager
				.findUsingPath("eclipsemonkeyMenu");
		IMenuManager menuManager = (IMenuManager) ((ActionSetContributionItem) contribution)
				.getInnerItem();

		MonkeyMenuStruct current = new MonkeyMenuStruct();
		current.key = "";
		current.menu = menuManager;
		current.submenu = new MonkeyMenuStruct();

		final SortedSet< Association > sorted = new TreeSet< Association >();
		sorted.addAll(menuData);

		Iterator iter = sorted.iterator();
		while (iter.hasNext()) {
			Association element = (Association) iter.next();
			String menu_string = element.key;
			final IFile script_file_to_run = element.file;
			addNestedMenuAction(current, menu_string, script_file_to_run);
		}

		final IWorkbenchWindow _window = this.window;

		if (sorted.size() != 0)
			menuManager.add(new Separator());

		menuManager.add(new Action("Paste New Script") {
			public void run() {
				IWorkbenchWindowActionDelegate delegate = new PasteScriptFromClipboardAction();
				delegate.init(_window);
				delegate.run(action);
			}
		});

		if (sorted.size() == 0) {
			menuManager.add(new Action("Examples") {
				public void run() {
					IWorkbenchWindowActionDelegate delegate = new CreateMonkeyExamplesAction();
					delegate.init(_window);
					delegate.run(action);
				}
			});
		}
		
		menuManager.updateAll(true);

	}

	private void addNestedMenuAction(MonkeyMenuStruct current,
			String menu_string, final IFile script_file_to_run) {
		if( menu_string == null ) return;
		Matcher match = submenu_pattern.matcher(menu_string);
		if (match.find()) {
			String primary_key = match.group(1).trim();
			String secondary_key = match.group(2).trim();
			if (!primary_key.equals(current.submenu.key)) {
				IMenuManager submenu = new MenuManager(primary_key);
				current.menu.add(submenu);
				current.submenu.menu = submenu;
				current.submenu.key = primary_key;
				current.submenu.submenu = new MonkeyMenuStruct();
			}
			addNestedMenuAction(current.submenu, secondary_key,
					script_file_to_run);
		} else {
			current.menu.add(menuAction(menu_string, script_file_to_run));
		}
	}

	private Action menuAction(String key, final IFile value) {
		final RunMonkeyScript runner = new RunMonkeyScript(value, window);
		Action action = new Action(key) {
			public void run() {
				runner.run( false );
			}
		};
		if (RunMonkeyScript.last_run != null
				&& value.equals(RunMonkeyScript.last_run.scriptFile))
			action.setAccelerator(SWT.ALT | SWT.CONTROL | 'M');
		return action;
	}

	private List< Association > createMenuFromMetadatas(Collection metaDatas) {
		final List< Association > menuData = new ArrayList< Association >();
		for (Iterator iter = metaDatas.iterator(); iter.hasNext();) {
			ScriptMetadata data = (ScriptMetadata) iter.next();
			if( data.getMenuName() != null ) 
				menuData.add(new Association(data.getMenuName(), data.getFile()));
		}
		return menuData;
	}

	private static int id = 0;

	class Association implements Comparable {
		String key;

		IFile file;

		int uniqueId;

		Association(String k, IFile f) {
			this.key = k;
			this.file = f;
			this.uniqueId = id++;
		}

		public int compareTo(Object arg0) {
			Association b = (Association) arg0;
			int value = key.compareTo(b.key);
			if (value == 0) {
				if (uniqueId < b.uniqueId)
					return -1;
				else
					return 1;
			} else
				return value;
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	private IWorkbenchWindow window;
}