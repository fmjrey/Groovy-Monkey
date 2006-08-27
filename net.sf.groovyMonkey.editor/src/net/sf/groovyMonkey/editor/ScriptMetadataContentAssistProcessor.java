package net.sf.groovyMonkey.editor;
import static net.sf.groovyMonkey.util.ListUtils.list;
import static org.apache.commons.lang.StringUtils.isBlank;
import java.util.List;
import net.sf.groovyMonkey.Tags;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class ScriptMetadataContentAssistProcessor 
implements IContentAssistProcessor
{
    public ICompletionProposal[] computeCompletionProposals( final ITextViewer viewer, 
                                                             final int offset )
    {
        final IDocument document = viewer.getDocument();
        try
        {
            final int lineNumber = document.getLineOfOffset( offset );
            final int lineStart = document.getLineOffset( lineNumber );
            final String line = document.get( lineStart, document.getLineLength( lineNumber ) );
            System.out.println( "ScriptMetadataContentAssistProcessor.computeCompletionProposals(): " + line );
            if( isBlank( line ) )
                return new ICompletionProposal[ 0 ];
            // The next few lines try to assure that the offset is after the first and only '* '
            //  in the line.
            final String prefix = line.substring( 0, offset - lineStart + 1 );
            if( prefix.indexOf( "* " ) == -1 )
                return new ICompletionProposal[ 0 ];
            if( prefix.substring( 0, prefix.indexOf( "* " ) ).contains( "*" ) )
                return new ICompletionProposal[ 0 ];
            if( document.getChar( offset ) == ' ' && document.getChar( offset - 1 ) == '*' )
                return proposals();
        }
        catch( final BadLocationException e )
        {
            throw new RuntimeException( e );
        }
        return new ICompletionProposal[ 0 ];
    }
    public IContextInformation[] computeContextInformation( final ITextViewer viewer, 
                                                            final int offset )
    {
        return new IContextInformation[ 0 ];
    }
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return new char[] { '*' };
    }
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }
    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }
    public String getErrorMessage()
    {
        return null;
    }
    public ICompletionProposal[] proposals()
    {
        final List< ICompletionProposal > list = list();
        for( final Tags.Type type : Tags.Type.values() )
            list.add( new TagCompletionProposal( Tags.getTag( type ) ) );
        return list.toArray( new ICompletionProposal[ 0 ] );
    }
}
