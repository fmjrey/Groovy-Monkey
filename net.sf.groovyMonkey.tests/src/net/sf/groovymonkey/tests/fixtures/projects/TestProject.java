package net.sf.groovymonkey.tests.fixtures.projects;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import java.io.InputStream;
import org.apache.commons.lang.Validate;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class TestProject
{
    protected final String name;
    protected final IProject project;
    
    public static IFile createFile( final IContainer folder, 
                                    final String name, 
                                    final InputStream contents ) 
    throws CoreException
    {
        final IFile file = folder.getFile( new Path( name ) );
        file.create( contents, IResource.FORCE, null );
        return file;
    }
    public TestProject( final String name )
    throws CoreException
    {
        Validate.notNull( name );
        this.name = name;
        final IWorkspaceRoot root = getWorkspace().getRoot();
        project = root.getProject( name );
        project.create( null );
        project.open( null );
    }
    public IProject getProject()
    {
        return project;
    }
    public IProject project()
    {
        return getProject();
    }
    public IFile makeFile( final String folderPath, 
                           final String fileName,
                           final InputStream input ) 
    throws CoreException
    {
        return createFile( createFolder( folderPath ), fileName, input );
    }
    public IFile makeFile( final String folderPath, 
                           final String fileName,
                           final String contents ) 
    throws CoreException
    {
        return createFile( createFolder( folderPath ), fileName, toInputStream( contents ) );
    }
    public IFolder createFolder( final String projectPath ) 
    throws CoreException
    {
        final IFolder folder = project.getFolder( projectPath );
        if( !folder.exists() )
            folder.create( false, true, null );
        return folder;
    }
    public void dispose() 
    throws CoreException
    {
        project.delete( true, true, null );
    }
}
