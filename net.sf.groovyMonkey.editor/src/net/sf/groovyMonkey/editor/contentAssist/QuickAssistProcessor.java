package net.sf.groovyMonkey.editor.contentAssist;
import static net.sf.groovyMonkey.GroovyMonkeyPlugin.logWarning;
import static net.sf.groovyMonkey.util.ListUtil.list;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.IMarkerResolution;

public class QuickAssistProcessor
implements IQuickAssistProcessor
{
    private final IAdaptable adapter;

    public QuickAssistProcessor( final IAdaptable adapter )
    {
        Validate.notNull( adapter );
        this.adapter = adapter;
    }
    public boolean canAssist( final IQuickAssistInvocationContext invocationContext )
    {
        logWarning( "invocationContext: " + invocationContext );
        return false;
    }
    public boolean canFix( final Annotation annotation )
    {
        logWarning( "annotation: " + annotation );
        return false;
    }
    public ICompletionProposal[] computeQuickAssistProposals( final IQuickAssistInvocationContext context )
    {
        final IDocument document = context.getSourceViewer().getDocument();
        final IFile script = ( IFile )adapter.getAdapter( IFile.class );
        final List< ICompletionProposal > proposals = list();
        final MarkerResolutionGenerator generator = new MarkerResolutionGenerator();
        try
        {
            for( final IMarker marker : script.findMarkers( null, true, IResource.DEPTH_ONE ) )
            {
                final int charStart = marker.getAttribute( "charStart" ) != null ? ( Integer )marker.getAttribute( "charStart" ) : document.getLineOffset( ( Integer )marker.getAttribute( "lineNumber" ) );
                final int charEnd = marker.getAttribute( "charEnd" ) != null ? ( Integer )marker.getAttribute( "charEnd" ) :  document.getLineLength( ( Integer )marker.getAttribute( "lineNumber" ) );
                if( ( charStart <= context.getOffset() && charEnd >= context.getOffset() )
                    || ( charStart > context.getOffset() && charStart <= context.getOffset() + context.getLength() ) )
                {
                    for( final IMarkerResolution resolution : generator.getResolutions( marker ) )
                    {
                        if( !( resolution instanceof ICompletionProposal ) )
                            continue;
                        proposals.add( ( ICompletionProposal )resolution );
                    }
                }
            }
            return proposals.size() != 0 ? proposals.toArray( new ICompletionProposal[ 0 ] ) : null;
        }
        catch( final CoreException e )
        {
            throw new RuntimeException( e );
        }
        catch( final BadLocationException e )
        {
            throw new RuntimeException( e );
        }
    }
    public String getErrorMessage()
    {
        return null;
    }
}
