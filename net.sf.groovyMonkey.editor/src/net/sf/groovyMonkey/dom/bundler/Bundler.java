package net.sf.groovyMonkey.dom.bundler;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.eclipse.pde.internal.core.PDECore.getDefault;
import static org.eclipse.pde.internal.ui.PDEPluginImages.DESC_PLUGIN_OBJ;
import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.progress.IProgressConstants.ICON_PROPERTY;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import net.sf.groovyMonkey.dom.project.Project;
import org.apache.commons.lang.Validate;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.internal.ui.build.FeatureExportInfo;
import org.eclipse.pde.internal.ui.build.PluginExportJob;
import org.eclipse.ui.progress.IProgressService;

public class Bundler
{
    public static final String DEPLOY_DIR = "/tmp/deployedBundles";
    private String deployDirPath = DEPLOY_DIR;
    private File deployDir;

    public Bundler setDeployDir( final String deployDirPath )
    {
        this.deployDirPath = deployDirPath;
        return this;
    }
    public String getDeployDir()
    {
        return deployDirPath;
    }
    public Bundler createDeployDir() 
    throws IOException
    {
        deployDir = new File( deployDirPath );
        if( deployDir.exists() )
            deleteDirectory( deployDir );
        try
        {
            Thread.sleep( 1000 );
        }
        catch( final InterruptedException e ) {}
        deployDir.mkdir();
        return this;
    }
    public Bundler buildPluginJar( final Project project ) 
    throws InterruptedException, InvocationTargetException
    {
        return buildPluginJar( project.getEclipseObject() );
    }
    public Bundler buildPluginJar( final IProject project ) 
    throws InvocationTargetException, InterruptedException
    {
        Validate.notNull( project );
        final IModel model = getDefault().getModelManager().findModel( project );
        if( model == null )
            throw new RuntimeException( "Error no such plugin project: " + project.getName() );
        final FeatureExportInfo info = new FeatureExportInfo();
        info.toDirectory = true;
        info.useJarFormat = true;
        info.exportSource = false;
        info.destinationDirectory = deployDir.getAbsolutePath();
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
        return this;
    }
}
