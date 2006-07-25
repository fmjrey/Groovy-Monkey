package net.sf.groovyMonkey.dom.runner;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.swt.widgets.Display.getCurrent;
import static org.eclipse.swt.widgets.Display.getDefault;
import java.util.Map;
import net.sf.groovyMonkey.RunMonkeyScript;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IWorkbenchWindow;

public class RunnerDOM
{
    public IFile file( final String path )
    {
        return getWorkspace().getRoot().getFile( new Path( path ) );
    }
    /**
     * This is the method to run to have the monkey script run from within the same
     * job as the one invoking.
     * @param window
     * @param scriptPath
     * @param map
     * @param monitor
     */
    public Object runScript( final IWorkbenchWindow window,
                             final String scriptPath, 
                             final Map< String, Object > map,
                             final IProgressMonitor monitor )
    {
        final IFile file = file( scriptPath );
        final RunMonkeyScript script = new RunMonkeyScript( file, window, map, true );
        return script.runScript( monitor );
    }
    public Object runScript( final IWorkbenchWindow window,
                             final String scriptPath, 
                             final Map< String, Object > map )
    {
        final IFile file = file( scriptPath );
        final RunMonkeyScript script = new RunMonkeyScript( file, window, map, true );
        return script.run();
    }
    public Object runScript( final IWorkbenchWindow window,
                           final String scriptPath )
    {
        return runScript( window, scriptPath, null );
    }
    public void syncExec( final Runnable runnable )
    {
        if( getCurrent() != null )
        {
            runnable.run();
            return;
        }
        getDefault().syncExec( runnable );
    }
    public void asyncExec( final Runnable runnable )
    {
        if( getCurrent() != null )
        {
            runnable.run();
            return;
        }
        getDefault().asyncExec( runnable );
    }
}
