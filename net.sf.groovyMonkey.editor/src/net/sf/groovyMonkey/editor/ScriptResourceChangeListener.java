package net.sf.groovyMonkey.editor;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.dom.Utilities.getFileContents;
import static net.sf.groovyMonkey.dom.Utilities.isMonkeyScript;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreeViewer;

public class ScriptResourceChangeListener 
implements IResourceChangeListener
{
    private final TreeViewer viewer;
    private final ScriptContentProvider provider;
    
    public ScriptResourceChangeListener( final TreeViewer viewer,
                                         final ScriptContentProvider provider )
    {
        this.viewer = viewer;
        this.provider = provider;
    }
    public void resourceChanged( final IResourceChangeEvent event )
    {
        final Boolean changes[] = new Boolean[ 1 ];
        changes[ 0 ] = new Boolean( false );
        final IFile[] changedFile = new IFile[ 1 ];
        final IResourceDeltaVisitor visitor = new IResourceDeltaVisitor()
        {
            private void found_a_change( final IFile file )
            {
                changes[ 0 ] = new Boolean( true );
                changedFile[ 0 ] = file;
            }
            public boolean visit( final IResourceDelta delta )
            {
                if( !( delta.getResource() instanceof IFile ) )
                    return true;
                final String fullPath = delta.getFullPath().toString();
                if( isMonkeyScript( fullPath ) )
                {
                    final IFile file = ( IFile )delta.getResource();
                    switch( delta.getKind() )
                    {
                        case IResourceDelta.ADDED:
                            found_a_change( file );
                            break;
                        case IResourceDelta.REMOVED:
                            found_a_change( file );
                            break;
                        case IResourceDelta.CHANGED:
                            found_a_change( file );
                            break;
                    }
                }
                return true;
            }
        };
        try
        {
            event.getDelta().accept( visitor );
        }
        catch( final CoreException x )
        {
            // log an error in the error log
            x.printStackTrace();
        }
        final boolean anyMatches = ( ( Boolean )( changes[ 0 ] ) ).booleanValue();
        if( anyMatches )
        {
            // This is how I get it to bloody well redraw, it is an ugly hack aint it?
            if( diff( changedFile[ 0 ] ) )
                viewer.setInput( viewer.getInput() );
        }
    }
    private boolean diff( final IFile changedScript )
    {
        try
        {
            return provider.diff( getScriptMetadata( getFileContents( changedScript ) ) );
        }
        catch( final Exception e ) {}
        return true;
    }
}
