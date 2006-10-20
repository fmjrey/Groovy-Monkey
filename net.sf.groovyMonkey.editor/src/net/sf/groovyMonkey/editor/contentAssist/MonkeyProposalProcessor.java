package net.sf.groovyMonkey.editor.contentAssist;
import static java.lang.Character.isJavaIdentifierPart;
import static net.sf.groovyMonkey.ScriptMetadata.getScriptMetadata;
import static net.sf.groovyMonkey.dom.Utilities.getDOMInfo;
import static net.sf.groovyMonkey.util.ListUtils.add;
import static net.sf.groovyMonkey.util.ListUtils.array;
import static net.sf.groovyMonkey.util.ListUtils.treeList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.remove;
import java.util.List;
import net.sf.groovyMonkey.DOMDescriptor;
import net.sf.groovyMonkey.ScriptMetadata;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class MonkeyProposalProcessor 
implements IContentAssistProcessor
{
    private static final class Proposal 
    implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2,
               ICompletionProposalExtension3, ICompletionProposalExtension4
    {
        private final String fString;
        private final String fPrefix;
        private final int fOffset;
        
        public Proposal( String string, String prefix, int offset )
        {
            fString = string;
            fPrefix = prefix;
            fOffset = offset;
        }
        public void apply( IDocument document )
        {
            apply( null, '\0', 0, fOffset );
        }
        public Point getSelection( IDocument document )
        {
            return new Point( fOffset + fString.length(), 0 );
        }
        public String getAdditionalProposalInfo()
        {
            return null;
        }
        public String getDisplayString()
        {
            return fPrefix + fString;
        }
        public Image getImage()
        {
            return null;
        }
        public IContextInformation getContextInformation()
        {
            return null;
        }
        public void apply( IDocument document, char trigger, int offset )
        {
            try
            {
                String replacement = fString.substring( offset - fOffset );
                document.replace( offset, 0, replacement );
            }
            catch( BadLocationException x )
            {
                // TODO Auto-generated catch block
                x.printStackTrace();
            }
        }
        public boolean isValidFor( IDocument document, int offset )
        {
            return validate( document, offset, null );
        }
        public char[] getTriggerCharacters()
        {
            return null;
        }
        public int getContextInformationPosition()
        {
            return 0;
        }
        public void apply( ITextViewer viewer, char trigger, int stateMask, int offset )
        {
            apply( viewer.getDocument(), trigger, offset );
        }
        public void selected( ITextViewer viewer, boolean smartToggle )
        {
        }
        public void unselected( ITextViewer viewer )
        {
        }
        public boolean validate( IDocument document, int offset, DocumentEvent event )
        {
            try
            {
                int prefixStart = fOffset - fPrefix.length();
                return offset >= fOffset && offset < fOffset + fString.length()
                        && document.get( prefixStart, offset - ( prefixStart ) ).equals( ( fPrefix + fString ).substring( 0, offset - prefixStart ) );
            }
            catch( BadLocationException x )
            {
                return false;
            }
        }
        public IInformationControlCreator getInformationControlCreator()
        {
            return null;
        }
        public CharSequence getPrefixCompletionText( IDocument document, int completionOffset )
        {
            return fPrefix + fString;
        }
        public int getPrefixCompletionStart( IDocument document, int completionOffset )
        {
            return fOffset - fPrefix.length();
        }
        public boolean isAutoInsertable()
        {
            return true;
        }
    }
    private final HippieProposalProcessor processor;
    
    public MonkeyProposalProcessor()
    {
        this.processor = new HippieProposalProcessor();
    }
    public ICompletionProposal[] computeCompletionProposals( final ITextViewer viewer, 
                                                             final int offset )
    {
        final List< ICompletionProposal > proposals = proposals( viewer, offset );
        add( proposals, processor.computeCompletionProposals( viewer, offset ) );
        return proposals.toArray( new ICompletionProposal[ 0 ] );
    }
    public static List< ICompletionProposal > proposals( final ITextViewer viewer, 
                                                         final int offset )
    {
        final List< ICompletionProposal > list = array();
        final ScriptMetadata metadata = getScriptMetadata( viewer.getDocument().get() );
        final List< String > varNames = treeList();
        for( final DOMDescriptor descriptor : metadata.getDOMs() )
            varNames.addAll( getDOMInfo( descriptor.pluginName ).keySet() );
        final IDocument document = viewer.getDocument();
        try
        {
            final int lineNumber = document.getLineOfOffset( offset );
            final int lineStart = document.getLineOffset( lineNumber );
            final String line = document.get( lineStart, document.getLineLength( lineNumber ) );
            if( isBlank( line ) )
            {
                for( final String varName : varNames )
                    if( metadata.getLang().trim().toLowerCase().equals( "ruby" ) )
                        list.add( new Proposal( "$" + varName, "", offset ) );
                    else
                        list.add( new Proposal( varName, "", offset ) );
                return list;
            }
            final String prefix = getPrefix( viewer, offset );
            for( final String varName : varNames )
            {
                final String var = metadata.getLang().trim().toLowerCase().equals( "ruby" ) ? "$" + varName : varName;
                if( var.equals( prefix ) || var.startsWith( prefix ) )
                    list.add( new Proposal( remove( varName, prefix ), prefix, offset ) );
            }
        }
        catch( final BadLocationException e )
        {
            throw new RuntimeException( e );
        }
        return list;
    }
    public static String getPrefix( final ITextViewer viewer, 
                                    int offset ) 
    throws BadLocationException 
    {
        final IDocument doc= viewer.getDocument();
        if( doc == null || offset > doc.getLength() )
            return "";
        
        int length= 0;
        while( --offset >= 0 && isJavaIdentifierPart( doc.getChar( offset ) ) )
            length++;
        
        return doc.get( offset + 1, length );
    }
    public IContextInformation[] computeContextInformation( final ITextViewer viewer, 
                                                            final int offset )
    {
        return processor.computeContextInformation( viewer, offset );
    }
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return processor.getCompletionProposalAutoActivationCharacters();
    }
    public char[] getContextInformationAutoActivationCharacters()
    {
        return processor.getContextInformationAutoActivationCharacters();
    }
    public IContextInformationValidator getContextInformationValidator()
    {
        return processor.getContextInformationValidator();
    }
    public String getErrorMessage()
    {
        return processor.getErrorMessage();
    }
}
