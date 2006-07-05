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
package net.sf.groovyMonkey.dom;
import static java.util.Collections.synchronizedMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.sf.groovyMonkey.GroovyMonkeyPlugin;
import net.sf.groovyMonkey.ErrorDialog;
import net.sf.groovyMonkey.ScriptMetadata;
import net.sf.groovyMonkey.internal.DynamicState;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class Utilities {

	public static String getFileContents(IFile file) throws CoreException,
			IOException {
		final int BUF_SIZE = 100000;
		InputStream in = null;
		try {
			in = file.getContents();
			StringBuffer result = new StringBuffer();
			while (true) {
				byte[] buf = new byte[BUF_SIZE];
				int count = in.read(buf);
				if (count <= 0)
					return result.toString();
				byte[] buf2 = new byte[count];
				for (int k = 0; k < count; k++) {
					buf2[k] = buf[k];
				}
				result.append(new String(buf2));
			}
		} finally {
			if (in != null)
				in.close();
		}
	}


	private static IDynamicState _state = new DynamicState();

	public static IDynamicState state() {
		return _state;
	}


	public static final String SCRIPT_NAME = "scriptName";
    
    public static boolean isMonkeyScript( final IFile file )
    {
        return isMonkeyScript( file.getFullPath() );
    }
    public static boolean isMonkeyScript( final IPath path )
    {
        return isMonkeyScript( path.toString() );
    }
    public static boolean isMonkeyScript( final String fullPath )
    {
        if( isJavaScript( fullPath ) )
            return true;
        return false;
    }
    public static boolean isJavaScript( final IFile file )
    {
        return isJavaScript( file.getFullPath() );
    }
    public static boolean isJavaScript( final IPath path )
    {
        return isJavaScript( path.toString() );
    }
    public static boolean isJavaScript( final String fullPath )
    {
        return fullPath.endsWith( ".em" );
    }
    public static String key( final IFile file )
    {
    	return file != null ? file.getFullPath().toString() : null;
    }
    public static Map< String, Object > getExtensionGlobalVariables( final ScriptMetadata metadata ) 
    {
        final IExtensionPoint point = getDOMExtensionPoint();
        final Map< String, Object > vars = synchronizedMap( new LinkedHashMap< String, Object >() );
        if( point == null )
            return vars;
        if( Display.getCurrent() == null )
        {
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    getExtensionGlobalVariables( metadata, point, vars );
                }
            };
            Display.getDefault().syncExec( runnable );
            return vars;
        }
        getExtensionGlobalVariables( metadata, point, vars );
        return vars;
    }
    private static void getExtensionGlobalVariables( final ScriptMetadata metadata, final IExtensionPoint point, final Map< String, Object > vars )
    {
        final IExtension[] extensions = point.getExtensions();
        for( int i = 0; i < extensions.length; i++ )
        {
            final IExtension extension = extensions[ i ];
            final IConfigurationElement[] configurations = extension.getConfigurationElements();
            for( int j = 0; j < configurations.length; j++ )
            {
                final IConfigurationElement element = configurations[ j ];
                if( !element.getName().equals( "dom" ) )
                    continue;
                try
                {
                    final IExtension declaring = element.getDeclaringExtension();
                    final String declaring_plugin_id = declaring.getContributor().getName();
                    if( metadata.containsDOM_by_plugin( declaring_plugin_id ) )
                    {
                        final String variableName = element.getAttribute( "variableName" );
                        final IMonkeyDOMFactory factory = ( IMonkeyDOMFactory )element.createExecutableExtension( "class" );
                        final Object rootObject = factory.getDOMroot();
                        vars.put( variableName, rootObject );
                    }
                }
                catch( final Exception x )
                {
                    // ignore bad extensions
                }
            }
        }
    }
    public static boolean hasDOM( final String pluginID )
    {
        final Map< String, Object > dom = getDOM( pluginID );
        if( dom == null || dom.size() == 0 )
            return false;
        return true;
    }
    public static List< String > getDOMPlugins()
    {
        final List< String > plugins = new ArrayList< String >();
        final IExtensionPoint point = getDOMExtensionPoint();
        if( point == null )
            return plugins;
        final IExtension[] extensions = point.getExtensions();
        if( extensions == null )
            return plugins;
        for( final IExtension extension : extensions )
            plugins.add( extension.getContributor().getName() );
        return plugins;
    }
    public static Map< String, Object > getDOM( final String pluginID ) 
    {
        final IExtensionPoint point = getDOMExtensionPoint();
        final Map< String, Object > vars = new LinkedHashMap< String, Object >();
        if( point == null )
            return vars;
        final IExtension[] extensions = point.getExtensions();
        if( extensions == null )
            return vars;
        for( int i = 0; i < extensions.length; i++ )
        {
            final IExtension extension = extensions[ i ];
            final IConfigurationElement[] configurations = extension.getConfigurationElements();
            for( int j = 0; j < configurations.length; j++ )
            {
                final IConfigurationElement element = configurations[ j ];
                if( !element.getName().equals( "dom" ) )
                    continue;
                try
                {
                    final IExtension declaring = element.getDeclaringExtension();
                    final String declaring_plugin_id = declaring.getContributor().getName();
                    if( pluginID.trim().equals( declaring_plugin_id.trim() ) )
                    {
                        final String variableName = element.getAttribute( "variableName" );
                        final IMonkeyDOMFactory factory = ( IMonkeyDOMFactory )element.createExecutableExtension( "class" );
                        final Object rootObject = factory.getDOMroot();
                        vars.put( variableName, rootObject );
                    }
                }
                catch( final InvalidRegistryObjectException x )
                {
                    // ignore bad extensions
                }
                catch( final CoreException x )
                {
                    // ignore bad extensions
                }
            }
        }
        return vars;
    }
    public static String getUpdateSiteForDOMPlugin( final String pluginID ) 
    {
        final IExtensionPoint point = getDOMExtensionPoint();
        if( point == null )
            return "";
        final IExtension[] extensions = point.getExtensions();
        if( extensions == null )
            return "";
        for( int i = 0; i < extensions.length; i++ )
        {
            final IExtension extension = extensions[ i ];
            if( !extension.getContributor().getName().equals( pluginID ) )
                continue;
            final IConfigurationElement[] configurations = extension.getConfigurationElements();
            for( int j = 0; j < configurations.length; j++ )
            {
                final IConfigurationElement element = configurations[ j ];
                if( !element.getName().equals( "updateSite" ) )
                    continue;
                return element.getAttribute( "url" );
            }
        }
        return "";
    }
    private static IExtensionPoint getDOMExtensionPoint()
    {
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        final IExtensionPoint point = registry.getExtensionPoint( "net.sf.groovyMonkey.dom" );
        return point;
    }
    public static void error( final String title, 
                              final String message,
                              final Throwable exception )
    {
        if( Display.getCurrent() == null )
        {
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    error( title, message, exception );
                }
            };
            Display.getDefault().asyncExec( runnable );
            return;
        }
        final ErrorDialog dialog = new ErrorDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
                                                    title, 
                                                    message, 
                                                    new Status( IStatus.ERROR, GroovyMonkeyPlugin.PLUGIN_ID, IStatus.ERROR, message, exception ),
                                                    IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR );
        dialog.open();
    }
}
