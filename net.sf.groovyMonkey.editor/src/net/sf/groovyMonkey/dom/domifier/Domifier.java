package net.sf.groovyMonkey.dom.domifier;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.eclipse.pde.internal.core.PDECore.getDefault;
import static org.eclipse.pde.internal.ui.PDEPluginImages.DESC_PLUGIN_OBJ;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.progress.IProgressConstants.ICON_PROPERTY;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import net.sf.groovyMonkey.dom.project.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.ui.build.PluginExportJob;
import org.eclipse.ui.progress.IProgressService;

public class Domifier
{
    private File tempdir;

    public void createTempDirectory() 
    throws IOException
    {
        tempdir = new File( "/tmp/domifier" );
        if( tempdir.exists() )
            deleteDirectory( tempdir );
        try
        {
            Thread.sleep( 1000 );
        }
        catch( final InterruptedException e ) {}
        tempdir.mkdir();
    }
    public void buildPluginJar( final Project project ) 
    throws InterruptedException, InvocationTargetException
    {
        buildPluginJar( project.getEclipseObject() );
    }
    public void buildPluginJar( final IProject project ) 
    throws InvocationTargetException, InterruptedException
    {
        final IModel model = getDefault().getModelManager().findModel( project );
        if( model == null )
            return;
        final FeatureExportInfo info = new FeatureExportInfo();
        info.toDirectory = true;
        info.useJarFormat = true;
        info.exportSource = false;
        info.destinationDirectory = tempdir.getAbsolutePath();
        info.zipFileName = null;
        info.items = new Object[]{ model };
        info.signingInfo = null;
        final PluginExportJob job = new PluginExportJob( info );
        job.setUser( true );
        job.schedule();
        job.setProperty( ICON_PROPERTY, DESC_PLUGIN_OBJ );
        final IProgressService progressService = getWorkbench().getProgressService();
        progressService.busyCursorWhile( new IRunnableWithProgress()
        {
            public void run( final IProgressMonitor monitor ) 
            throws InterruptedException
            {
                job.join();
            }
        } );
        System.out.println( job.getResult() + " done with building" );
    }
}
