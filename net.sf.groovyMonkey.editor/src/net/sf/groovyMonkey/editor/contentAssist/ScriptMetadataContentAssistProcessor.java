package net.sf.groovyMonkey.editor.contentAssist;
import static net.sf.groovyMonkey.util.ListUtil.list;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import java.util.List;
import net.sf.groovyMonkey.Tags;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IMarkerResolution;

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
            if( isBlank( line ) )
                return new ICompletionProposal[ 0 ];
            // The next few lines try to assure that the offset is after the first and only '* '
            //  in the line.
            final String prefix = line.substring( 0, offset - lineStart + 1 );
            if( prefix.indexOf( "*" ) == -1 )
                return new ICompletionProposal[ 0 ];
            if( prefix.substring( 0, prefix.indexOf( "*" ) ).contains( "*" ) )
                return new ICompletionProposal[ 0 ];
            return proposals( offset, document, prefix );
        }
        catch( final BadLocationException e )
        {
            throw new RuntimeException( e );
        }
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
    // At this point the method is invoked when the line starts with '*' and has
    //  no other '*' in it.
    private ICompletionProposal[] proposals( final int offset,
                                             final IDocument document,
                                             final String beforeInsert )
    {
        final String alreadyTyped = substringAfterLast( beforeInsert, "*" ).trim();
        final List< ICompletionProposal > list = list();
        for( final Tags.Type type : Tags.Type.values() )
        {
            if( type == Tags.Type.BAD_TAG || type == Tags.Type.PATH )
                continue;
            if( !matchAlreadyTyped( alreadyTyped, type ) )
                continue;
            if( alreadyTyped.equals( Tags.getTag( type ) ) )
            {
                if( type == Tags.Type.EXEC_MODE )
                {
                    addExecModeSuggestions( list );
                    continue;
                }
                if( type == Tags.Type.JOB )
                {
                    addJobSuggestions( list );
                    continue;
                }
                if( type == Tags.Type.LANG )
                {
                    addLangSuggestions( list );
                    continue;
                }
            }
            final String insert = !substringAfterLast( beforeInsert, "*" ).startsWith( " " ) ? " " + Tags.getTag( type ) : Tags.getTag( type ) ;
            list.add( new CompletionProposal( insert, offset - alreadyTyped.length(), alreadyTyped.length(), insert.length() ) );
        }
        return list.toArray( new ICompletionProposal[ 0 ] );
    }
    private void addExecModeSuggestions( final List< ICompletionProposal > list )
    {
        final List< IMarkerResolution > resolutions = list();
        MarkerResolutionGenerator.addExecModeSuggestions( resolutions, false );
        for( final IMarkerResolution resolution : resolutions )
        {
            if( !( resolution instanceof ICompletionProposal ) )
                continue;
            list.add( ( ICompletionProposal )resolution );
        }
    }
    private void addJobSuggestions( final List< ICompletionProposal > list )
    {
        final List< IMarkerResolution > resolutions = list();
        MarkerResolutionGenerator.addJobSuggestions( resolutions, false );
        for( final IMarkerResolution resolution : resolutions )
        {
            if( !( resolution instanceof ICompletionProposal ) )
                continue;
            list.add( ( ICompletionProposal )resolution );
        }
    }
    private void addLangSuggestions( final List< ICompletionProposal > list )
    {
        final List< IMarkerResolution > resolutions = list();
        MarkerResolutionGenerator.addLangSuggestions( resolutions, false );
        for( final IMarkerResolution resolution : resolutions )
        {
            if( !( resolution instanceof ICompletionProposal ) )
                continue;
            list.add( ( ICompletionProposal )resolution );
        }
    }
    private boolean matchAlreadyTyped( final String alreadyTyped,
                                       final Tags.Type type )
    {
        final StringBuffer match = new StringBuffer();
        for( final char character : alreadyTyped.toCharArray() )
        {
            match.append( character );
            if( !Tags.getTag( type ).toLowerCase().startsWith( match.toString().toLowerCase() ) )
                return false;
        }
        return true;
    }
}
