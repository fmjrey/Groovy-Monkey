package net.sf.groovyMonkey;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static org.apache.commons.lang.StringUtils.equals;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.strip;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.core.runtime.Platform.getBundle;
import static org.eclipse.jface.dialogs.MessageDialog.QUESTION;
import static org.eclipse.jface.dialogs.MessageDialog.openError;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.update.search.UpdateSearchRequest.createDefaultSiteSearchCategory;
import static org.eclipse.update.ui.UpdateManagerUI.openInstaller;
import static org.osgi.framework.Bundle.UNINSTALLED;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.update.search.UpdateSearchRequest;
import org.eclipse.update.search.UpdateSearchScope;
import org.eclipse.update.ui.UpdateJob;
import org.osgi.framework.Bundle;

public class ScriptMetadata 
{
    public static final DOMDescriptor DEFAULT_DOM = new DOMDescriptor( "http://groovy-monkey.sourceforge.net/update/plugins", PLUGIN_ID );
    public enum JobModes
    {
        Job, UIJob, WorkspaceJob
    }
    public enum ExecModes
    {
        Background, Foreground
    }
    public static final String DEFAULT_LANG = "Groovy";
    public static final JobModes DEFAULT_JOB = JobModes.Job;
    public static final ExecModes DEFAULT_MODE = ExecModes.Background;
	private IFile file;
	private String menuName = "";
    private String kudos = "";
    private String license = "EPL 1.0";
	private String lang = DEFAULT_LANG;
	private final List< DOMDescriptor > doms = new ArrayList< DOMDescriptor >();
	private final List< Subscription > subscriptions = new ArrayList< Subscription >();
    private final Set< String > includes = new LinkedHashSet< String >();
    private final Set< String > includedBundles = new LinkedHashSet< String >();
    private JobModes jobMode = DEFAULT_JOB;
    private ExecModes execMode = DEFAULT_MODE; 
    
    public void setJobMode( final String jobMode )
    {
        for( final JobModes mode : JobModes.values() )
        {
            if( !equalsIgnoreCase( mode.toString(), jobMode.trim() ) )
                continue;
            this.jobMode = mode;
            break;
        }
    }
    public JobModes getJobMode()
    {
        return jobMode;
    }
    public void setExecMode( final String execMode )
    {
        for( final ExecModes mode : ExecModes.values() )
        {
            if( !equalsIgnoreCase( mode.toString(), execMode.trim() ) )
                continue;
            this.execMode = mode;
            break;
        }
    }
    public ExecModes getExecMode()
    {
        return execMode;
    }
    public boolean isBackground()
    {
        return execMode.equals( ExecModes.Background );
    }
    public boolean isForeground()
    {
        return execMode.equals( ExecModes.Foreground );
    }
    public void addInclude( final String include )
    {
        includes.add( include );
    }
	public Set< String > getIncludes()
    {
        return includes;
    }
    public void addIncludedBundle( final String bundleID )
    {
        includedBundles.add( bundleID );
    }
    public Set< String > getIncludedBundles()
    {
        return includedBundles;
    }
    public void setMenuName( final String string )
    {
        menuName = string;
    }
    public void setFile( final IFile file )
    {
        this.file = file;
    }
    public IFile getFile()
    {
        return file;
    }
    public String getMenuName()
    {
        return menuName;
    }
    public void addDOM( final DOMDescriptor dom )
    {
        doms.add( dom );
    }
	public List< DOMDescriptor > getDOMs() 
    {
		return doms;
	}
	public String getReasonableFilename()
    {
        if( file != null )
            return file.getName();
        if( isNotBlank( menuName ) )
        {
            String result = menuName;
            result = result.replaceAll( " ", "_" );
            final Pattern illegalChars = compile( "[^\\p{Alnum}_-]" );
            final Matcher match = illegalChars.matcher( result );
            result = match.replaceAll( "" );
            if( !result.equals( "" ) )
                return result + FILE_EXTENSION;
        }
        return "script" + FILE_EXTENSION;
    }
    public boolean containsDOMByPlugin( final String pluginID )
    {
        for( final DOMDescriptor dom : doms )
            if( equals( dom.pluginName, pluginID ) )
                return true;
        return false;
    }
	public boolean ensureDomsAreLoaded( final IWorkbenchWindow window )
    {
        String missingPlugins = "";
        final URLtoPluginMap missingUrls = new URLtoPluginMap();
        for( final Object element0 : doms )
        {
            final DOMDescriptor element = ( DOMDescriptor )element0;
            Bundle b = getBundle( element.pluginName );
            if( b == null )
            {
                missingPlugins += "     " + element.pluginName + "\n";
                missingUrls.add( element );
            }
            else if( b.getState() == UNINSTALLED )
                missingPlugins += "     " + element.pluginName + "\n";
        }
        if( missingPlugins.length() > 0 )
        {
            missingPlugins = missingPlugins.substring( 0, missingPlugins.length() - 1 );
            final String choice = notifyMissingDOMs( missingPlugins );
            if( choice.startsWith( "Install" ) )
                launchUpdateInstaller( missingUrls );
            if( choice.startsWith( "Edit" ) )
                openEditor();
            return false;
        }
        return true;
    }
	@Override
    public boolean equals( final Object obj )
    {
	    return reflectionEquals( this, obj );
    }
    @Override
    public int hashCode()
    {
        return reflectionHashCode( this );
    }
    class URLtoPluginMap
    {
        final Map< String, Set< String > > map = new HashMap< String, Set< String > >();

        String getPluginNames( final String url )
        {
            final Set ids = map.get( url );
            String idstr = "";
            for( final Iterator iterator = ids.iterator(); iterator.hasNext(); )
            {
                final String id = ( String )iterator.next();
                idstr += id + ", ";
            }
            idstr = idstr.substring( 0, idstr.length() - 2 );
            return idstr;
        }
        void add( final DOMDescriptor domdesc )
        {
            Set< String > ids = map.get( domdesc.url );
            if( ids == null )
                ids = new HashSet< String >();
            ids.add( domdesc.pluginName );
            map.put( domdesc.url, ids );
        }
    }
	private void openEditor()
    {
        try
        {
            final IWorkbenchPage page = getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorDescriptor desc = getWorkbench().getEditorRegistry().getDefaultEditor( file.getName() );
            if( desc == null )
                desc = getWorkbench().getEditorRegistry().getDefaultEditor( "foo" + FILE_EXTENSION );
            page.openEditor( new FileEditorInput( file ), desc.getId() );
        }
        catch( final PartInitException x )
        {
            openError( null, "Eclipse::PartInitException", "Unable to open editor on " + file.getName() + " due to " + x.toString() );
        }
    }
	private void launchUpdateInstaller( final URLtoPluginMap missingUrls ) 
    {
        if( Display.getCurrent() == null )
        {
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    launchUpdateInstaller( missingUrls );
                }
            };
            Display.getDefault().syncExec( runnable );
            return;
        }
		final UpdateSearchScope scope = new UpdateSearchScope();
        for( final String url : missingUrls.map.keySet() )
        {
            System.out.println( "ScriptMetadata.launchUpdateInstaller(): url: " + url );
            final String id = missingUrls.getPluginNames( url );
            final String plural = id.indexOf( "," ) >= 0 ? "s" : "";
            final String description = "Site providing DOM" + plural + " ( " + id + " )";
			try 
            {
				scope.addSearchSite( description, new URL( url ), new String[ 0 ] );
			} 
            catch( final MalformedURLException x ) {}
		}
		final UpdateSearchRequest request = new UpdateSearchRequest( createDefaultSiteSearchCategory(), scope );
		final UpdateJob job = new UpdateJob( "Install Groovy Monkey DOMs", request );
        final Shell shell = getWorkbench().getActiveWorkbenchWindow().getShell();
		openInstaller( shell, job );
	}
	private String notifyMissingDOMs( final String missingPlugins )
    {
        if( Display.getCurrent() == null )
        {
            final String[] returnValue = new String[ 1 ];
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    returnValue[ 0 ] = notifyMissingDOMs( missingPlugins );
                }
            };
            Display.getDefault().syncExec( runnable );
            return returnValue[ 0 ];
        }
        final String plural = missingPlugins.indexOf( "\n" ) >= 0 ? "s" : "";
        final String these = isNotBlank( plural ) ? "these" : "this";
        final String[] choices = new String[]{ "Cancel Script", "Edit Script", "Install Plug-in" + plural };
        final MessageDialog dialog = new MessageDialog( null, 
                                                        "Missing DOM" + plural, 
                                                        null, 
                                                        "The script " + file.getName() + " requires " + these + " missing DOM plug-in" + plural + ":\n" + missingPlugins,
                                                        QUESTION, 
                                                        choices, 
                                                        2 );
        final int result = dialog.open();
        final String choice = choices[ result ];
        return choice;
    }
    public String toHeader()
    {
        final StringBuffer buffer = new StringBuffer();
        buffer.append( "/*" ).append( "\n" );
        if( isNotBlank( getMenuName() ) )
            buffer.append( " * Menu: " + getMenuName() ).append( "\n" );
        buffer.append( " * Kudos: " + getKudos() ).append( "\n" );
        buffer.append( " * License: " + getLicense() ).append( "\n" );
        if( !getLang().equals( DEFAULT_LANG ) )
            buffer.append( " * LANG: " + getLang() ).append( "\n" );
        if( !getJobMode().equals( DEFAULT_JOB ) )
            buffer.append( " * Job: " + getJobMode() ).append( "\n" );
        if( !getExecMode().equals( DEFAULT_MODE ) )
            buffer.append( " * Exec-Mode: " + getExecMode() ).append( "\n" );
        for( final DOMDescriptor dom : getDOMs() )
        {
            if( dom.equals( DEFAULT_DOM ) )
                continue;
            buffer.append( " * DOM: " + dom ).append( "\n" );
        }
        for( final String include : getIncludes() )
            buffer.append( " * Include: " + include ).append( "\n" );
        for( final String include : getIncludedBundles() )
            buffer.append( " * Include-Bundle: " + include ).append( "\n" );
        buffer.append( " */" ).append( "\n" );
        buffer.append( "\n" );
        return buffer.toString();
    }
	public static ScriptMetadata getScriptMetadata( final String contents ) 
    {
		final ScriptMetadata metadata = new ScriptMetadata();
        metadata.addDOM( DEFAULT_DOM );
        Pattern pattern = compile( "^\\s*\\/\\*.*?\\*\\/", DOTALL );
        Matcher matcher = pattern.matcher( contents );
        if( !matcher.find() )
            return metadata; // no meta-data comment - do nothing

        final String comment = matcher.group();
        pattern = compile( "Menu:\\s*((\\p{Graph}| )+)", DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setMenuName( matcher.group( 1 ) );
        
        pattern = compile( "Kudos:\\s*((\\p{Graph}| )+)", DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setKudos( matcher.group( 1 ) );
        
        pattern = compile( "License:\\s*((\\p{Graph}| )+)", DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setLicense( matcher.group( 1 ) );
        
        pattern = compile( "DOM:\\s*(\\p{Graph}+)\\/((\\p{Alnum}|\\.)+)", DOTALL );
        matcher = pattern.matcher( comment );
        while( matcher.find() )
            metadata.addDOM( new DOMDescriptor( matcher.group( 1 ), matcher.group( 2 ) ) );
        
        pattern = compile( "Listener:", DOTALL );
        matcher = pattern.matcher( comment );
        while( matcher.find() )
            metadata.subscriptions.add( new Subscription( "workspace", "addResourceChangeListener" ) );
        
        pattern = compile( "LANG:\\s*((\\p{Graph}| )+)", DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setLang( matcher.group( 1 ) );
        
        pattern = compile( "Include:\\s*((\\p{Graph}| )+)", DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.addInclude( matcher.group( 1 ) );

        pattern = compile( "Include-Bundle:\\s*((\\p{Graph}| )+)", DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.addIncludedBundle( matcher.group( 1 ) );
        
        pattern = compile( "Job:\\s*((\\p{Graph}| )+)", DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setJobMode( matcher.group( 1 ) );
        
        pattern = compile( "Exec-Mode:\\s*((\\p{Graph}| )+)", DOTALL );
        matcher = pattern.matcher( comment );
        if( matcher.find() )
            metadata.setExecMode( matcher.group( 1 ) );
        
        return metadata;
	}
    public static List< String > getMetadataLines( final String contents )
    {
        final String[] lines = split( contents, "\r\n" );
        final List< String > code = new ArrayList< String >();
        boolean started = false;
        boolean finished = false;
        for( final String line : lines )
        {
            if( strip( line ).startsWith( "/*" ) )
            {
                started = true;
                code.add( line );
                continue;
            }
            if( strip( line ).endsWith( "*/" ) )
            {
                finished = true;
                code.add( line );
                continue;
            }
            if( started && !finished )
            {
                code.add( line );
                continue;
            }
        }
        return code;
    }
	public static String stripMetadata( final String contents )
    {
	    final String[] lines = split( contents, "\r\n" );
        final List< String > code = new ArrayList< String >();
        boolean started = false;
        boolean finished = false;
        for( final String line : lines )
        {
            if( strip( line ).startsWith( "/*" ) )
            {
                started = true;
                continue;
            }
            if( strip( line ).endsWith( "*/" ) )
            {
                finished = true;
                continue;
            }
            if( started && !finished )
                continue;
            code.add( line );
        }
        return join( code.toArray( new String[ 0 ] ), "\n" );
    }
	public void subscribe()
    {
        for( final Subscription subscription : subscriptions )
            subscription.subscribe();
    }
    public void unsubscribe()
    {
        for( final Subscription subscription : subscriptions )
            subscription.unsubscribe();
    }
	public String getLang() 
    {
		return lang;
	}
	public void setLang( final String lang ) 
    {
		this.lang = isNotBlank( lang ) ? lang : DEFAULT_LANG;
	}
    public String getKudos()
    {
        return kudos;
    }
    public void setKudos( final String kudos )
    {
        this.kudos = kudos;
    }
    public String getLicense()
    {
        return license;
    }
    public void setLicense( final String license )
    {
        this.license = license;
    }
}
class Subscription
{
    private final String addMethodName;
    private Object listenerProxy;
    private Method removeMethod;
    private Object source;

    public Subscription( final String source, 
                         final String addMethodName )
    {
        this.addMethodName = addMethodName;
    }
    public void subscribe()
    {
        source = getWorkspace();
        try
        {
            subscribe( source, addMethodName );
        }
        catch( final InstantiationException e )
        {
            e.printStackTrace();
        }
        catch( final IllegalAccessException e )
        {
            e.printStackTrace();
        }
        catch( final InvocationTargetException e )
        {
            e.printStackTrace();
        }
        catch( final NoSuchMethodException e )
        {
            e.printStackTrace();
        }
    }
    public void unsubscribe()
    {
        try
        {
            removeMethod.invoke( source, new Object[]{ listenerProxy } );
        }
        catch( final IllegalArgumentException e )
        {
            e.printStackTrace();
        }
        catch( final IllegalAccessException e )
        {
            e.printStackTrace();
        }
        catch( final InvocationTargetException e )
        {
            e.printStackTrace();
        }
    }
    private void subscribe( final Object foo, 
                            final String methodName ) 
    throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        final InvocationHandler listener = new GenericListener();
        final Method addMethod = findAddMethod( foo, methodName );
        // TODO what if null is returned?
        final Class listenerType = addMethod.getParameterTypes()[ 0 ];
        listenerProxy = newProxyInstance( listenerType.getClassLoader(), new Class[]{ listenerType }, listener );
        addMethod.invoke( foo, new Object[]{ listenerProxy } );
    }
    private Method findAddMethod( final Object source, 
                                  final String methodName )
    {
        final Method methods[] = source.getClass().getMethods();
        for( final Method method : methods )
        {
            if( !method.getName().equals( methodName ) )
                continue;
            if( method.getParameterTypes().length != 1 )
                continue;
            if( findRemoveMethod( source, methodName, method.getParameterTypes() ) == null )
                continue;
            return method;
        }
        return null;
    }
    private Method findRemoveMethod( final Object source, 
                                     final String methodName, 
                                     final Class[] parameterTypes )
    {
        final String removeMethodName = "remove" + methodName.substring( 3 );
        try
        {
            removeMethod = source.getClass().getMethod( removeMethodName, parameterTypes );
            return removeMethod;
        }
        catch( final Exception e )
        {
            return null;
        }
    }
    class GenericListener 
    implements InvocationHandler
    {
        public Object invoke( final Object proxy, 
                              final Method method, 
                              final Object[] args ) 
        throws Throwable
        {
            System.out.println( method.getName() + " did it " + args[ 0 ] );
            return null;
        }
    }
}
