package net.sf.groovyMonkey;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.FILE_EXTENSION;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.MONKEY_DIR;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.PLUGIN_ID;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.SCRIPTS_PROJECT;
import static net.sf.groovyMonkey.Tags.getTag;
import static net.sf.groovyMonkey.Tags.getTagText;
import static net.sf.groovyMonkey.dom.Utilities.contents;
import static net.sf.groovyMonkey.dom.Utilities.getContents;
import static net.sf.groovyMonkey.dom.Utilities.readLines;
import static net.sf.groovyMonkey.dom.Utilities.setContents;
import static net.sf.groovyMonkey.util.ListUtil.caseless;
import static net.sf.groovyMonkey.util.ListUtil.list;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.chomp;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.strip;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.eclipse.core.resources.IResource.DEPTH_INFINITE;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.Platform.getBundle;
import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.jface.dialogs.MessageDialog.QUESTION;
import static org.eclipse.jface.dialogs.MessageDialog.openError;
import static org.eclipse.swt.widgets.Display.getCurrent;
import static org.eclipse.swt.widgets.Display.getDefault;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.update.search.UpdateSearchRequest.createDefaultSiteSearchCategory;
import static org.eclipse.update.ui.UpdateManagerUI.openInstaller;
import static org.osgi.framework.Bundle.UNINSTALLED;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.groovyMonkey.lang.IMonkeyScriptFactory;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
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
    public static final DOMDescriptor DEFAULT_DOM = new DOMDescriptor( "http://groovy-monkey.sourceforge.net/update/plugins", PLUGIN_ID, null );
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
    private String scriptPath = "";
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
    private final Set< Marker > markers = new HashSet< Marker >();

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
    public static ScriptMetadata getScriptMetadata( final IFile file )
    throws CoreException, IOException
    {
        return getScriptMetadata( getContents( file ) );
    }
    public static ScriptMetadata getScriptMetadata( final String contents )
    {
        final ScriptMetadata metadata = new ScriptMetadata();
        metadata.addDOM( DEFAULT_DOM );
        metadata.addIncludedBundle( PLUGIN_ID );
        Pattern pattern = compile( "^\\s*\\/\\*.*?\\*\\/", DOTALL );
        Matcher matcher = pattern.matcher( contents );
        if( !matcher.find() )
            return metadata; // no meta-data comment - do nothing
        int lineNumber = 0;
        int currentLineEOLOffset = 0;
        for( final String lineString : readLines( matcher.group() ) )
        {
            final int eolOffset = currentLineEOLOffset;
            currentLineEOLOffset += lineString.length() + 1;
            lineNumber++;
            // The next line ensures that /*, *, */ and whitespace are removed from the ends of the line.
            final String line = removeEnd( removeEnd( removeStart( removeStart( lineString.trim(), "/" ), "*" ).trim(), "/" ).trim(), "*" ).trim();
            if( isBlank( line ) )
                continue;
            if( line.startsWith( getTag( Tags.Type.PATH ) ) )
            {
                metadata.setScriptPath( removeStart( line, getTag( Tags.Type.PATH ) ).trim() );
                continue;
            }
            if( line.startsWith( getTag( Tags.Type.MENU ) ) )
            {
                metadata.setMenuName( removeStart( line, getTag( Tags.Type.MENU ) ).trim() );
                continue;
            }
            if( line.startsWith( getTag( Tags.Type.KUDOS ) ) )
            {
                metadata.setKudos( removeStart( line, getTag( Tags.Type.KUDOS ) ).trim() );
                continue;
            }
            if( line.startsWith( getTag( Tags.Type.LICENSE ) ) )
            {
                metadata.setLicense( removeStart( line, getTag( Tags.Type.LICENSE ) ).trim() );
                continue;
            }
            if( line.startsWith( getTag( Tags.Type.DOM ) ) )
            {
                pattern = compile( getTag( Tags.Type.DOM ) + "\\s*(\\p{Graph}+)\\/((\\p{Alnum}|\\.)+)", DOTALL );
                final String domString = substringBeforeLast( line, "[" );
                final String varMapString = substringBeforeLast( substringAfterLast( line, "[" ), "]" );
                final Map< String, String > varNameMap = new TreeMap< String, String >();
                for( final String mapping : split( varMapString, ',' ) )
                {
                    final String localVarName = substringBeforeLast( mapping, ":" );
                    final String domVarName = substringAfterLast( mapping, ":" );
                    if( isNotBlank( localVarName ) && isNotBlank( domVarName ) )
                        varNameMap.put( domVarName.trim(), localVarName.trim() );
                }
                matcher = pattern.matcher( domString );
                if( matcher.find() )
                    metadata.addDOM( new DOMDescriptor( matcher.group( 1 ), matcher.group( 2 ), varNameMap ) );
                continue;
            }
            if( line.startsWith( getTag( Tags.Type.LISTENER ) ) )
            {
                final String filter = removeStart( line, getTag( Tags.Type.LISTENER ) ).trim();
                if( isBlank( filter ) )
                    metadata.markers.add( Marker.warning( Tags.Type.LISTENER, "Empty filter string: matches all resource changed events.", lineNumber ) );
                metadata.subscriptions.add( new Subscription( metadata, filter ) );
                continue;
            }
            if( line.startsWith( getTag( Tags.Type.LANG ) ) )
            {
                final String langName = removeStart( line, getTag( Tags.Type.LANG ) ).trim();
                final Map< String, IMonkeyScriptFactory > languages = RunMonkeyScript.getScriptFactories();
                if( !languages.containsKey( langName ) )
                {
                    final int charStart = lineString.indexOf( langName ) + eolOffset;
                    final int charEnd = charStart + langName.length();
                    metadata.markers.add( Marker.error( Tags.Type.LANG, "Error language: " + langName + " is not currently supported. Supported: " + languages.keySet(), lineNumber, charStart, charEnd ) );
                }
                metadata.setLang( langName );
                continue;
            }
            if( line.startsWith( getTag( Tags.Type.INCLUDE ) ) )
            {
                metadata.addInclude( removeStart( line, getTag( Tags.Type.INCLUDE ) ).trim() );
                continue;
            }
            if( line.startsWith( getTag( Tags.Type.INCLUDE_BUNDLE ) ) )
            {
                metadata.addIncludedBundle( removeStart( line, getTag( Tags.Type.INCLUDE_BUNDLE ) ).trim() );
                continue;
            }
            if( line.startsWith( getTag( Tags.Type.JOB ) ) )
            {
                final String jobType = removeStart( line, getTag( Tags.Type.JOB ) ).trim();
                if( !caseless( JobModes.values() ).contains( jobType ) )
                {
                    final int charStart = lineString.indexOf( jobType ) + eolOffset;
                    final int charEnd = charStart + jobType.length();
                    metadata.markers.add( Marker.error( Tags.Type.JOB, "Error no Job Type: " + jobType + " supported. Supported: " + list( JobModes.values() ), lineNumber, charStart, charEnd ) );
                }
                metadata.setJobMode( jobType );
                continue;
            }
            if( line.startsWith( getTag( Tags.Type.EXEC_MODE ) ) )
            {
                final String execModeType = removeStart( line, getTag( Tags.Type.EXEC_MODE ) ).trim();
                if( !caseless( ExecModes.values() ).contains( execModeType ) )
                {
                    final int charStart = lineString.indexOf( execModeType ) + eolOffset;
                    final int charEnd = charStart + execModeType.length();
                    metadata.markers.add( Marker.error( Tags.Type.EXEC_MODE, "Error no Exec-Mode Type: " + execModeType + " supported. Supported: " + list( ExecModes.values() ), lineNumber, charStart, charEnd ) );
                }
                metadata.setExecMode( execModeType );
                continue;
            }
            metadata.markers.add( Marker.error( Tags.Type.BAD_TAG, "Error no identifiable tag: " + line, lineNumber ) );
        }
        return metadata;
    }
    public static void refreshScriptMetadata( final IFile script,
                                              final ScriptMetadata metadata )
    {
        refreshScriptMetadata( script, metadata, false );
    }
    public static void refreshScriptMetadata( final IFile script,
                                              final ScriptMetadata metadata,
                                              final boolean force )
    {
        if( script == null || !script.exists() || metadata == null )
            return;
        try
        {
            if( !force && metadata.toHeader().equals( getScriptMetadata( script ).toHeader() ) )
            {
                new WorkspaceJob( "Refreshing markers for script: " + script.getName() )
                {
                    @Override
                    public IStatus runInWorkspace( final IProgressMonitor monitor ) throws CoreException
                    {
                        setMarkers( script );
                        return Status.OK_STATUS;
                    }
                }.schedule();
                return;
            }
        }
        catch( final CoreException e )
        {
            throw new RuntimeException( e );
        }
        catch( final IOException e )
        {
            throw new RuntimeException( e );
        }
        new WorkspaceJob( "Refreshing metadata for script: " + script.getName() )
        {
            @Override
            public IStatus runInWorkspace( final IProgressMonitor monitor ) throws CoreException
            {
                try
                {
                    if( !force && metadata.header().equals( getScriptMetadata( script ).header() ) )
                        return Status.OK_STATUS;
                    setContents( metadata.header() + stripMetadata( contents( script ) ), script );
                }
                catch( final IOException e )
                {
                    throw new CoreException( new Status( ERROR, PLUGIN_ID, 0, e.getMessage(), e ) );
                }
                finally
                {
                    setMarkers( script );
                }
                return OK_STATUS;
            }
        }.schedule();
}
    public static void setMarkers( final IFile script )
    throws CoreException
    {
        if( script == null || !script.exists() )
            return;
        try
        {
            script.deleteMarkers( IMarker.PROBLEM, true, DEPTH_INFINITE );
            final ScriptMetadata metadata = getScriptMetadata( script );
            for( final Marker marker : metadata.markers() )
            {
                final IMarker m = script.createMarker( IMarker.PROBLEM );
                m.setAttribute( IMarker.SEVERITY, marker.severity() );
                m.setAttribute( IMarker.MESSAGE, marker.message() );
                // The next attribute is a marker to allow us to provide potential resolutions ( i.e. Quick Fixes )
                m.setAttribute( PLUGIN_ID, "marker" );
                // The next attribute is a hint to the resolution ( i.e. quick fix ) generator as to what to do.
                m.setAttribute( PLUGIN_ID + ".tag", marker.tag().toString() );
                if( marker.lineNumber() != -1 )
                    m.setAttribute( IMarker.LINE_NUMBER, marker.lineNumber() );
                if( marker.charStart() != -1 )
                    m.setAttribute( IMarker.CHAR_START, marker.charStart() );
                if( marker.charEnd() != -1 )
                    m.setAttribute( IMarker.CHAR_END, marker.charEnd() );
            }
        }
        catch( final IOException e )
        {
            throw new CoreException( new Status( ERROR, PLUGIN_ID, 0, e.getMessage(), e ) );
        }
    }
    public static String stripIllegalChars( final String string )
    {
        return compile( "[^\\p{Alnum}_-]" ).matcher( string ).replaceAll( "" );
    }
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
        menuName = defaultString( string );
    }
    public void setScriptPath( final String string )
    {
        scriptPath = defaultString( string );
    }
    public void setScriptPath()
    {
        if( file() != null )
            scriptPath = defaultString( file().getFullPath().toString() );
        else
            scriptPath = "/" + SCRIPTS_PROJECT + "/" + MONKEY_DIR + "/" + getReasonableFilename();
    }
    public String getScriptPath()
    {
        if( isBlank( scriptPath ) || ( file() != null && !file().getFullPath().toString().equals( scriptPath ) ) )
            setScriptPath();
        return scriptPath;
    }
    public String scriptPath()
    {
        return getScriptPath();
    }
    public void setFile( final IFile file )
    {
        this.file = file;
        setScriptPath();
    }
    public IFile getFile()
    {
        return file;
    }
    public IFile file()
    {
        return getFile();
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
	private String getReasonableFilename()
    {
        if( file != null )
            return file.getName();
        if( isNotBlank( getMenuName() ) )
        {
            final StringBuffer buffer = new StringBuffer();
            final String[] array = split( getMenuName(), " " );
            for( final String token : array )
                buffer.append( capitalize( stripIllegalChars( token ) ) );
            if( isNotBlank( buffer.toString() ) )
                return buffer.toString() + FILE_EXTENSION;
        }
        return "script" + FILE_EXTENSION;
    }
    public boolean containsDOMByPlugin( final String pluginID )
    {
        for( final DOMDescriptor dom : doms )
            if( StringUtils.equals( dom.pluginName, pluginID ) )
                return true;
        return false;
    }
    public DOMDescriptor getDOMByPlugin( final String pluginID )
    {
        for( final DOMDescriptor dom : doms )
            if( StringUtils.equals( dom.pluginName, pluginID ) )
                return dom;
        return null;
    }
	public boolean ensureDomsAreLoaded( final IWorkbenchWindow window )
    {
        final StringBuffer missingPlugins = new StringBuffer();
        final URLtoPluginMap missingUrls = new URLtoPluginMap();
        for( final DOMDescriptor dom : doms )
        {
            final Bundle bundle = getBundle( dom.pluginName );
            if( bundle == null )
            {
                missingPlugins.append( "     " ).append( dom.pluginName ).append( "\n" );
                missingUrls.add( dom );
                continue;
            }
            if( bundle.getState() == UNINSTALLED )
                missingPlugins.append( "     "  ).append( dom.pluginName ).append( "\n" );
        }
        if( missingPlugins.length() > 0 )
        {
            final String choice = notifyMissingDOMs( chomp( missingPlugins.toString() ) );
            if( choice.startsWith( "Install" ) )
                launchUpdateInstaller( missingUrls );
            if( choice.startsWith( "Edit" ) )
                openEditor();
            return false;
        }
        return true;
    }
	public List< Subscription > getSubscriptions()
    {
        return subscriptions;
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
            final StringBuffer buffer = new StringBuffer();
            for( final String id : map.get( url ) )
                buffer.append( id ).append( ", " );
            return removeEnd( buffer.toString(), ", " );
        }
        void add( final DOMDescriptor domdesc )
        {
            if( map.get( domdesc.url ) == null )
                map.put( domdesc.url, new HashSet< String >() );
            final Set< String > ids = map.get( domdesc.url );
            ids.add( domdesc.pluginName );
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
        if( getCurrent() == null )
        {
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    launchUpdateInstaller( missingUrls );
                }
            };
            getDefault().syncExec( runnable );
            return;
        }
		final UpdateSearchScope scope = new UpdateSearchScope();
        for( final String url : missingUrls.map.keySet() )
        {
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
        if( getCurrent() == null )
        {
            final String[] returnValue = new String[ 1 ];
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    returnValue[ 0 ] = notifyMissingDOMs( missingPlugins );
                }
            };
            getDefault().syncExec( runnable );
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
    public String header()
    {
        return toHeader();
    }
    public String toHeader()
    {
        final StringBuffer buffer = new StringBuffer();
        buffer.append( "/*" ).append( "\n" );
        if( isNotBlank( getMenuName() ) )
            buffer.append( getTagText( Tags.Type.MENU ) + getMenuName() ).append( "\n" );

        if( isNotBlank( scriptPath() ) )
            buffer.append( getTagText( Tags.Type.PATH ) + scriptPath() ).append( "\n" );

        buffer.append( getTagText( Tags.Type.KUDOS ) + getKudos() ).append( "\n" );

        buffer.append( getTagText( Tags.Type.LICENSE ) + getLicense() ).append( "\n" );
        if( !getLang().equals( DEFAULT_LANG ) )
            buffer.append( getTagText( Tags.Type.LANG ) + getLang() ).append( "\n" );

        if( !getJobMode().equals( DEFAULT_JOB ) )
            buffer.append( getTagText( Tags.Type.JOB ) + getJobMode() ).append( "\n" );

        if( !getExecMode().equals( DEFAULT_MODE ) )
            buffer.append( getTagText( Tags.Type.EXEC_MODE ) + getExecMode() ).append( "\n" );

        for( final DOMDescriptor dom : getDOMs() )
            if( !dom.equals( DEFAULT_DOM ) )
                buffer.append( getTagText( Tags.Type.DOM ) + dom ).append( "\n" );

        for( final String include : getIncludes() )
            buffer.append( getTagText( Tags.Type.INCLUDE ) + include ).append( "\n" );

        for( final String include : getIncludedBundles() )
            if( !include.equals( PLUGIN_ID ) )
                buffer.append( getTagText( Tags.Type.INCLUDE_BUNDLE ) + include ).append( "\n" );

        for( final Subscription subscription : getSubscriptions() )
            buffer.append( getTagText( Tags.Type.LISTENER ) + subscription.getFilter() ).append( "\n" );

        buffer.append( " */" ).append( "\n" );
        buffer.append( "\n" );
        return buffer.toString();
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
    public Set< Marker > getMarkers()
    {
        return markers;
    }
    public Set< Marker > markers()
    {
        return getMarkers();
    }
}
