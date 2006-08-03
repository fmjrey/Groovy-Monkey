package net.sf.groovyMonkey.dom.resources;
import static net.sf.groovyMonkey.dom.resources.Resources.standardMarkerName;
import static org.eclipse.core.resources.IMarker.LINE_NUMBER;
import static org.eclipse.core.resources.IMarker.MESSAGE;
import static org.eclipse.core.resources.IMarker.TASK;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class Line
{
    private final String text;
    private final int lineNumber;
    private final File file;

    public Line( final String text, 
                 final int number, 
                 final File file )
    {
        this.text = text;
        this.lineNumber = number;
        this.file = file;
    }
    public String getString()
    {
        return text;
    }
    public int getLineNumber()
    {
        return lineNumber;
    }
    public void addMyTask( final String scriptID,
                           final String message ) 
    throws CoreException
    {
        final IMarker marker = file.getEclipseObject().createMarker( TASK );
        marker.setAttribute( standardMarkerName, scriptID );
        marker.setAttribute( MESSAGE, message );
        marker.setAttribute( LINE_NUMBER, lineNumber );
    }
}
