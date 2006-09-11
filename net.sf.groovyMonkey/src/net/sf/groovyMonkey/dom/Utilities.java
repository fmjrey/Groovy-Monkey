package net.sf.groovyMonkey.dom;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.context;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.getAllRequiredBundles;
import static net.sf.groovyMonkey.util.ListUtils.list;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.eclipse.core.resources.IResource.DEPTH_ONE;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.INFO;
import static org.eclipse.core.runtime.IStatus.OK;
import static org.eclipse.core.runtime.IStatus.WARNING;
import static org.eclipse.core.runtime.Platform.getExtensionRegistry;
import static org.eclipse.swt.widgets.Display.getCurrent;
import static org.eclipse.swt.widgets.Display.getDefault;
import static org.eclipse.ui.PlatformUI.getWorkbench;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.groovyMonkey.DOMDescriptor;
import net.sf.groovyMonkey.ErrorDialog;
import net.sf.groovyMonkey.ScriptMetadata;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;

public class Utilities 
{
    public static final String SCRIPT_NAME = "scriptName";
    public static final String DOM_EXTENSION_POINT_ID = PLUGIN_ID + ".dom";
    
    public static String contents( final IFile file ) 
    throws CoreException, IOException
    {
        return getContents( file );
    }
    public static String getContents( final IFile file ) 
    throws CoreException, IOException
    {
        if( file == null )
            return "";
        InputStream contents = null;
        try
        {
            if( !file.isSynchronized( DEPTH_ONE ) )
                file.refreshLocal( DEPTH_ONE, null );
            contents = file.getContents( true );
            return IOUtils.toString( contents );
        }
        finally
        {
            closeQuietly( contents );
        }
    }
    public static List< String > readLines( final Reader input ) 
    throws IOException 
    {
        final BufferedReader reader = new BufferedReader( input );
        final List< String > list = list();
        String line;
        while( ( line = reader.readLine() ) != null ) 
            list.add( line );
        return list;
    }
    public static List< String > readLines( final String string )
    {
        try
        {
            return readLines( new StringReader( string ) );
        }
        catch( final IOException e )
        {
            throw new RuntimeException( e );
        }
    }
    public static void setContents( final String contents, 
                                    final IFile file ) 
    throws CoreException
    {
        if( file == null )
            return;
        final byte[] bytes = contents != null ? contents.getBytes() : "".getBytes();
        try
        {
            file.setContents( new ByteArrayInputStream( bytes ), true, false, null );
        }
        finally
        {
            file.refreshLocal( DEPTH_ONE, null );
        }
    }
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
        return fullPath.endsWith( FILE_EXTENSION );
    }
    public static String key( final IFile file )
    {
    	return file != null ? file.getFullPath().toString() : null;
    }
    public static Set< String > getAllAvailableBundles()
    {
        final Set< String > set = new TreeSet< String >();
        final Set< String > defaultBundles = getAllRequiredBundles();
        for( final Bundle bundle : context().getBundles() )
            if( !defaultBundles.contains( bundle.getSymbolicName() ) )
                set.add( bundle.getSymbolicName() );
        return set;
    }
    public static Map< String, Object > getExtensionGlobalVariables( final ScriptMetadata metadata ) 
    {
        final IExtensionPoint point = getDOMExtensionPoint();
        final Map< String, Object > vars = new LinkedHashMap< String, Object >();
        if( point == null )
            return vars;
        if( getCurrent() == null )
        {
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    getExtensionGlobalVariables( metadata, point, vars );
                }
            };
            getDefault().syncExec( runnable );
            return vars;
        }
        getExtensionGlobalVariables( metadata, point, vars );
        return vars;
    }
    @SuppressWarnings("deprecation")
    private static void getExtensionGlobalVariables( final ScriptMetadata metadata, 
                                                     final IExtensionPoint point, 
                                                     final Map< String, Object > vars )
    {
        final IExtension[] extensions = point.getExtensions();
        for( final IExtension extension : extensions )
        {
            final IConfigurationElement[] configurations = extension.getConfigurationElements();
            for( final IConfigurationElement element : configurations )
            {
                if( !element.getName().equals( "dom" ) )
                    continue;
                try
                {
                    final IExtension declaring = element.getDeclaringExtension();
                    final String declaring_plugin_id = declaring.getDeclaringPluginDescriptor().getUniqueIdentifier();
                    if( metadata.containsDOMByPlugin( declaring_plugin_id ) )
                    {
                        final String variableName = element.getAttribute( "variableName" );
                        final IMonkeyDOMFactory factory = ( IMonkeyDOMFactory )element.createExecutableExtension( "class" );
                        final Object rootObject = factory.getDOMroot();
                        final DOMDescriptor descriptor = metadata.getDOMByPlugin( declaring_plugin_id );
                        if( !StringUtils.equals( descriptor.pluginName, PLUGIN_ID ) && descriptor.map.containsKey( variableName ) )
                            vars.put( descriptor.map.get( variableName ), rootObject );
                        else
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
    @SuppressWarnings("deprecation")
    public static Set< String > getDOMPlugins()
    {
        final Set< String > plugins = new TreeSet< String >();
        final IExtensionPoint point = getDOMExtensionPoint();
        if( point == null )
            return plugins;
        final IExtension[] extensions = point.getExtensions();
        if( extensions == null )
            return plugins;
        for( final IExtension extension : extensions )
            plugins.add( extension.getDeclaringPluginDescriptor().getUniqueIdentifier() );
        return plugins;
    }
    @SuppressWarnings("deprecation")
    public static Map< String, Class > getDOMInfo( final String pluginID ) 
    {
        final IExtensionPoint point = getDOMExtensionPoint();
        final Map< String, Class > vars = new LinkedHashMap< String, Class >();
        if( point == null )
            return vars;
        final IExtension[] extensions = point.getExtensions();
        if( extensions == null )
            return vars;
        for( final IExtension extension : extensions )
        {
            final IConfigurationElement[] configurations = extension.getConfigurationElements();
            for( final IConfigurationElement element : configurations )
            {
                if( !element.getName().equals( "dom" ) )
                    continue;
                try
                {
                    final IExtension declaring = element.getDeclaringExtension();
                    final String declaring_plugin_id = declaring.getDeclaringPluginDescriptor().getUniqueIdentifier();
                    if( pluginID.trim().equals( declaring_plugin_id.trim() ) )
                    {
                        final String variableName = element.getAttribute( "variableName" );
                        final String resourceName = element.getAttribute( "resource" );
                        try
                        {
                            if( isNotBlank( resourceName ) )
                            {
                                vars.put( variableName, Class.forName( resourceName ) );
                                continue;
                            }
                        }
                        catch( final ClassNotFoundException e )
                        {
                            error( "Class Not Found", "Could not find named class: " + resourceName, e );
                        }
                        final IMonkeyDOMFactory factory = ( IMonkeyDOMFactory )element.createExecutableExtension( "class" );
                        final Object rootObject = factory.getDOMroot();
                        vars.put( variableName, rootObject.getClass() );
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
    @SuppressWarnings("deprecation")
    public static Map< String, Object > getDOM( final String pluginID ) 
    {
        final IExtensionPoint point = getDOMExtensionPoint();
        final Map< String, Object > vars = new LinkedHashMap< String, Object >();
        if( point == null )
            return vars;
        final IExtension[] extensions = point.getExtensions();
        if( extensions == null )
            return vars;
        for( final IExtension extension : extensions )
        {
            final IConfigurationElement[] configurations = extension.getConfigurationElements();
            for( final IConfigurationElement element : configurations )
            {
                if( !element.getName().equals( "dom" ) )
                    continue;
                try
                {
                    final IExtension declaring = element.getDeclaringExtension();
                    final String declaring_plugin_id = declaring.getDeclaringPluginDescriptor().getUniqueIdentifier();
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
    @SuppressWarnings("deprecation")
    public static String getUpdateSiteForDOMPlugin( final String pluginID ) 
    {
        final IExtensionPoint point = getDOMExtensionPoint();
        if( point == null )
            return "";
        final IExtension[] extensions = point.getExtensions();
        if( extensions == null )
            return "";
        for( final IExtension extension : extensions )
        {
            if( !extension.getDeclaringPluginDescriptor().getUniqueIdentifier().equals( pluginID ) )
                continue;
            final IConfigurationElement[] configurations = extension.getConfigurationElements();
            for( final IConfigurationElement element : configurations )
            {
                if( !element.getName().equals( "updateSite" ) )
                    continue;
                return element.getAttribute( "url" );
            }
        }
        return "";
    }
    public static IExtensionPoint getDOMExtensionPoint()
    {
        final IExtensionRegistry registry = getExtensionRegistry();
        return registry.getExtensionPoint( DOM_EXTENSION_POINT_ID );
    }
    public static void error( final String title, 
                              final String message,
                              final Throwable exception )
    {
        showDialog( title, message, exception, ERROR );
    }
    public static void warning( final String title, 
                                final String message,
                                final Throwable exception )
    {
        showDialog( title, message, exception, WARNING );
    }
    private static void showDialog( final String title, 
                                    final String message, 
                                    final Throwable exception,
                                    final int type )
    {
        if( getCurrent() == null )
        {
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    error( title, message, exception );
                }
            };
            getDefault().syncExec( runnable );
            return;
        }
        final ErrorDialog dialog = new ErrorDialog( getWorkbench().getActiveWorkbenchWindow().getShell(), 
                                                    title, 
                                                    message, 
                                                    new Status( type, PLUGIN_ID, type, defaultString( message ), exception ),
                                                    OK | INFO | WARNING | ERROR );
        dialog.open();
    }
    public static IWorkbenchWindow activeWindow()
    {
        return getWorkbench().getActiveWorkbenchWindow();
    }
    public static Shell shell()
    {
        return activeWindow().getShell();
    }
    public static IWorkbenchPage activePage()
    {
        return activeWindow().getActivePage();
    }
    public static ImageDescriptor imageDescriptor( final String symbolicName )
    {
        return getWorkbench().getSharedImages().getImageDescriptor( symbolicName );
    }
    public static void closeEditor( final IFile file )
    {
        if( file == null )
            return;
        final IEditorPart editorPart = activePage().findEditor( new FileEditorInput( file ) );
        if( editorPart == null )
            return;
        activePage().closeEditor( editorPart, true );
    }
    public static void openEditor( final IFile file ) 
    throws PartInitException
    {
        if( file == null )
            return;
        final IEditorDescriptor descriptor = getWorkbench().getEditorRegistry().getDefaultEditor( file.getName() );
        activePage().openEditor( new FileEditorInput( file ), descriptor.getId() );
    }
}
