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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.mozilla.javascript.Scriptable;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipseMonkeyPlugin extends AbstractUIPlugin implements IStartup {
    public static final String PLUGIN_ID = "net.sf.groovyMonkey";
	public static final String PUBLISH_BEFORE_MARKER = "--- Came wiffling through the eclipsey wood ---";

	public static final String PUBLISH_AFTER_MARKER = "--- And burbled as it ran! ---";

	// The shared instance.
	private static EclipseMonkeyPlugin plugin;
	private static BundleContext context;
    
	private final Map< String, StoredScript > scriptStore = new HashMap< String, StoredScript >();

	public Map< String, StoredScript > getScriptStore() {
		return scriptStore;
	}

    public static Map< String, StoredScript > scriptStore()
    {
        return getDefault().getScriptStore();
    }
    
	private final Map< String, Scriptable > scopeStore = new HashMap< String, Scriptable >();

	public Map< String, Scriptable > getScopeStore() {
		return scopeStore;
	}
    
    public static Map< String, Scriptable > scopeStore()
    {
        return getDefault().getScopeStore();
    }

	public EclipseMonkeyPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
        EclipseMonkeyPlugin.context = context;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static EclipseMonkeyPlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(
				PLUGIN_ID, path);
	}

	public void earlyStartup() {
		UpdateMonkeyActionsResourceChangeListener listener = new UpdateMonkeyActionsResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
				IResourceChangeEvent.POST_CHANGE);
		listener.rescanAllFiles();
		UpdateMonkeyActionsResourceChangeListener.createTheMonkeyMenu();
	}

	public void addScript( final String name, 
                           final StoredScript script) 
    {	
		final Map< String, StoredScript > store = getScriptStore();
		final StoredScript oldScript = (StoredScript)store.get(name);
		if (oldScript != null) {
			oldScript.metadata.unsubscribe();
		}
		store.put(name, script);
		script.metadata.subscribe();		
	}

	public void removeScript(String name) {
		Map store = getScriptStore();
		StoredScript oldScript = (StoredScript)store.remove(name);
		if (oldScript == null) return;
		oldScript.metadata.unsubscribe();		
	}

	public void clearScripts() {
		getScriptStore().clear();
		//TODO unsubscribe
	}
    public static ImageRegistry registry()
    {
        return getDefault().getImageRegistry();
    }
    public static Image icon( final String name )
    {
        final Image image = registry().get( name );
        if( image != null )
            return image;
        registry().put( name, EclipseMonkeyPlugin.getImageDescriptor( "icons/" + name ) );
        return registry().get( name );
    }
    public static BundleContext context()
    {
        return context;
    }
}
