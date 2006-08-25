package net.sf.groovyMonkey.editor;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.ScriptMetadata.refreshScriptMetadata;
import static org.apache.commons.lang.StringUtils.defaultString;
import java.io.IOException;
import net.sf.groovyMonkey.ScriptMetadata;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;

public class ChangeToQuickFix 
implements IMarkerResolution
{
    public interface IRunnable
    {
        public void run( final ScriptMetadata metadata, final String value );
    }
    private final String value;
    private final IRunnable runnable;
    
    public ChangeToQuickFix( final String value,
                             final IRunnable runnable )
    {
        this.value = defaultString( value );
        this.runnable = runnable;
    }
    public String getLabel()
    {
        return "Change to " + value;
    }
    public void run( final IMarker marker )
    {
        if( !( marker.getResource() instanceof IFile ) )
            return;
        final IFile script = ( IFile )marker.getResource();
        try
        {
            final ScriptMetadata metadata = getScriptMetadata( script );
            this.runnable.run( metadata, value );
            refreshScriptMetadata( script, metadata, true );
        }
        catch( final CoreException e )
        {
            throw new RuntimeException( e );
        }
        catch( final IOException e )
        {
            throw new RuntimeException( e );
        }
    }
}
