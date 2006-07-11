package net.sf.groovyMonkey.dom.project;
import static org.apache.commons.io.IOUtils.toInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import net.sf.groovyMonkey.dom.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class Project
{
    IProject project;
    Attributes attributes = null;

    public Project( final IProject p )
    {
        super();
        project = p;
    }
    public IProject getEclipseObject()
    {
        return project;
    }
    public Object createFile( final String fileName )
    throws CoreException
    {
        final IFile file = project.getFile( fileName );
        file.create( toInputStream( "" ), true, null );
        return new File( file );
    }
    public String name()
    {
        return project.getName();
    }
    public String id() 
    throws CoreException, IOException
    {
        return manifestAttributes().getValue( "Bundle-SymbolicName" );
    }
    public Version version() 
    throws CoreException, IOException
    {
        final String version = manifestAttributes().getValue( "Bundle-Version" );
        if( version == null )
            return new Version( "0.0.0" );
        return new Version( version );
    }
    public String provider() 
    throws CoreException, IOException
    {
        final String vendor = manifestAttributes().getValue( "Bundle-Vendor" );
        if( vendor == null )
            return "no provider";
        return vendor;
    }
    public Attributes manifestAttributes() 
    throws CoreException, IOException
    {
        if( attributes != null )
            return attributes;
        final Manifest manifest = new Manifest( project.getFile( "META-INF/MANIFEST.MF" ).getContents() );
        attributes = manifest.getMainAttributes();
        return attributes;
    }
}
