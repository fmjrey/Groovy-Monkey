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
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.mozilla.javascript.Scriptable;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class GroovyMonkeyPlugin 
extends AbstractUIPlugin 
implements IStartup
{
    public static final String PLUGIN_ID = "net.sf.groovyMonkey";
    public static final String PUBLISH_BEFORE_MARKER = "--- Came wiffling through the eclipsey wood ---";
    public static final String PUBLISH_AFTER_MARKER = "--- And burbled as it ran! ---";
    private static GroovyMonkeyPlugin plugin;
    private static BundleContext context;
    private final Map< String, StoredScript > scriptStore = new HashMap< String, StoredScript >();
    private final Map< String, Scriptable > scopeStore = new HashMap< String, Scriptable >();
    private ServiceTracker tracker = null;
    
    public Map< String, StoredScript > getScriptStore()
    {
        return scriptStore;
    }
    public static Map< String, StoredScript > scriptStore()
    {
        return getDefault().getScriptStore();
    }
    public Map< String, Scriptable > getScopeStore()
    {
        return scopeStore;
    }
    public static Map< String, Scriptable > scopeStore()
    {
        return getDefault().getScopeStore();
    }
    public GroovyMonkeyPlugin()
    {
        plugin = this;
    }
    @Override
    public void start( final BundleContext context ) 
    throws Exception
    {
        super.start( context );
        GroovyMonkeyPlugin.context = context;
        tracker = new ServiceTracker( context, PlatformAdmin.class.getName(), null );
        tracker.open();
    }
    @Override
    public void stop( final BundleContext context )
    throws Exception
    {
        super.stop( context );
        plugin = null;
        if( tracker != null )
            tracker.close();
    }
    public static GroovyMonkeyPlugin getDefault()
    {
        return plugin;
    }
    public static ImageDescriptor getImageDescriptor( final String path )
    {
        return imageDescriptorFromPlugin( PLUGIN_ID, path );
    }
    public void earlyStartup()
    {
        final UpdateMonkeyActionsResourceChangeListener listener = new UpdateMonkeyActionsResourceChangeListener();
        getWorkspace().addResourceChangeListener( listener, IResourceChangeEvent.POST_CHANGE );
        listener.rescanAllFiles();
        UpdateMonkeyActionsResourceChangeListener.createTheMonkeyMenu();
    }
    public void addScript( final String name, 
                           final StoredScript script )
    {
        final Map< String, StoredScript > store = getScriptStore();
        final StoredScript oldScript = store.get( name );
        if( oldScript != null )
            oldScript.metadata.unsubscribe();
        store.put( name, script );
        script.metadata.subscribe();
    }
    public void removeScript( final String name )
    {
        final Map store = getScriptStore();
        final StoredScript oldScript = ( StoredScript )store.remove( name );
        if( oldScript == null )
            return;
        oldScript.metadata.unsubscribe();
    }
    public void clearScripts()
    {
        getScriptStore().clear();
        // TODO unsubscribe
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
        registry().put( name, getImageDescriptor( "icons/" + name ) );
        return registry().get( name );
    }
    public static BundleContext context()
    {
        return context;
    }
    public static PlatformAdmin getPlatformAdmin()
    {
        return ( PlatformAdmin )getDefault().tracker.getService();
    }
    public static PlatformAdmin platformAdmin()
    {
        return getPlatformAdmin();
    }
    public static BundleDescription getBundleDescription()
    {
        final PlatformAdmin admin = getPlatformAdmin();
        return admin.getState().getBundle( getDefault().getBundle().getBundleId() );
    }
    public static BundleDescription bundleDescription()
    {
        return getBundleDescription();
    }
    public static List< String > getRequiredBundles()
    {
        final List< String > list = new ArrayList< String >();
        final BundleDescription description = bundleDescription();
        for( final BundleSpecification required : description.getRequiredBundles() )
            list.add( required.getName() );
        return list;
    }
}
