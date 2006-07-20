package net.sf.groovyMonkey.editor.wizard;
import static net.sf.groovyMonkey.dom.Utilities.activePage;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.jface.dialogs.MessageDialog.openError;
import static org.eclipse.ui.ide.IDE.openEditor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import net.sf.groovyMonkey.ScriptMetadata;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the
 * provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "mpe". If a
 * sample multi-page editor (also available as a template) is registered for the
 * same extension, it will be able to open it.
 */
public class NewScriptWizard 
extends Wizard 
implements INewWizard
{
    private NewScriptWizardPage page;
    private ISelection selection;
    /**
     * Constructor for SampleNewWizard.
     */
    public NewScriptWizard()
    {
        super();
        setNeedsProgressMonitor( true );
    }
    /**
     * Adding the page to the wizard.
     */
    @Override
    public void addPages()
    {
        page = new NewScriptWizardPage( selection );
        addPage( page );
    }
    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     */
    @Override
    public boolean performFinish()
    {
        page.finished();
        final String containerName = page.getContainerName();
        final String fileName = page.getFileName();
        final ScriptMetadata metadata = new ScriptMetadata();
        metadata.setMenuName( page.getMenuName() );
        metadata.setLang( page.getLang() );
        metadata.setKudos( page.getKudos() );
        metadata.setJobMode( page.getJobMode() );
        metadata.setExecMode( page.getExecMode() );
        final IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run( final IProgressMonitor monitor ) 
            throws InvocationTargetException
            {
                try
                {
                    doFinish( containerName, fileName, metadata, monitor );
                }
                catch( final CoreException e )
                {
                    throw new InvocationTargetException( e );
                }
                finally
                {
                    monitor.done();
                }
            }
        };
        try
        {
            getContainer().run( true, false, op );
        }
        catch( final InterruptedException e )
        {
            return false;
        }
        catch( final InvocationTargetException e )
        {
            final Throwable realException = e.getTargetException();
            openError( getShell(), "Error", realException.getMessage() );
            return false;
        }
        return true;
    }
    /**
     * The worker method. It will find the container, create the file if missing
     * or just replace its contents, and open the editor on the newly created
     * file.
     */
    private void doFinish( final String containerName, 
                           final String fileName,
                           final ScriptMetadata metadata,
                           final IProgressMonitor monitor ) 
    throws CoreException
    {
        monitor.beginTask( "Creating " + fileName, 2 );
        final IWorkspaceRoot root = getWorkspace().getRoot();
        final IResource resource = root.findMember( new Path( containerName ) );
        if( !resource.exists() || !( resource instanceof IContainer ) )
            throwCoreException( "Container \"" + containerName + "\" does not exist." );
        final IContainer container = ( IContainer )resource;
        final IFile file = container.getFile( new Path( fileName ) );
        metadata.setFile( file );
        try
        {
            final InputStream stream = openContentStream( metadata );
            if( file.exists() )
                file.setContents( stream, true, true, monitor );
            else
                file.create( stream, true, monitor );
            stream.close();
        }
        catch( final IOException e )
        {
        }
        monitor.worked( 1 );
        monitor.setTaskName( "Opening file for editing..." );
        getShell().getDisplay().asyncExec( new Runnable()
        {
            public void run()
            {
                try
                {
                    openEditor( activePage(), file, true );
                }
                catch( final PartInitException e )
                {
                }
            }
        } );
        monitor.worked( 1 );
    }
    /**
     * We will initialize file contents with the metadata header.
     */
    private InputStream openContentStream( final ScriptMetadata metadata )
    {
        return new ByteArrayInputStream( metadata.toHeader().getBytes() );
    }
    private void throwCoreException( final String message ) 
    throws CoreException
    {
        final IStatus status = new Status( IStatus.ERROR, "Groovy Monkey Plugin", IStatus.OK, message, null );
        throw new CoreException( status );
    }
    /**
     * We will accept the selection in the workbench to see if we can initialize
     * from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init( final IWorkbench workbench, 
                      final IStructuredSelection selection )
    {
        this.selection = selection;
    }
}
