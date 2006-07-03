package net.sf.groovyMonkey.dom.runner;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import java.util.Map;
import net.sf.groovyMonkey.RunMonkeyScript;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

public class MonkeyRunner
{
    /**
     * This is the method to run to have the monkey script run from within the same
     * job as the one invoking.
     * @param window
     * @param scriptPath
     * @param map
     * @param monitor
     */
    public void runScript( final IWorkbenchWindow window,
                           final String scriptPath, 
                           final Map< String, Object > map,
                           final IProgressMonitor monitor )
    {
        final IFile file = getWorkspace().getRoot().getFile( new Path( scriptPath ) );
        final RunMonkeyScript script = new RunMonkeyScript( file, window, map, true );
        script.runScript( monitor );
    }
    public void runScript( final IWorkbenchWindow window,
                           final String scriptPath, 
                           final Map< String, Object > map )
    {
        final IFile file = getWorkspace().getRoot().getFile( new Path( scriptPath ) );
        final RunMonkeyScript script = new RunMonkeyScript( file, window, map, true );
        script.run();
    }
    public void runScript( final IWorkbenchWindow window,
                           final String scriptPath )
    {
        runScript( window, scriptPath, null );
    }
    public void syncExec( final Runnable runnable )
    {
        if( Display.getCurrent() != null )
        {
            runnable.run();
            return;
        }
        Display.getDefault().syncExec( runnable );
    }
    public void asyncExec( final Runnable runnable )
    {
        if( Display.getCurrent() != null )
        {
            runnable.run();
            return;
        }
        Display.getDefault().asyncExec( runnable );
    }
}
