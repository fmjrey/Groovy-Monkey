package net.sf.groovyMonkey.editor;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.logExceptionWarning;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.dom.Utilities.getContents;
import static net.sf.groovyMonkey.dom.Utilities.isMonkeyScript;
import static org.eclipse.core.resources.IResourceDelta.ADDED;
import static org.eclipse.core.resources.IResourceDelta.CHANGED;
import static org.eclipse.core.resources.IResourceDelta.REMOVED;
import static org.eclipse.swt.widgets.Display.getCurrent;
import static org.eclipse.swt.widgets.Display.getDefault;
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
        changes[ 0 ] = Boolean.FALSE;
        final IFile[] changedFile = new IFile[ 1 ];
        final IResourceDeltaVisitor visitor = new IResourceDeltaVisitor()
        {
            private void found_a_change( final IFile file )
            {
                changes[ 0 ] = Boolean.TRUE;
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
                        case ADDED:
                            found_a_change( file );
                            break;
                        case REMOVED:
                            found_a_change( file );
                            break;
                        case CHANGED:
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
            logExceptionWarning( x );
        }
        final boolean anyMatches = changes[ 0 ].booleanValue();
        if( anyMatches && diff( changedFile[ 0 ] ) )
            updateViewer();
    }
    public void updateViewer()
    {
        if( getCurrent() == null )
        {
            final Runnable runnable = new Runnable()
            {
                public void run()
                {
                    updateViewer();
                }
            };
            getDefault().asyncExec( runnable );
            return;
        }
        // This is how I get it to bloody well redraw, it is an ugly hack aint it?
        viewer.setInput( viewer.getInput() );
    }
    private boolean diff( final IFile changedScript )
    {
        try
        {
            return provider.diff( getScriptMetadata( getContents( changedScript ) ) );
        }
        catch( final Exception e ) 
        {
            logExceptionWarning( e );
        }
        return true;
    }
}
