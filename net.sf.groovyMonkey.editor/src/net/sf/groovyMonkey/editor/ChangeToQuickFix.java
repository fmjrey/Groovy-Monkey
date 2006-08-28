package net.sf.groovyMonkey.editor;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.ScriptMetadata.refreshScriptMetadata;
import static net.sf.groovyMonkey.ScriptMetadata.stripMetadata;
import static net.sf.groovyMonkey.dom.Utilities.activePage;
import static org.apache.commons.lang.StringUtils.defaultString;
import java.io.IOException;
import net.sf.groovyMonkey.ScriptMetadata;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;

public class ChangeToQuickFix 
implements IMarkerResolution, ICompletionProposal
{
    public interface IRunnable
    {
        public void run( final ScriptMetadata metadata, 
                         final String value );
    }
    private final String value;
    private final IRunnable runnable;
    private final boolean defaultValue;
    private boolean force = true;
    
    public ChangeToQuickFix( final String value,
                             final IRunnable runnable,
                             final boolean defaultValue )
    {
        this.value = defaultString( value );
        this.runnable = runnable;
        this.defaultValue = defaultValue;
    }
    public ChangeToQuickFix( final String value,
                             final IRunnable runnable )
    {
        this( value, runnable, false );
    }
    public ChangeToQuickFix setForce( final boolean force )
    {
        this.force = force;
        return this;
    }
    public String getLabel()
    {
        return "Change to " + value + ( defaultValue ? " <default>" : "" );
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
    public void apply( final IDocument document )
    {
        final String contents = document.get();
        final ScriptMetadata metadata = getScriptMetadata( contents );
        final String code = stripMetadata( contents );
        this.runnable.run( metadata, value );
        document.set( metadata.header() + code );
        if( !force )
            return;
        // Since this should be activated when the user uses Ctrl-1, the
        //  active editor should be the one, if this doesn't work we could try
        //  and cheat using the script path metadata attribute.
        final IEditorPart editor = activePage().getActiveEditor();
        if( editor != null )
            activePage().saveEditor( editor, false );
    }
    public String getAdditionalProposalInfo()
    {
        return null;
    }
    public IContextInformation getContextInformation()
    {
        return null;
    }
    public String getDisplayString()
    {
        return getLabel();
    }
    public Image getImage()
    {
        return null;
    }
    public Point getSelection( IDocument document )
    {
        return null;
    }
}
