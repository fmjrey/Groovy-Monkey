package net.sf.groovyMonkey.dom.resources;
import static net.sf.groovyMonkey.dom.Utilities.SCRIPT_NAME;
import static net.sf.groovyMonkey.dom.Utilities.state;
import static net.sf.groovyMonkey.dom.resources.Resources.standardMarkerName;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.readLines;
import static org.eclipse.core.resources.IMarker.TASK;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class File
{
    private final IFile file;

    public File( final IResource resource )
    {
        file = ( IFile )resource;
    }
    public IFile getEclipseObject()
    {
        return file;
    }
    public List< Line > getLines()
    {
        InputStream input = null;
        try
        {
            input = file.getContents();
            final List lines = readLines( new BufferedInputStream( input ) );
            int lineNumber = 0;
            final List< Line > result = new ArrayList< Line >();
            for( Object line : lines )
            {
                lineNumber++;
                result.add( new Line( "" + line, lineNumber, this ) );
            }
            return result;
        }
        catch( final CoreException x )
        {
            return new ArrayList< Line >();
        }
        catch( final IOException x )
        {
            return new ArrayList< Line >();
        }
        finally
        {
            closeQuietly( input );
        }
    }
    public void removeMyTasks() 
    throws CoreException
    {
        final String key = this.getMarkerKey();
        removeMyTasks( key );
    }
    public void removeMyTasks( final String key ) 
    throws CoreException
    {
        final IMarker[] markers = file.findMarkers( TASK, false, 0 );
        for( IMarker marker : markers )
            if( key.equals( marker.getAttribute( standardMarkerName ) ) )
                marker.delete();
    }
    public String getMarkerKey()
    {
        return ( String )state().get( SCRIPT_NAME );
    }
}
