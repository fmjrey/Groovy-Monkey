package net.sf.groovyMonkey.dom.resources;
import static java.util.regex.Pattern.compile;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class Resources
{
    public static final String standardMarkerName = "Groovy Monkey Script";

    public Resources()
    {
    }
    public Object[] filesMatching( final String patternString )
    {
        final Pattern pattern = compile( patternString );
        final List< Object > result = new ArrayList< Object >();
        try
        {
            final IProject[] projects = getWorkspace().getRoot().getProjects();
            for( final IProject project : projects )
                walk( project, pattern, result );
        }
        catch( final CoreException x )
        {
            // ignore Eclipse internal errors
        }
        return result.toArray();
    }
    private void walk( final IResource resource, 
                       final Pattern pattern, 
                       final Collection< Object > result ) 
    throws CoreException
    {
        if( resource instanceof IFolder )
        {
            final IResource[] children = ( ( IFolder )resource ).members();
            for( final IResource resource2 : children )
            {
                walk( resource2, pattern, result );
            }
        }
        else if( resource instanceof IProject )
        {
            final IProject project = ( IProject )resource;
            if( !project.isOpen() )
                return;
            final IResource[] children = project.members();
            for( final IResource resource2 : children )
                walk( resource2, pattern, result );
        }
        else if( resource instanceof IFile )
        {
            final String path = resource.getFullPath().toString();
            final Matcher match = pattern.matcher( path );
            if( match.matches() )
                result.add( new File( resource ) );
        }
    }
}
