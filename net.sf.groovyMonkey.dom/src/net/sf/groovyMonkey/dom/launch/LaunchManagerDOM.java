package net.sf.groovyMonkey.dom.launch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

public class LaunchManagerDOM
{
    public ILaunchManager manager()
    {
        return DebugPlugin.getDefault().getLaunchManager();
    }
    public IWorkbenchWindow window()
    {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }
    public Shell shell()
    {
        return window().getShell();
    }
    public void error( final String title,
                       final String message )
    {
        new UIJob( "Warning: " + title )
        {
            @Override
            public IStatus runInUIThread( final IProgressMonitor monitor )
            {
                MessageDialog.openError( shell(), title, message );
                return Status.OK_STATUS;
            }

        }.schedule();
    }
    public void warning( final String title,
                         final String message )
    {
        new UIJob( "Warning: " + title )
        {
            @Override
            public IStatus runInUIThread( final IProgressMonitor monitor )
            {
                MessageDialog.openWarning( shell(), title, message);
                return Status.OK_STATUS;
            }

        }.schedule();
    }
    public Job launch( final String name,
    				   final List< String> configurationNames )
    {
    	return launchWithMode( name, ILaunchManager.RUN_MODE, configurationNames );
    }
    public Job launchWithMode( final String name,
    			    		   final String mode,
    						   final List< String > configurationNames )
    {
        if( configurationNames == null || configurationNames.isEmpty() )
            return null;
        final List< ILaunchConfiguration > configurations = getLaunchConfigurations( configurationNames );
        final Job job = new Job( "" + name )
        {
            @Override
            protected IStatus run( final IProgressMonitor progressMonitor )
            {
                final IProgressMonitor monitor = progressMonitor != null ? progressMonitor : new NullProgressMonitor();
                final List< String > finished = new ArrayList< String >();
                monitor.beginTask( name + " " + complete( finished, configurations ), configurations.size() );
                for( final ILaunchConfiguration configuration : configurations )
                {
                    final String inProgress = "in progress: " + configuration.getName() + ", finished " + complete( finished, configurations );
                    try
                    {
                        monitor.subTask( configuration.getName() + " " + complete( finished, configurations ) );
                        final ILaunch launch = DebugUITools.buildAndLaunch( configuration, mode, monitor );
                        while( true )
                        {
                            try
                            {
                                Thread.sleep( 500 );
                            }
                            catch( final InterruptedException e ) {}
                            if( launch.getProcesses().length == 0 || launch.isTerminated() )
                                break;
                            if( monitor.isCanceled() )
                            {
                                warning( "Cancelled launching: " + configuration.getName(), inProgress );
                                return new Status( IStatus.CANCEL, "net.sourceforge.groovyMonkey.launch", -1, "Cancelled launching: " + configuration.getName()  + ", " + inProgress, null );
                            }
                        }
                    }
                    catch( final CoreException e )
                    {
                        error( "Error launching: " + configuration.getName(),
                                inProgress + ". " + e.getMessage() );
                        return new Status( IStatus.ERROR, "net.sourceforge.groovyMonkey.launch", -1, "Error launching: " + configuration.getName()  + inProgress + ". " + e.getMessage(), e );
                    }
                    finished.add( configuration.getName() );
                    monitor.worked( 1 );
                }
                monitor.done();
                return Status.OK_STATUS;
            }
            private String complete( final List< String > finished,
                                     final List< ILaunchConfiguration > total )
            {
                return "( " + finished.size() + " / " + total.size() + " )";
            }
        };
        job.setUser( false );
        job.schedule();
        return job;
    }
    private List< ILaunchConfiguration > getLaunchConfigurations( final List< String > configurationNames )
    {
        final List< ILaunchConfiguration > list = new ArrayList< ILaunchConfiguration >();
        final ILaunchConfiguration[] launchConfigurations;
        try
        {
            launchConfigurations = manager().getLaunchConfigurations();
        }
        catch( final CoreException e )
        {
            throw new RuntimeException( "Error could not get the set of launch configurations from Eclipse: " + e.getMessage(), e );
        }
        for( final String configName : configurationNames )
        {
            boolean found = false;
            for( final ILaunchConfiguration launchConfig : launchConfigurations )
            {
                if( launchConfig.getName().equals( configName ) )
                {
                    list.add( launchConfig );
                    found = true;
                    break;
                }
            }
            if( !found )
                throw new RuntimeException( "Error could not find launch config: " + configName + " in list of launch configurations" );
        }
        return list;
    }
    public Job launchWithMode( final String name,
    						   final String mode,
    						   final String... configurations )
    {
        return launchWithMode( name, mode, Arrays.asList( configurations ) );
    }
    public Job launch( final String name,
            		   final String... configurations )
    {
    	return launch( name, Arrays.asList( configurations ) );
    }
}
