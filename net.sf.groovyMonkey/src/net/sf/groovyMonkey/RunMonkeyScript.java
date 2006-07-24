package net.sf.groovyMonkey;
import static java.util.Collections.synchronizedMap;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.addScript;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.scriptStore;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.UpdateMonkeyActionsResourceChangeListener.createTheMonkeyMenu;
import static net.sf.groovyMonkey.dom.Utilities.SCRIPT_NAME;
import static net.sf.groovyMonkey.dom.Utilities.contents;
import static net.sf.groovyMonkey.dom.Utilities.key;
import static net.sf.groovyMonkey.dom.Utilities.state;
import static org.eclipse.core.runtime.Platform.getExtensionRegistry;
import static org.eclipse.core.runtime.SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.sf.groovyMonkey.ScriptMetadata.JobModes;
import net.sf.groovyMonkey.dom.Utilities;
import net.sf.groovyMonkey.lang.CompilationException;
import net.sf.groovyMonkey.lang.IMonkeyScriptFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.progress.UIJob;

public class RunMonkeyScript 
{
    public static ScriptMetadata last_run = null;
	private final IWorkbenchWindow window;
	private final IFile file;
    private final boolean throwError;    
    private volatile Map< String, Object > map = synchronizedMap( new HashMap< String, Object >() );
    private ScriptMetadata metadata = null;
    private boolean synchronous = true;
    
	public RunMonkeyScript( final IFile file, 
                            final IWorkbenchWindow window ) 
    {
	    this( file, window, false );
	}
    public RunMonkeyScript( final IFile file, 
                            final IWorkbenchWindow window, 
                            final boolean throwError ) 
    {
        this( file, window, null, throwError );
    }
    public RunMonkeyScript( final IFile file, 
                            final IWorkbenchWindow window,
                            final Map< String, Object > map,
                            final boolean throwError ) 
    {
        this.window = window;
        this.file = file;
        this.throwError = throwError;
        this.map = map != null ? map : this.map;
    }
    public void run( final boolean synchronous )
    {
        this.synchronous = synchronous;
        run();
    }
	public void run()
    {
        setMetadata();
        if( metadata.getJobMode() == JobModes.UIJob )
        {
            runUIJob();
            return;
        }
        if( metadata.getJobMode() == JobModes.WorkspaceJob )
        {
            runWorkspaceJob();
            return;
        }
        runJob();
	}
    private void setMetadata()
    {
        final String fileName = key( file );
        metadata = scriptStore().get( fileName );
        if( metadata != null )
            return;
        try
        {
            metadata = getScriptMetadata( contents( file ) );
            metadata.setFile( file );
            addScript( fileName, metadata );
            scriptStore().put( fileName, metadata );
        }
        catch( final IOException ioe )
        {
            throw new RuntimeException( "Could not load script: " + fileName + ". " + ioe, ioe );
        }
        catch( final CoreException e )
        {
            throw new RuntimeException( "Could not load script: " + fileName + ". " + e, e );
        }
    }
    private void runUIJob()
    {
        final UIJob job = new UIJob( "Script: " + file.getName() )
        {
            @Override
            public IStatus runInUIThread( final IProgressMonitor monitor )
            {
                runScript( monitor );
                monitor.worked( 1 );
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        exec( job );
    }
    private void runWorkspaceJob()
    {
        final WorkspaceJob job = new WorkspaceJob( "Script: " + file.getName() )
        {
            @Override
            public IStatus runInWorkspace( final IProgressMonitor monitor )
            throws CoreException
            {
                runScript( monitor );
                monitor.worked( 1 );
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        exec( job );
    }
    private void runJob()
    {
        final Job job = new Job( "Script: " + file.getName() )
        {
            @Override
            public IStatus run( final IProgressMonitor monitor )
            {
                runScript( monitor );
                monitor.worked( 1 );
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        exec( job );
    }
    private void exec( final Job job )
    {
        if( metadata.isForeground() )
            job.setUser( true );
        job.schedule();
        if( synchronous )
            join( job );
    }
    private void join( final Job job )
    {
        // There is no use joining a job from within the same UI Thread
        if( job instanceof UIJob && Display.getCurrent() != null )
            return;
        try
        {
            job.join();
        }
        catch( final InterruptedException e ) {}
    }
    public void runScript( final IProgressMonitor progressMonitor )
    {
        final IProgressMonitor monitor = progressMonitor != null ? progressMonitor : new NullProgressMonitor();
        monitor.beginTask( file.getName(), 1 );
        map.put( "monitor", new SubProgressMonitor( monitor, 1, PREPEND_MAIN_LABEL_TO_SUBTASK ) );
        try
        {
            setMetadata();
            runScript();
        }
        catch( final Throwable x )
        {
            error( x, x.getMessage() );
        }
        finally
        {
            monitor.done();
        }
    }
    private void runScript()
    {
        try
        {
            defineDynamicVariables( file );
            if( !metadata.ensureDomsAreLoaded( window ) )
                return;
            final String scriptLang = metadata.getLang();
            final Map< String, IMonkeyScriptFactory > factories = getScriptFactories();
            boolean found = false;
            for( final String language : factories.keySet() )
            {
                final IMonkeyScriptFactory factory = factories.get( language );
                if( !factory.isLang( scriptLang ) )
                    continue;
                found = true;
                factory.runner( metadata, map ).run();
            }
            if( !found )
                error( "No factory for language: " + scriptLang,
                       scriptLang + " not found. Available are: " + factories.keySet(),
                       new Exception() );
        }
        catch( final CompilationException x )
        {
            error( x, x.getMessage() );
        }
        finally
        {
            last_run = metadata;
            createTheMonkeyMenu();
            undefineDynamicVariables( file );
        }
    }
    
	private void defineDynamicVariables( final IFile file ) 
    {
		state().begin( file );
		state().set( SCRIPT_NAME, file.getFullPath().toPortableString() );
	}
	private void undefineDynamicVariables( final IFile file ) 
    {
		state().end( file );
	}
    public static Map< String, IMonkeyScriptFactory > getScriptFactories()
    {
        final Map< String, IMonkeyScriptFactory > factories = new HashMap< String, IMonkeyScriptFactory >();
        final IExtensionRegistry registry = getExtensionRegistry();
        final IExtensionPoint point = registry.getExtensionPoint( "net.sf.groovyMonkey.lang" );
        if( point == null )
            return factories;
        final IExtension[] extensions = point.getExtensions();
        for( final IExtension extension : extensions )
        {
            for( final IConfigurationElement element : extension.getConfigurationElements() )
            {
                final String languageName = element.getAttribute( "name" );
                if( factories.containsKey( languageName ) )
                    // Ignoring duplicate extensions.
                    continue;
                try
                {
                    final IMonkeyScriptFactory factory = ( IMonkeyScriptFactory )element.createExecutableExtension( "factory" );
                    factories.put( languageName, factory );
                }
                catch( final CoreException e )
                {
                    // Ignoring bad extensions
                    continue;
                }   
            }
        }
        return factories;
    }
	private void error( final Throwable x, 
                        final String string ) 
    {
        error( x.getClass().getName(), string, x.getCause() != null ? x.getCause() : x );
	}
	private void error( final String title, 
                        final String message,
                        final Throwable exception )
    {
        if( throwError )
            throw new RuntimeException( title + ". " + message, exception );
        Utilities.error( title, message, exception );
    }
}
