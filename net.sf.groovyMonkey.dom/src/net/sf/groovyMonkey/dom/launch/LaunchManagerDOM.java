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

public class LaunchManagerDOM
{
    public ILaunchManager manager()
    {
        return DebugPlugin.getDefault().getLaunchManager();
    }
    public LaunchManagerDOM launch( final String name,
                                    final List< String > configurationNames ) 
    {
        if( configurationNames == null || configurationNames.isEmpty() )
            return this;
        final List< ILaunchConfiguration > configurations = getLaunchConfigurations( configurationNames );
        new Job( "" + name )
        {
            @Override
            protected IStatus run( final IProgressMonitor progressMonitor )
            {
                final IProgressMonitor monitor = progressMonitor != null ? progressMonitor : new NullProgressMonitor();
                monitor.beginTask( "" + name, configurations.size() );
                for( final ILaunchConfiguration configuration : configurations )
                {
                    try
                    {
                        monitor.subTask( configuration.getName() );
                        final ILaunch launch = DebugUITools.buildAndLaunch( configuration, ILaunchManager.RUN_MODE, monitor );
                        while( true )
                        {
                            try
                            {
                                Thread.sleep( 500 );
                            }
                            catch( InterruptedException e ) {}
                            if( monitor.isCanceled() )
                                return Status.CANCEL_STATUS;
                            if( launch.getProcesses().length == 0 || launch.isTerminated() )
                                break;
                        }
                    }
                    catch( CoreException e )
                    {
                        MessageDialog.openError( null, "Error launching: " + configuration.getName(), e.getMessage() );
                        return new Status( IStatus.ERROR, "net.sourceforge.groovyMonkey.launch", -1, "Error launching: " + configuration.getName() + ": " + e.getMessage(), e );
                    }
                    monitor.worked( 1 );
                }
                monitor.done();
                return Status.OK_STATUS;
            }
            
        }.schedule();
        return this;
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
    public LaunchManagerDOM launch( final String name,
                                    final String... configurations ) 
    {
        return launch( name, Arrays.asList( configurations ) );
    }
}
